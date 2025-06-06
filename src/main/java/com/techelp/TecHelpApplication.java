package com.techelp;

import com.techelp.config.DatabaseInitializer;
import com.techelp.controller.LoginController;
import com.techelp.util.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TecHelpApplication extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializa o banco de dados
        new DatabaseInitializer().initialize();
        
        // Carrega a tela de login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();
        
        // Configura o controller
        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        
        primaryStage.setTitle("TecHelp - Sistema de Chamados");
        primaryStage.setScene(scene);
        
        // Configura o tamanho da janela de login
        WindowManager.setupLoginWindow(primaryStage);
        
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 