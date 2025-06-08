package com.techelp.controller;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import com.techelp.service.AssistenteService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import java.time.LocalDateTime;
import org.json.JSONObject;

public class NovoChamadoController extends BaseController {
    
    private final ChamadoService chamadoService;
    private final AuthService authService;
    private final AssistenteService assistenteService;
    private Usuario usuarioLogado;
    
    @FXML
    private TextField tituloField;
    
    @FXML
    private TextArea descricaoArea;
    
    @FXML
    private Text mensagemErro;
    
    public NovoChamadoController() {
        this.chamadoService = new ChamadoService();
        this.authService = AuthService.getInstance();
        this.assistenteService = new AssistenteService();
    }
    
    @FXML
    public void initialize() {
        try {
            usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null) {
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
        } catch (Exception e) {
            mostrarErro("Erro ao inicializar tela: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCriarChamado() {
        try {
            // Validação dos campos
            if (tituloField.getText().isEmpty() || descricaoArea.getText().isEmpty()) {
                mostrarErro("Por favor, preencha todos os campos");
                return;
            }
            
            // Cria o objeto chamado com informações básicas
            Chamado novoChamado = new Chamado();
            novoChamado.setTitulo(tituloField.getText());
            novoChamado.setDescricao(descricaoArea.getText());
            novoChamado.setSolicitante(usuarioLogado);
            novoChamado.setStatus(Chamado.StatusChamado.ABERTO);
            novoChamado.setDataAbertura(LocalDateTime.now());
            
            // Usa a IA para classificar o chamado
            String prompt = String.format("""
                Analise o chamado técnico abaixo e classifique sua categoria e prioridade.
                
                Título: %s
                Descrição: %s
                
                Responda apenas em formato JSON com os campos:
                - categoria: HARDWARE, SOFTWARE, REDE, ACESSO, EMAIL ou IMPRESSORA
                - prioridade: BAIXA, MEDIA ou ALTA
                - justificativa: breve explicação da classificação
                
                Considere:
                - Prioridade ALTA: problemas que impedem o trabalho ou afetam muitos usuários
                - Prioridade MEDIA: problemas que dificultam mas não impedem o trabalho
                - Prioridade BAIXA: problemas que causam pequenos inconvenientes
                """,
                novoChamado.getTitulo(),
                novoChamado.getDescricao()
            );
            
            String resposta = assistenteService.processarMensagem(prompt, novoChamado);
            JSONObject classificacao = new JSONObject(resposta);
            
            // Define categoria e prioridade com base na análise da IA
            novoChamado.setCategoria(classificacao.getString("categoria"));
            novoChamado.setPrioridade(Chamado.PrioridadeChamado.valueOf(classificacao.getString("prioridade")));
            
            // Salva o chamado
            chamadoService.criarChamado(novoChamado);
            
            mostrarSucesso("Chamado criado com sucesso!\n\nCategoria: " + novoChamado.getCategoria() + 
                          "\nPrioridade: " + novoChamado.getPrioridade() +
                          "\n\nJustificativa: " + classificacao.getString("justificativa"));
            
            // Volta para a tela anterior
            String telaRetorno = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };
            
            carregarTela(telaRetorno);
            
        } catch (Exception e) {
            mostrarErro("Erro ao criar chamado: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoltar() {
        try {
            String telaRetorno = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };
            
            carregarTela(telaRetorno);
        } catch (Exception e) {
            mostrarErro("Erro ao voltar: " + e.getMessage());
        }
    }
} 