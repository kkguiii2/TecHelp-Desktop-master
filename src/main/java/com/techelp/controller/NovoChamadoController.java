package com.techelp.controller;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import java.time.LocalDateTime;

public class NovoChamadoController extends BaseController {
    
    private final ChamadoService chamadoService;
    private final AuthService authService;
    private Usuario usuarioLogado;
    
    @FXML
    private TextField tituloField;
    
    @FXML
    private TextArea descricaoArea;
    
    @FXML
    private ComboBox<Chamado.PrioridadeChamado> prioridadeCombo;
    
    @FXML
    private ComboBox<Chamado.CategoriaChamado> categoriaCombo;
    
    public NovoChamadoController() {
        this.chamadoService = new ChamadoService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando NovoChamadoController");
            
            usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            // Configura o ComboBox de prioridade
            prioridadeCombo.setItems(FXCollections.observableArrayList(Chamado.PrioridadeChamado.values()));
            prioridadeCombo.setValue(Chamado.PrioridadeChamado.MEDIA);
            
            // Configura o ComboBox de categoria
            categoriaCombo.setItems(FXCollections.observableArrayList(Chamado.CategoriaChamado.values()));
            categoriaCombo.setValue(Chamado.CategoriaChamado.OUTROS);
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar tela: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar tela: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCriarChamado() {
        try {
            System.out.println("Tentando criar chamado");
            
            // Validação dos campos
            if (tituloField.getText().isEmpty() || descricaoArea.getText().isEmpty() || 
                categoriaCombo.getValue() == null || prioridadeCombo.getValue() == null) {
                mostrarErro("Por favor, preencha todos os campos");
                return;
            }
            
            // Cria o objeto chamado
            Chamado novoChamado = new Chamado();
            novoChamado.setTitulo(tituloField.getText());
            novoChamado.setDescricao(descricaoArea.getText());
            novoChamado.setPrioridade(prioridadeCombo.getValue());
            novoChamado.setCategoria(categoriaCombo.getValue().toString());
            novoChamado.setSolicitante(usuarioLogado);
            novoChamado.setStatus(Chamado.StatusChamado.ABERTO);
            novoChamado.setDataAbertura(LocalDateTime.now());
            
            // Tenta criar o chamado
            chamadoService.criarChamado(novoChamado);
            
            mostrarSucesso("Chamado criado com sucesso!");
            
            // Volta para a tela anterior
            String telaRetorno = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };
            
            carregarTela(telaRetorno);
            
        } catch (Exception e) {
            System.err.println("Erro ao criar chamado: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao criar chamado: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoltar() {
        try {
            System.out.println("Voltando para tela anterior");
            
            String telaRetorno = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };
            
            carregarTela(telaRetorno);
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao voltar: " + e.getMessage());
        }
    }
} 