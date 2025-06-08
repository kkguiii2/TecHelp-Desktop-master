package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.UsuarioService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.collections.FXCollections;
import java.time.LocalDateTime;
import javafx.scene.control.CheckBox;
import javafx.scene.text.Text;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;
import java.net.URL;
import javafx.application.Platform;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;

public class CadastroController extends BaseController {
    
    private final UsuarioService usuarioService;
    private final AuthService authService;
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField senhaField;
    
    @FXML
    private PasswordField confirmarSenhaField;
    
    @FXML
    private ComboBox<String> departamentoCombo;
    
    @FXML
    private CheckBox lgpdCheckbox;
    
    @FXML
    private Text mensagemErro;
    
    @FXML
    private Text emailValidationText;
    
    @FXML
    private VBox senhaCriteriaBox;
    
    @FXML
    private Text confirmarSenhaValidationText;
    
    public CadastroController() {
        this.usuarioService = new UsuarioService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando CadastroController");
        
        // Configura o ComboBox de departamentos
        departamentoCombo.setItems(FXCollections.observableArrayList(
            "TI",
            "RH",
            "Financeiro",
            "Comercial",
            "Suporte",
            "Admin",
            "Outros"
        ));
        departamentoCombo.setValue("Outros");
        
        // Garante que a mensagem de erro esteja invisível inicialmente
        mensagemErro.setVisible(false);
        
        // Configura listeners para limpar mensagem de erro quando o usuário começa a digitar
        nomeField.textProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        emailField.textProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        senhaField.textProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        confirmarSenhaField.textProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        departamentoCombo.valueProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        lgpdCheckbox.selectedProperty().addListener((obs, old, newValue) -> mensagemErro.setVisible(false));
        
        // Validação em tempo real do Email
        emailField.textProperty().addListener((obs, oldVal, val) -> {
            boolean valido = val.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
            if (val.isEmpty()) {
                emailField.setStyle("");
                emailValidationText.setVisible(false);
            } else if (valido) {
                emailField.setStyle("-fx-border-color: #43a047; -fx-border-width: 0 0 2 0;");
                emailValidationText.setVisible(false);
            } else {
                emailField.setStyle("-fx-border-color: #e53935; -fx-border-width: 0 0 2 0;");
                emailValidationText.setText("Email inválido");
                emailValidationText.setVisible(true);
            }
        });

        // Critérios de senha
        senhaCriteriaBox.getChildren().clear();
        Text minLen = new Text("✗ Pelo menos 8 caracteres");
        Text upper = new Text("✗ Uma letra maiúscula");
        Text lower = new Text("✗ Uma letra minúscula");
        Text digit = new Text("✗ Um número");
        Text special = new Text("✗ Um caractere especial");
        for (Text t : new Text[]{minLen, upper, lower, digit, special}) {
            t.setFill(Color.web("#888"));
            t.setStyle("-fx-font-size: 12px;");
        }
        senhaCriteriaBox.getChildren().addAll(minLen, upper, lower, digit, special);

        senhaField.textProperty().addListener((obs, oldVal, val) -> {
            boolean okLen = val.length() >= 8;
            boolean okUpper = val.matches(".*[A-Z].*");
            boolean okLower = val.matches(".*[a-z].*");
            boolean okDigit = val.matches(".*\\d.*");
            boolean okSpecial = val.matches(".*[^A-Za-z0-9].*");
            minLen.setText((okLen ? "✓" : "✗") + " Pelo menos 8 caracteres");
            minLen.setFill(okLen ? Color.web("#43a047") : Color.web("#e53935"));
            upper.setText((okUpper ? "✓" : "✗") + " Uma letra maiúscula");
            upper.setFill(okUpper ? Color.web("#43a047") : Color.web("#e53935"));
            lower.setText((okLower ? "✓" : "✗") + " Uma letra minúscula");
            lower.setFill(okLower ? Color.web("#43a047") : Color.web("#e53935"));
            digit.setText((okDigit ? "✓" : "✗") + " Um número");
            digit.setFill(okDigit ? Color.web("#43a047") : Color.web("#e53935"));
            special.setText((okSpecial ? "✓" : "✗") + " Um caractere especial");
            special.setFill(okSpecial ? Color.web("#43a047") : Color.web("#e53935"));
            if (okLen && okUpper && okLower && okDigit && okSpecial) {
                senhaField.setStyle("-fx-border-color: #43a047; -fx-border-width: 0 0 2 0;");
            } else {
                senhaField.setStyle("-fx-border-color: #e53935; -fx-border-width: 0 0 2 0;");
            }
        });

        // Validação em tempo real do Confirmar Senha
        confirmarSenhaField.textProperty().addListener((obs, oldVal, val) -> {
            String senha = senhaField.getText();
            if (val.isEmpty()) {
                confirmarSenhaField.setStyle("");
                confirmarSenhaValidationText.setVisible(false);
            } else if (val.equals(senha)) {
                confirmarSenhaField.setStyle("-fx-border-color: #43a047; -fx-border-width: 0 0 2 0;");
                confirmarSenhaValidationText.setText("Senhas coincidem");
                confirmarSenhaValidationText.setFill(Color.web("#43a047"));
                confirmarSenhaValidationText.setVisible(true);
            } else {
                confirmarSenhaField.setStyle("-fx-border-color: #e53935; -fx-border-width: 0 0 2 0;");
                confirmarSenhaValidationText.setText("Senhas não coincidem");
                confirmarSenhaValidationText.setFill(Color.web("#e53935"));
                confirmarSenhaValidationText.setVisible(true);
            }
        });
        // Atualizar confirmação ao digitar na senha principal
        senhaField.textProperty().addListener((obs, oldVal, val) -> {
            String confirmar = confirmarSenhaField.getText();
            if (!confirmar.isEmpty()) {
                if (confirmar.equals(val)) {
                    confirmarSenhaField.setStyle("-fx-border-color: #43a047; -fx-border-width: 0 0 2 0;");
                    confirmarSenhaValidationText.setText("Senhas coincidem");
                    confirmarSenhaValidationText.setFill(Color.web("#43a047"));
                    confirmarSenhaValidationText.setVisible(true);
                } else {
                    confirmarSenhaField.setStyle("-fx-border-color: #e53935; -fx-border-width: 0 0 2 0;");
                    confirmarSenhaValidationText.setText("Senhas não coincidem");
                    confirmarSenhaValidationText.setFill(Color.web("#e53935"));
                    confirmarSenhaValidationText.setVisible(true);
                }
            }
        });
    }
    
