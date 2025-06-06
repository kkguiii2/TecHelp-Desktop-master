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
            novoUsuario.setLgpdAceite(true);
            novoUsuario.setDataAceiteLgpd(LocalDateTime.now());
            
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