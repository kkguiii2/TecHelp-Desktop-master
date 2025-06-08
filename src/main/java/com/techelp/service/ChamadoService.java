package com.techelp.service;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Interacao;
import com.techelp.model.entity.Usuario;
import com.techelp.model.dto.ChamadoDTO;
import com.techelp.repository.ChamadoRepository;
import com.techelp.repository.InteracaoRepository;
import com.techelp.repository.UsuarioRepository;
import com.techelp.service.NotificacaoService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.techelp.config.DatabaseConfig;
import com.techelp.config.AppConfig;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import com.techelp.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChamadoService {
    private static final Logger logger = LoggerFactory.getLogger(ChamadoService.class);
    private final ChamadoRepository chamadoRepository;
    private final InteracaoRepository interacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AssistenteService assistenteService;
    private final NotificacaoService notificacaoService;
    private final CacheManager cacheManager;
    
    public ChamadoService() {
        this.chamadoRepository = new ChamadoRepository();
        this.interacaoRepository = new InteracaoRepository();
        this.usuarioRepository = new UsuarioRepository();
        this.assistenteService = AppConfig.getAssistenteService();
        this.notificacaoService = new NotificacaoService();
        this.cacheManager = CacheManager.getInstance();
    }
    
    public ChamadoDTO abrirChamado(String titulo, String descricao, Usuario solicitante) {
        Chamado chamado = new Chamado();
        chamado.setTitulo(titulo);
        chamado.setDescricao(descricao);
        chamado.setSolicitante(solicitante);
        chamado.setStatus(Chamado.StatusChamado.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setCategoriaIa(assistenteService.classificarChamado(chamado));
        chamado.setCategoria(Chamado.CategoriaChamado.OUTROS.getDescricao());
        
        // Ajusta a prioridade automaticamente
        ajustarPrioridadeAutomatica(chamado);
        
        chamado = chamadoRepository.save(chamado);
        
        // Adiciona interação inicial do chatbot
        Interacao interacaoChatbot = new Interacao();
        interacaoChatbot.setMensagem(assistenteService.processarMensagem("Olá! Como posso ajudar?", chamado));
        interacaoChatbot.setUsuario(solicitante);
        interacaoChatbot.setChamado(chamado);
        interacaoChatbot.setTipo(Interacao.TipoInteracao.RESPOSTA_CHATBOT);
        interacaoChatbot.setDataHora(LocalDateTime.now());
        
        interacaoRepository.save(interacaoChatbot);
        
        return new ChamadoDTO(chamado);
    }
    
    public ChamadoDTO atribuirTecnico(Long chamadoId, Usuario tecnico) {
        Chamado chamado = buscarChamado(chamadoId);
        chamado.setTecnico(tecnico);
        chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
        
        registrarInteracao(chamado, "Chamado atribuído ao técnico " + tecnico.getNome(), 
            tecnico, Interacao.TipoInteracao.MUDANCA_STATUS);
            
        return new ChamadoDTO(chamadoRepository.save(chamado));
    }
    
    public ChamadoDTO adicionarInteracao(Long chamadoId, String mensagem, Usuario usuario) {
        Chamado chamado = buscarChamado(chamadoId);
        
        // Garante que a categoria está definida
        if (chamado.getCategoria() == null || chamado.getCategoria().trim().isEmpty()) {
            chamado.setCategoria(Chamado.CategoriaChamado.OUTROS.getDescricao());
            chamadoRepository.save(chamado);
        }
        
        // Adiciona a interação do usuário
        Interacao interacao = new Interacao();
        interacao.setMensagem(mensagem);
        interacao.setUsuario(usuario);
        interacao.setChamado(chamado);
        interacao.setDataHora(LocalDateTime.now());
        interacao.setTipo(usuario.getTipo() == Usuario.TipoUsuario.TECNICO ? 
            Interacao.TipoInteracao.RESPOSTA_TECNICO : 
            Interacao.TipoInteracao.COMENTARIO_CLIENTE);
            
        interacaoRepository.save(interacao);

        // Se a interação for do solicitante e não houver técnico atribuído, processa com o assistente virtual
        if (usuario.getTipo() != Usuario.TipoUsuario.TECNICO && chamado.getTecnico() == null) {
            // Retorna imediatamente o DTO do chamado, sem a resposta do assistente
            ChamadoDTO dto = new ChamadoDTO(chamado);
            
            // Processa a resposta do assistente em background após o delay
            CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute(() -> {
                try {
                    // Processa a mensagem do assistente
                    String respostaAssistente = assistenteService.processarMensagem(mensagem, chamado);
                    
                    // Adiciona a resposta do assistente como uma nova interação
                    Interacao respostaBot = new Interacao();
                    respostaBot.setMensagem(respostaAssistente);
                    respostaBot.setUsuario(usuario);
                    respostaBot.setChamado(chamado);
                    respostaBot.setDataHora(LocalDateTime.now());
                    respostaBot.setTipo(Interacao.TipoInteracao.RESPOSTA_CHATBOT);
                    interacaoRepository.save(respostaBot);

                    // Se o assistente não conseguir resolver, encaminha para técnicos
                    if (assistenteService.precisaEncaminharParaTecnico(respostaAssistente)) {
                        // Adiciona um pequeno delay antes de enviar a mensagem de encaminhamento
                        CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS).execute(() -> {
                            try {
                                // Atualiza o status do chamado
                                chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
                                chamadoRepository.save(chamado);
                                
                                // Notifica os técnicos disponíveis
                                List<Usuario> tecnicos = usuarioRepository.findByTipo(Usuario.TipoUsuario.TECNICO);
                                for (Usuario tecnico : tecnicos) {
                                    notificacaoService.notificarNovoChamado(tecnico, chamado);
                                }
                                
                                // Adiciona mensagem de encaminhamento
                                Interacao encaminhamento = new Interacao();
                                encaminhamento.setMensagem("Chamado encaminhado para análise técnica. Em breve um técnico irá atendê-lo.");
                                encaminhamento.setUsuario(usuario);
                                encaminhamento.setChamado(chamado);
                                encaminhamento.setDataHora(LocalDateTime.now());
                                encaminhamento.setTipo(Interacao.TipoInteracao.RESPOSTA_CHATBOT);
                                interacaoRepository.save(encaminhamento);
                            } catch (Exception e) {
                                logger.error("Erro ao processar encaminhamento: " + e.getMessage(), e);
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("Erro ao processar resposta do assistente: " + e.getMessage(), e);
                }
            });
            
            return dto;
        }
        
        return new ChamadoDTO(chamado);
    }
    
    public ChamadoDTO fecharChamado(Long chamadoId, String resolucao, Usuario tecnico) {
        Chamado chamado = buscarChamado(chamadoId);
        chamado.setStatus(Chamado.StatusChamado.FECHADO);
        chamado.setDataFechamento(LocalDateTime.now());
        chamado.setTempoResolucao(java.time.Duration.between(chamado.getDataAbertura(), chamado.getDataFechamento()).toHours());
        
        registrarInteracao(chamado, "Chamado fechado: " + resolucao, 
            tecnico, Interacao.TipoInteracao.RESPOSTA_TECNICO);
            
        return new ChamadoDTO(chamadoRepository.save(chamado));
    }
    
    public List<ChamadoDTO> listarChamadosPorSolicitante(Usuario solicitante) {
        return chamadoRepository.findBySolicitante(solicitante).stream()
                .map(ChamadoDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<ChamadoDTO> listarChamadosPorTecnico(Usuario tecnico) {
        return chamadoRepository.findByTecnico(tecnico).stream()
                .map(ChamadoDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<ChamadoDTO> listarChamadosPorTecnicoEStatus(Usuario tecnico, Chamado.StatusChamado status) {
        return chamadoRepository.findByTecnicoEStatus(tecnico, status).stream()
                .map(ChamadoDTO::new)
                .collect(Collectors.toList());
    }
    
    public Map<String, Long> gerarEstatisticasPorCategoria() {
        return chamadoRepository.contarChamadosPorCategoria().stream()
                .collect(Collectors.toMap(
                    arr -> (String) arr[0],
                    arr -> (Long) arr[1]
                ));
    }
    
    public List<ChamadoDTO> listarChamados() {
        return chamadoRepository.findAll().stream()
                .map(ChamadoDTO::new)
                .collect(Collectors.toList());
    }
    
    private void registrarInteracao(Chamado chamado, String mensagem, Usuario usuario, 
            Interacao.TipoInteracao tipo) {
        Interacao interacao = new Interacao();
        interacao.setMensagem(mensagem);
        interacao.setUsuario(usuario);
        interacao.setChamado(chamado);
        interacao.setTipo(tipo);
        interacao.setDataHora(LocalDateTime.now());
        interacaoRepository.save(interacao);
    }
    
    public Double calcularTempoMedioResolucaoPorData(Usuario tecnico, LocalDateTime data) {
        LocalDateTime inicio = data.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime fim = data.withHour(23).withMinute(59).withSecond(59);
        
        return chamadoRepository.findByTecnicoEStatus(tecnico, Chamado.StatusChamado.FECHADO).stream()
                .filter(c -> c.getDataFechamento() != null && 
                           c.getDataFechamento().isAfter(inicio) && 
                           c.getDataFechamento().isBefore(fim))
                .mapToLong(Chamado::getTempoResolucao)
                .average()
                .orElse(0.0);
    }
    
    public void avaliarChamado(Long chamadoId, Integer avaliacao) {
        if (avaliacao < 1 || avaliacao > 5) {
            throw new RuntimeException("Avaliação deve ser entre 1 e 5");
        }
        
        Chamado chamado = buscarChamado(chamadoId);
        if (chamado.getStatus() != Chamado.StatusChamado.FECHADO) {
            throw new RuntimeException("Apenas chamados fechados podem ser avaliados");
        }
        
        chamado.setAvaliacao(avaliacao);
        chamadoRepository.save(chamado);
        
        // Invalida o cache para forçar uma nova leitura do banco
        cacheManager.getChamadoCache().invalidate(chamadoId);
        
        if (chamado.getTecnico() != null) {
            notificacaoService.notificarAvaliacao(chamado.getTecnico(), chamado.getId(), avaliacao);
        }
    }
    
    public List<Chamado> listarChamadosPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.findByPeriodo(inicio, fim);
    }
    
    public void adicionarInteracao(Chamado chamado, String mensagem, Usuario usuario, Interacao.TipoInteracao tipo) {
        Interacao interacao = new Interacao();
        interacao.setMensagem(mensagem);
        interacao.setUsuario(usuario);
        interacao.setChamado(chamado);
        interacao.setDataHora(LocalDateTime.now());
        interacao.setTipo(tipo);
        
        interacaoRepository.save(interacao);
    }
    
    public List<Interacao> listarInteracoes(Chamado chamado) {
        return interacaoRepository.findByChamado(chamado);
    }
    
    public ChamadoDTO atualizarStatus(Long chamadoId, Chamado.StatusChamado novoStatus, Usuario tecnico) {
        // Verifica se é um técnico
        if (tecnico.getTipo() != Usuario.TipoUsuario.TECNICO) {
            throw new RuntimeException("Apenas técnicos podem alterar o status do chamado");
        }
        
        Chamado chamado = buscarChamado(chamadoId);
        
        // Verifica se o status é válido
        if (novoStatus == null) {
            throw new RuntimeException("O novo status não pode ser nulo");
        }
        
        // Atualiza o status
        chamado.setStatus(novoStatus);
        
        // Se estiver fechando o chamado, atualiza a data de fechamento e tempo de resolução
        if (novoStatus == Chamado.StatusChamado.FECHADO) {
            chamado.setDataFechamento(LocalDateTime.now());
            chamado.setTempoResolucao(java.time.Duration.between(chamado.getDataAbertura(), chamado.getDataFechamento()).toHours());
        }
        
        // Registra a interação
        String mensagem = "Status alterado para: " + novoStatus.name();
        registrarInteracao(chamado, mensagem, tecnico, Interacao.TipoInteracao.MUDANCA_STATUS);
        
        // Salva e retorna o chamado atualizado
        return new ChamadoDTO(chamadoRepository.save(chamado));
    }
    
    public void atribuirTecnico(Chamado chamado, Usuario tecnico) {
        chamado.setTecnico(tecnico);
        chamado.setStatus(Chamado.StatusChamado.EM_ANDAMENTO);
        chamadoRepository.save(chamado);
    }
    
    public void avaliarAtendimento(Chamado chamado, int avaliacao) {
        chamado.setAvaliacao(avaliacao);
        chamadoRepository.save(chamado);
    }
    
    public List<Interacao> listarInteracoes(Long chamadoId) {
        return interacaoRepository.findByChamadoId(chamadoId);
    }
    
    public Chamado criarChamado(Chamado chamado) {
        // Garante que a categoria está definida
        if (chamado.getCategoria() == null || chamado.getCategoria().trim().isEmpty()) {
            chamado.setCategoria(Chamado.CategoriaChamado.OUTROS.getDescricao());
        }
        
        // Garante que a data de abertura está definida
        if (chamado.getDataAbertura() == null) {
            chamado.setDataAbertura(LocalDateTime.now());
        }
        
        // Garante que o status está definido
        if (chamado.getStatus() == null) {
            chamado.setStatus(Chamado.StatusChamado.ABERTO);
        }
        
        // Garante que a prioridade está definida
        if (chamado.getPrioridade() == null) {
            chamado.setPrioridade(Chamado.PrioridadeChamado.MEDIA);
        }
        
        return chamadoRepository.save(chamado);
    }
    
    public Chamado buscarChamado(Long id) {
        return cacheManager.getChamadoCache().get(id, key -> {
            return chamadoRepository.findById(id).orElse(null);
        });
    }

    private void ajustarPrioridadeAutomatica(Chamado chamado) {
        String categoria = chamado.getCategoriaIa();
        String descricao = chamado.getDescricao().toLowerCase();
        
        // Define prioridade alta para categorias críticas
        if (categoria.equals("REDE") || categoria.equals("HARDWARE")) {
            chamado.setPrioridade(Chamado.PrioridadeChamado.ALTA);
        }
        // Define prioridade média para outras categorias, exceto se houver palavras-chave de urgência
        else {
            // Palavras que indicam alta prioridade
            if (descricao.contains("urgente") || 
                descricao.contains("crítico") || 
                descricao.contains("emergência") ||
                descricao.contains("parado") ||
                descricao.contains("não funciona")) {
                chamado.setPrioridade(Chamado.PrioridadeChamado.ALTA);
            }
            // Palavras que indicam prioridade crítica
            else if (descricao.contains("produção") ||
                     descricao.contains("sistema fora") ||
                     descricao.contains("servidor down")) {
                chamado.setPrioridade(Chamado.PrioridadeChamado.CRITICA);
            }
            // Palavras que indicam baixa prioridade
            else if (descricao.contains("quando puder") ||
                     descricao.contains("não urgente") ||
                     descricao.contains("baixa prioridade")) {
                chamado.setPrioridade(Chamado.PrioridadeChamado.BAIXA);
            }
            // Prioridade padrão para outras categorias
            else {
                chamado.setPrioridade(Chamado.PrioridadeChamado.MEDIA);
            }
        }
    }
    
    public ChamadoDTO atualizarChamado(ChamadoDTO chamadoDTO) {
        Chamado chamado = buscarChamado(chamadoDTO.getId());
        chamado.setStatus(chamadoDTO.getStatus());
        chamado.setPrioridade(chamadoDTO.getPrioridade());
        
        // Se estiver fechando o chamado, atualiza a data de fechamento e tempo de resolução
        if (chamadoDTO.getStatus() == Chamado.StatusChamado.FECHADO && chamado.getDataFechamento() == null) {
            chamado.setDataFechamento(LocalDateTime.now());
            chamado.setTempoResolucao(java.time.Duration.between(chamado.getDataAbertura(), chamado.getDataFechamento()).toHours());
        }
        
        return new ChamadoDTO(chamadoRepository.save(chamado));
    }
    
    public void excluirChamado(Long chamadoId) {
        // Primeiro exclui todas as interações do chamado
        List<Interacao> interacoes = interacaoRepository.findByChamadoId(chamadoId);
        for (Interacao interacao : interacoes) {
            interacaoRepository.delete(interacao.getId());
        }
        
        // Depois exclui o chamado
        chamadoRepository.delete(chamadoId);
        cacheManager.getChamadoCache().invalidate(chamadoId);
    }
    
    public void limparCache() {
        cacheManager.getChamadoCache().invalidateAll();
    }
    
    public int countByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return chamadoRepository.countByPeriodo(inicio, fim);
    }
    
    public void limparTodosChamados() {
        logger.info("Iniciando processo de limpeza de todos os chamados");
        try {
            // Primeiro limpa todas as interações
            interacaoRepository.deleteAll();
            logger.info("Todas as interações foram removidas");
            
            // Depois limpa todos os chamados
            chamadoRepository.deleteAll();
            logger.info("Todos os chamados foram removidos");
            
            // Limpa o cache
            limparCache();
            logger.info("Cache limpo com sucesso");
        } catch (Exception e) {
            logger.error("Erro ao limpar chamados: " + e.getMessage(), e);
            throw new RuntimeException("Erro ao limpar chamados", e);
        }
    }
} 