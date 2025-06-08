package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.AuthService;
import com.techelp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.application.Platform;

public class PerfilController extends BaseController {
    
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private Usuario usuarioLogado;
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField senhaAtualField;
    
    @FXML
    private PasswordField novaSenhaField;
    
    @FXML
    private PasswordField confirmaSenhaField;
    
    @FXML
    private Label tipoLabel;
    
    @FXML
    private Label mensagemLabel;
    
    public PerfilController() {
        this.authService = AuthService.getInstance();
        this.usuarioService = new UsuarioService();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando PerfilController");
            
            usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            carregarDados();
            inicializarNotificacoes(usuarioLogado);
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar perfil: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar perfil: " + e.getMessage());
        }
    }
    
    private void carregarDados() {
        nomeField.setText(usuarioLogado.getNome());
        emailField.setText(usuarioLogado.getEmail());
        tipoLabel.setText("Tipo: " + usuarioLogado.getTipo().name());
    }
    
    @FXML
    private void handleSalvar() {
        try {
            // Validação dos campos
            if (nomeField.getText().isEmpty() || emailField.getText().isEmpty()) {
                mensagemLabel.setText("Preencha todos os campos obrigatórios");
                return;
            }
            
            // Atualiza os dados do usuário
            usuarioLogado.setNome(nomeField.getText());
            usuarioLogado.setEmail(emailField.getText());
            
            // Se foi informada nova senha
            if (!novaSenhaField.getText().isEmpty()) {
                if (!authService.validarSenha(senhaAtualField.getText(), usuarioLogado.getSenha())) {
                    mostrarErro("Senha atual incorreta");
                    return;
                }
                
                if (!novaSenhaField.getText().equals(confirmaSenhaField.getText())) {
                    mostrarErro("Nova senha e confirmação não coincidem");
                    return;
                }
                
                usuarioLogado.setSenha(novaSenhaField.getText());
            }
            
            usuarioService.atualizar(usuarioLogado);
            mensagemLabel.setText("Perfil atualizado com sucesso!");
            
            // Limpa os campos de senha
            senhaAtualField.clear();
            novaSenhaField.clear();
            confirmaSenhaField.clear();
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar perfil: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao salvar perfil: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoltar() {
        Platform.runLater(() -> {
            try {
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
        });
    }
} 