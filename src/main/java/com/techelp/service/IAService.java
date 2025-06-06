package com.techelp.service;

import com.techelp.model.entity.Chamado;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.ArrayList;

public class IAService {
    private static final Map<String, List<String>> PALAVRAS_CHAVE = new HashMap<>();
    private static final Map<String, List<String>> SOLUCOES_AUTOMATICAS = new HashMap<>();
    
    private static final List<String> PALAVRAS_CHAVE_ENCAMINHAMENTO = Arrays.asList(
        "não sei", "não consigo", "não funciona", "erro", "problema",
        "ajuda", "suporte", "técnico", "especialista", "complexo"
    );
    
    static {
        PALAVRAS_CHAVE.put("INFRAESTRUTURA_REDE", Arrays.asList(
            "internet", "rede", "wifi", "conexão", "cabo", "roteador", "switch",
            "servidor", "dns", "ip", "ping", "lento", "lenta"
        ));
        
        PALAVRAS_CHAVE.put("HARDWARE", Arrays.asList(
            "computador", "notebook", "impressora", "mouse", "teclado", "monitor",
            "memória", "hd", "ssd", "processador", "cpu", "fonte", "bateria"
        ));
        
        PALAVRAS_CHAVE.put("SOFTWARE", Arrays.asList(
            "programa", "sistema", "windows", "office", "excel", "word", "outlook",
            "antivírus", "atualização", "licença", "erro", "travando", "bug"
        ));
        
        PALAVRAS_CHAVE.put("SERVICOS", Arrays.asList(
            "email", "backup", "restore", "acesso", "senha", "permissão", "conta",
            "login", "usuário", "bloqueado", "expirado", "cadastro"
        ));

        // Inicializa soluções automáticas para cada categoria
        SOLUCOES_AUTOMATICAS.put("INFRAESTRUTURA_REDE", Arrays.asList(
            "Vamos tentar alguns passos para resolver seu problema de rede:\n" +
            "1. Desligue seu roteador por 30 segundos e ligue novamente\n" +
            "2. Verifique se o cabo de rede está bem conectado\n" +
            "3. Tente conectar outro dispositivo para verificar se o problema persiste\n" +
            "O problema foi resolvido com esses passos?",

            "Podemos tentar verificar sua conexão:\n" +
            "1. Abra o prompt de comando (cmd)\n" +
            "2. Digite 'ping google.com'\n" +
            "3. Me diga se aparece resposta ou timeout",

            "Vamos tentar redefinir suas configurações de rede:\n" +
            "1. Abra o prompt de comando como administrador\n" +
            "2. Digite 'ipconfig /release'\n" +
            "3. Depois digite 'ipconfig /renew'\n" +
            "4. Por fim, digite 'ipconfig /flushdns'\n" +
            "O problema persiste?"
        ));

        SOLUCOES_AUTOMATICAS.put("HARDWARE", Arrays.asList(
            "Vamos verificar seu hardware:\n" +
            "1. Verifique se todos os cabos estão bem conectados\n" +
            "2. Tente reiniciar o equipamento\n" +
            "3. Se for notebook, tente remover e recolocar a bateria\n" +
            "Alguma dessas ações resolveu?",

            "Podemos tentar identificar problemas de hardware:\n" +
            "1. Pressione Win + X\n" +
            "2. Selecione 'Gerenciador de Dispositivos'\n" +
            "3. Há algum item com símbolo de exclamação amarela?",

            "Vamos verificar a temperatura do seu equipamento:\n" +
            "1. Verifique se as saídas de ar estão limpas\n" +
            "2. O equipamento está muito quente?\n" +
            "3. Os ventiladores estão funcionando?"
        ));

        SOLUCOES_AUTOMATICAS.put("SOFTWARE", Arrays.asList(
            "Vamos tentar resolver o problema do software:\n" +
            "1. Feche completamente o programa\n" +
            "2. Pressione Ctrl + Shift + Esc\n" +
            "3. Verifique se há alguma instância do programa ainda rodando\n" +
            "4. Se houver, finalize pelo Gerenciador de Tarefas\n" +
            "5. Abra o programa novamente\n" +
            "O problema foi resolvido?",

            "Podemos tentar reparar o programa:\n" +
            "1. Abra 'Configurações'\n" +
            "2. Vá em 'Aplicativos'\n" +
            "3. Localize o programa com problema\n" +
            "4. Clique em 'Modificar' e depois em 'Reparar'\n" +
            "Isso ajudou?",

            "Vamos verificar por atualizações:\n" +
            "1. Verifique se há atualizações pendentes do Windows\n" +
            "2. Procure por atualizações do programa em questão\n" +
            "3. Instale todas as atualizações disponíveis\n" +
            "O problema continua?"
        ));

        SOLUCOES_AUTOMATICAS.put("SERVICOS", Arrays.asList(
            "Vamos verificar seu acesso:\n" +
            "1. Confirme se está usando o email correto\n" +
            "2. Verifique se a tecla Caps Lock está desligada\n" +
            "3. Tente redefinir sua senha\n" +
            "Conseguiu acessar?",

            "Podemos tentar limpar os dados de navegação:\n" +
            "1. Pressione Ctrl + Shift + Delete\n" +
            "2. Selecione 'Cookies e dados de sites'\n" +
            "3. Clique em 'Limpar dados'\n" +
            "4. Tente acessar novamente\n" +
            "O problema foi resolvido?",

            "Vamos verificar seu navegador:\n" +
            "1. Tente acessar em uma guia anônima/privativa\n" +
            "2. Se funcionar, o problema pode ser com extensões\n" +
            "3. Desative todas as extensões e teste novamente\n" +
            "Funcionou em algum desses casos?"
        ));
    }

