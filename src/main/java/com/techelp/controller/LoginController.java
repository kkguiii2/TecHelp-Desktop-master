package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import java.net.URL;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.util.prefs.Preferences;

public class LoginController extends BaseController {
    
    private final AuthService authService;
    private static final String PREF_EMAIL = "email";
    private static final String PREF_SENHA = "senha";
    private static final String PREF_LEMBRAR = "lembrar";
    private final Preferences prefs;
    private boolean senhaVisivel = false;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField senhaField;
    
    @FXML
    private TextField senhaVisivelField;
    
    @FXML
    private Button loginButton;
    
    @FXML
    private ProgressIndicator loginSpinner;
    
    @FXML
    private CheckBox lembrarCheckbox;
    
    @FXML
    private Button toggleSenhaButton;
    
    public LoginController() {
        this.authService = AuthService.getInstance();
        this.prefs = Preferences.userNodeForPackage(LoginController.class);
    }
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando LoginController");
        // Garante que os campos estão inicializados
        if (emailField == null || senhaField == null || loginButton == null || 
            loginSpinner == null || lembrarCheckbox == null || senhaVisivelField == null ||
            toggleSenhaButton == null) {
            throw new RuntimeException("Campos do formulário não foram injetados corretamente");
        }
        
        // Configura os listeners para sincronizar os campos de senha
        senhaField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!senhaVisivel) {
                senhaVisivelField.setText(newValue);
            }
        });
        
        senhaVisivelField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (senhaVisivel) {
                senhaField.setText(newValue);
            }
        });
        
        // Carrega as credenciais salvas, se existirem
        carregarCredenciaisSalvas();
    }
    
    @FXML
    private void handleToggleSenha() {
        senhaVisivel = !senhaVisivel;
        
        if (senhaVisivel) {
            // Mostra a senha
            senhaVisivelField.setText(senhaField.getText());
            senhaField.setVisible(false);
            senhaField.setManaged(false);
            senhaVisivelField.setVisible(true);
            senhaVisivelField.setManaged(true);
            toggleSenhaButton.setText("Ocultar");
        } else {
            // Oculta a senha
            senhaField.setText(senhaVisivelField.getText());
            senhaVisivelField.setVisible(false);
            senhaVisivelField.setManaged(false);
            senhaField.setVisible(true);
            senhaField.setManaged(true);
            toggleSenhaButton.setText("Exibir");
        }
    }
    
    private void carregarCredenciaisSalvas() {
        boolean lembrar = prefs.getBoolean(PREF_LEMBRAR, false);
        if (lembrar) {
            String email = prefs.get(PREF_EMAIL, "");
            String senha = prefs.get(PREF_SENHA, "");
            
            if (!email.isEmpty() && !senha.isEmpty()) {
                emailField.setText(email);
                senhaField.setText(senha);
                senhaVisivelField.setText(senha);
                lembrarCheckbox.setSelected(true);
            }
        }
    }
    
    private void salvarCredenciais(String email, String senha) {
        if (lembrarCheckbox.isSelected()) {
            prefs.put(PREF_EMAIL, email);
            prefs.put(PREF_SENHA, senha);
            prefs.putBoolean(PREF_LEMBRAR, true);
        } else {
            // Se a opção "Lembrar-me" estiver desmarcada, remove as credenciais salvas
            prefs.remove(PREF_EMAIL);
            prefs.remove(PREF_SENHA);
            prefs.putBoolean(PREF_LEMBRAR, false);
        }
    }
    
    @FXML
    private void handleLogin() {
        System.out.println("Tentando realizar login");
        String email = emailField.getText();
        String senha = senhaVisivel ? senhaVisivelField.getText() : senhaField.getText();
        
        if (email.isEmpty() || senha.isEmpty()) {
            mostrarErro("Por favor, preencha todos os campos");
            return;
        }
        
        // Desabilita o botão e mostra o spinner
        loginButton.setDisable(true);
        loginSpinner.setVisible(true);
        
        // Cria uma tarefa assíncrona para o login
        Task<Usuario> loginTask = new Task<>() {
            @Override
            protected Usuario call() throws Exception {
                // Simula um delay de 2 segundos
                Thread.sleep(2000);
                return authService.autenticar(email, senha);
            }
        };
        
        // Configura o que fazer quando a tarefa terminar
        loginTask.setOnSucceeded(event -> {
            Usuario usuario = loginTask.getValue();
            if (usuario != null) {
                // Salva as credenciais se a opção "Lembrar-me" estiver marcada
                salvarCredenciais(email, senha);
                
                String fxmlPath = switch (usuario.getTipo()) {
                    case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                    case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                    case ADMIN -> "/fxml/AdminDashboardView.fxml";
                };
                
                System.out.println("Redirecionando para: " + fxmlPath);
                try {
                    carregarTela(fxmlPath);
                } catch (Exception e) {
                    System.err.println("Erro ao redirecionar: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErro("Erro ao redirecionar: " + e.getMessage());
                }
            } else {
                mostrarErro("Credenciais inválidas");
                // Reativa o botão e esconde o spinner
                loginButton.setDisable(false);
                loginSpinner.setVisible(false);
            }
        });
        
        // Configura o que fazer em caso de erro
        loginTask.setOnFailed(event -> {
            System.err.println("Erro no login: " + loginTask.getException().getMessage());
            loginTask.getException().printStackTrace();
            mostrarErro("Erro ao realizar login: " + loginTask.getException().getMessage());
            // Reativa o botão e esconde o spinner
            loginButton.setDisable(false);
            loginSpinner.setVisible(false);
        });
        
        // Inicia a tarefa em uma nova thread
        new Thread(loginTask).start();
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