    @FXML
    private void handleCadastro() {
        try {
            System.out.println("Tentando realizar cadastro");
            
            // Validação dos campos
            if (nomeField.getText().isEmpty() || emailField.getText().isEmpty() || 
                senhaField.getText().isEmpty() || confirmarSenhaField.getText().isEmpty() ||
                departamentoCombo.getValue() == null) {
                mostrarErro("Por favor, preencha todos os campos obrigatórios");
                return;
            }
            
            if (!senhaField.getText().equals(confirmarSenhaField.getText())) {
                mostrarErro("As senhas não coincidem");
                return;
            }

            // Validação de senha forte
            String senha = senhaField.getText();
            boolean okLen = senha.length() >= 8;
            boolean okUpper = senha.matches(".*[A-Z].*");
            boolean okLower = senha.matches(".*[a-z].*");
            boolean okDigit = senha.matches(".*\\d.*");
            boolean okSpecial = senha.matches(".*[^A-Za-z0-9].*");

            if (!(okLen && okUpper && okLower && okDigit && okSpecial)) {
                mostrarErro("A senha não atende a todos os requisitos de segurança");
                return;
            }

            if (!lgpdCheckbox.isSelected()) {
                mostrarErro("É necessário aceitar os termos de uso e política de privacidade");
                return;
            }
            
            // Verifica se o email já existe
            if (authService.emailJaExiste(emailField.getText())) {
                mostrarErro("Este email já está cadastrado");
                return;
            }
            
            // Cria o objeto usuário
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(nomeField.getText());
            novoUsuario.setEmail(emailField.getText());
            novoUsuario.setSenha(authService.hashSenha(senhaField.getText()));
            novoUsuario.setDepartamento(departamentoCombo.getValue());
            
            // Define o tipo de usuário baseado no departamento
            String departamento = departamentoCombo.getValue();
            if ("Admin".equals(departamento)) {
                novoUsuario.setTipo(Usuario.TipoUsuario.ADMIN);
            } else if ("TI".equals(departamento)) {
                novoUsuario.setTipo(Usuario.TipoUsuario.TECNICO);
            } else {
                novoUsuario.setTipo(Usuario.TipoUsuario.SOLICITANTE);
            }
            
            novoUsuario.setDataCriacao(LocalDateTime.now());
            
            // Salva o usuário
            usuarioService.salvar(novoUsuario);
            
            // Mostra mensagem de sucesso
            mostrarSucesso("Cadastro realizado com sucesso!");
            
            // Aguarda um pouco antes de redirecionar
            PauseTransition delay = new PauseTransition(Duration.seconds(1));
            delay.setOnFinished(event -> {
                Platform.runLater(() -> {
                    try {
                        carregarTela("/fxml/LoginView.fxml");
                    } catch (Exception e) {
                        System.err.println("Erro ao carregar tela de login: " + e.getMessage());
                        e.printStackTrace();
                        mostrarErro("Erro ao carregar tela de login: " + e.getMessage());
                    }
                });
            });
            delay.play();
            
        } catch (Exception e) {
            System.err.println("Erro no cadastro: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao realizar cadastro: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoltar() {
        Platform.runLater(() -> {
            try {
                carregarTela("/fxml/LoginView.fxml");
            } catch (Exception e) {
                System.err.println("Erro ao voltar para login: " + e.getMessage());
                e.printStackTrace();
                mostrarErro("Erro ao voltar para tela de login: " + e.getMessage());
            }
        });
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
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Erro ao abrir política de privacidade: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir política de privacidade: " + e.getMessage());
        }
    }

    private void limparCampos() {
        nomeField.clear();
        emailField.clear();
        senhaField.clear();
        confirmarSenhaField.clear();
        departamentoCombo.setValue("Outros");
        lgpdCheckbox.setSelected(false);
        mensagemErro.setVisible(false);
    }
} 