    private Map<Long, Integer> tentativasPorChamado = new HashMap<>();
    private Map<Long, List<String>> solucoesUtilizadas = new HashMap<>();

    public String processarMensagem(String mensagem, Chamado chamado) {
        final String mensagemLower = mensagem.toLowerCase();
        
        // Verifica se é uma saudação
        if (isSaudacao(mensagemLower)) {
            return "Olá! Sou o assistente virtual do TecHelp. Como posso ajudar você hoje?";
        }
        
        // Verifica se é um agradecimento
        if (isAgradecimento(mensagemLower)) {
            return "Por nada! Estou aqui para ajudar. Há mais alguma coisa que você precisa?";
        }

        // Verifica se a mensagem indica que o problema foi resolvido
        if (isProblemaResolvido(mensagemLower)) {
            return "Ótimo! Fico feliz em ter ajudado. Se precisar de mais alguma coisa, estou à disposição!";
        }

        // Se ainda não tentamos nenhuma solução para este chamado
        if (!tentativasPorChamado.containsKey(chamado.getId())) {
            tentativasPorChamado.put(chamado.getId(), 0);
            solucoesUtilizadas.put(chamado.getId(), new ArrayList<>());
        }

        String categoria = chamado.getCategoriaIa();
        List<String> solucoesPossiveis = SOLUCOES_AUTOMATICAS.get(categoria);
        
        if (solucoesPossiveis != null) {
            int tentativas = tentativasPorChamado.get(chamado.getId());
            List<String> solucoesJaUtilizadas = solucoesUtilizadas.get(chamado.getId());

            // Se ainda há soluções não tentadas e não excedemos o limite de tentativas
            if (tentativas < solucoesPossiveis.size() && tentativas < 3) {
                String proximaSolucao = solucoesPossiveis.get(tentativas);
                
                // Verifica se essa solução já foi utilizada
                if (!solucoesJaUtilizadas.contains(proximaSolucao)) {
                    tentativasPorChamado.put(chamado.getId(), tentativas + 1);
                    solucoesJaUtilizadas.add(proximaSolucao);
                    return proximaSolucao;
                }
            }
        }

        // Se chegamos aqui, significa que já tentamos todas as soluções automáticas
        // ou excedemos o número de tentativas, então vamos encaminhar para um técnico
        return "Já tentamos algumas soluções, mas parece que seu problema requer uma análise mais detalhada. " +
               "Vou encaminhar seu chamado para um de nossos técnicos especializados que poderá ajudar melhor com essa questão.";
    }
    
    private boolean isProblemaResolvido(String mensagem) {
        return Pattern.compile("\\b(resolveu|resolvido|funcionou|ok|sim|deu certo|consegui)\\b")
                .matcher(mensagem)
                .find();
    }

    public boolean precisaEncaminharParaTecnico(String mensagem) {
        final String mensagemLower = mensagem.toLowerCase();
        
        // Verifica se contém palavras-chave que indicam necessidade de suporte técnico
        return PALAVRAS_CHAVE_ENCAMINHAMENTO.stream()
                .anyMatch(palavra -> mensagemLower.contains(palavra));
    }
    
    private boolean isSaudacao(String mensagem) {
        return Pattern.compile("\\b(oi|olá|ola|bom dia|boa tarde|boa noite|hi|hello)\\b")
                .matcher(mensagem)
                .find();
    }
    
    private boolean isAgradecimento(String mensagem) {
        return Pattern.compile("\\b(obrigado|obrigada|valeu|thanks|thank you|grato|grata)\\b")
                .matcher(mensagem)
                .find();
    }

    public String classificarChamado(Chamado chamado) {
        String textoCompleto = (chamado.getTitulo() + " " + chamado.getDescricao()).toLowerCase();
        Map<String, Integer> pontuacao = new HashMap<>();
        
        for (Map.Entry<String, List<String>> categoria : PALAVRAS_CHAVE.entrySet()) {
            int pontos = categoria.getValue().stream()
                .mapToInt(palavra -> textoCompleto.contains(palavra) ? 1 : 0)
                .sum();
            
            pontuacao.put(categoria.getKey(), pontos);
        }
        
        return pontuacao.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("OUTROS");
    }

    public String obterRespostaAutomatica(String categoria) {
        List<String> solucoes = SOLUCOES_AUTOMATICAS.get(categoria);
        if (solucoes != null && !solucoes.isEmpty()) {
            return solucoes.get(0); // Retorna a primeira solução da categoria
        }
        return "Recebemos seu chamado e nossa equipe irá avaliar o mais breve possível.";
    }
} 