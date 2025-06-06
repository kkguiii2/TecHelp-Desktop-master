package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;

public class LoginController extends BaseController {
    
    private final AuthService authService;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField senhaField;
    
    public LoginController() {
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando LoginController");
        // Garante que os campos estão inicializados
        if (emailField == null || senhaField == null) {
            throw new RuntimeException("Campos do formulário não foram injetados corretamente");
        }
    }
    
    @FXML
    private void handleLogin() {
        System.out.println("Tentando realizar login");
        String email = emailField.getText();
        String senha = senhaField.getText();
        
        if (email.isEmpty() || senha.isEmpty()) {
            mostrarErro("Por favor, preencha todos os campos");
            return;
        }
        
        try {
            Usuario usuario = authService.autenticar(email, senha);
            if (usuario != null) {
                String fxmlPath = switch (usuario.getTipo()) {
                    case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                    case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                    case ADMIN -> "/fxml/AdminDashboardView.fxml";
                };
                
                System.out.println("Redirecionando para: " + fxmlPath);
                carregarTela(fxmlPath);
            } else {
                mostrarErro("Credenciais inválidas");
            }
        } catch (Exception e) {
            System.err.println("Erro no login: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao realizar login: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCadastro() {
        try {
            System.out.println("Abrindo tela de cadastro");
            carregarTela("/fxml/CadastroView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir cadastro: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir tela de cadastro: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePoliticaPrivacidade() {
        try {
            System.out.println("Abrindo política de privacidade");
            URL fxmlUrl = getClass().getClassLoader().getResource("fxml/PoliticaPrivacidadeView.fxml");
            if (fxmlUrl == null) {
                throw new RuntimeException("Arquivo FXML da política de privacidade não encontrado");
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getClassLoader().getResource("css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            Stage stage = new Stage();
            stage.setTitle("Política de Privacidade");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erro ao abrir política: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir política de privacidade: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            carregarTela("/fxml/LoginView.fxml");
        } catch (Exception e) {
            mostrarErro("Erro ao realizar logout");
        }
    }
} 