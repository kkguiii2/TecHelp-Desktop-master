package com.techelp.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import com.techelp.service.NotificacaoService;
import com.techelp.model.entity.Usuario;
import com.techelp.TecHelpApplication;
import com.techelp.util.WindowManager;
import javafx.application.Platform;
import javafx.scene.effect.BoxBlur;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public abstract class BaseController {
    
    protected final NotificacaoService notificacaoService;
    protected Stage primaryStage;
    
    public BaseController() {
        this.notificacaoService = new NotificacaoService();
    }
    
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        
        // Configura a janela para sempre iniciar maximizada
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        stage.setMaximized(true);
    }
    
    protected void inicializarNotificacoes(Usuario usuario) {
        // Registra um listener para notificações do usuário
        notificacaoService.registrarListener(
            usuario, 
            mensagem -> mostrarSucesso(mensagem)
        );
    }
    
    protected void carregarTela(String fxml) throws IOException {
        try {
            System.out.println("Tentando carregar tela: " + fxml);
            
            // Primeiro, tenta carregar usando o caminho direto em resources
            URL fxmlUrl = getClass().getClassLoader().getResource(fxml.startsWith("/") ? fxml.substring(1) : fxml);
            
            // Se não encontrar, tenta na pasta fxml
            if (fxmlUrl == null) {
                String fxmlPath = "fxml/" + (fxml.contains("/") ? fxml.substring(fxml.lastIndexOf("/") + 1) : fxml);
                System.out.println("Tentando carregar de: " + fxmlPath);
                fxmlUrl = getClass().getClassLoader().getResource(fxmlPath);
            }
            
            if (fxmlUrl == null) {
                throw new IOException("Arquivo FXML não encontrado: " + fxml);
            }
            
            System.out.println("URL do FXML encontrada: " + fxmlUrl);
            
            // Carrega o FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Configura o controller
            BaseController controller = loader.getController();
            if (controller != null) {
                if (primaryStage != null) {
                    controller.setPrimaryStage(primaryStage);
                } else if (getClass().equals(LoginController.class)) {
                    // Se for o LoginController e não tiver Stage, cria um novo
                    Stage newStage = new Stage();
                    controller.setPrimaryStage(newStage);
                    primaryStage = newStage;
                } else {
                    throw new RuntimeException("Stage não foi inicializado. Verifique se setPrimaryStage foi chamado.");
                }
            }
            
            // Cria e configura a Scene
            Scene scene = new Scene(root);
            
            // Tenta carregar o CSS
            URL cssUrl = getClass().getClassLoader().getResource("css/styles.css");
            if (cssUrl != null) {
                System.out.println("CSS encontrado: " + cssUrl);
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Arquivo CSS não encontrado");
            }
            
            // Define a Scene
            primaryStage.setScene(scene);
            
            // Configura o tamanho da janela e aplica transições
            if (fxml.contains("LoginView") || fxml.contains("CadastroView")) {
                WindowManager.setupLoginWindow(primaryStage);
            } else {
                WindowManager.setupMainWindow(primaryStage);
            }
            
            // Mostra a janela
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar tela: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    protected void carregarTela(String fxml, Object dados) throws IOException {
        try {
            System.out.println("Tentando carregar tela com dados: " + fxml);
            
            URL fxmlUrl = getClass().getClassLoader().getResource(fxml.startsWith("/") ? fxml.substring(1) : fxml);
            
            if (fxmlUrl == null) {
                String fxmlPath = "fxml/" + (fxml.contains("/") ? fxml.substring(fxml.lastIndexOf("/") + 1) : fxml);
                fxmlUrl = getClass().getClassLoader().getResource(fxmlPath);
            }
            
            if (fxmlUrl == null) {
                throw new IOException("Arquivo FXML não encontrado: " + fxml);
            }
            
            // Carrega o FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            
            // Configura o controller
            BaseController controller = loader.getController();
            if (controller != null) {
                if (primaryStage != null) {
                    controller.setPrimaryStage(primaryStage);
                } else if (getClass().equals(LoginController.class)) {
                    // Se for o LoginController e não tiver Stage, cria um novo
                    Stage newStage = new Stage();
                    controller.setPrimaryStage(newStage);
                    primaryStage = newStage;
                } else {
                    throw new RuntimeException("Stage não foi inicializado. Verifique se setPrimaryStage foi chamado.");
                }
                
                if (controller instanceof DadosAware) {
                    ((DadosAware) controller).setDados(dados);
                }
            }
            
            // Cria e configura a Scene
            Scene scene = new Scene(root);
            URL cssUrl = getClass().getClassLoader().getResource("css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            // Define a Scene
            primaryStage.setScene(scene);
            
            // Configura o tamanho da janela e aplica transições
            if (fxml.contains("LoginView") || fxml.contains("CadastroView")) {
                WindowManager.setupLoginWindow(primaryStage);
            } else {
                WindowManager.setupMainWindow(primaryStage);
            }
            
            // Mostra a janela
            primaryStage.show();
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar tela com dados: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    protected void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
    
    protected void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
} 