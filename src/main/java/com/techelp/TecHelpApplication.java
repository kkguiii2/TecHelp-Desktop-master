package com.techelp;

import com.techelp.config.DatabaseInitializer;
import com.techelp.controller.LoginController;
import com.techelp.util.WindowManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.techelp.config.DatabaseSyncManager;

public class TecHelpApplication extends Application {
    private DatabaseSyncManager syncManager;
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Inicializa o banco de dados
        new DatabaseInitializer().initialize();
        
        // Inicializa o gerenciador de sincronização
        syncManager = new DatabaseSyncManager();
        
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
    
    @Override
    public void stop() {
        // Para o gerenciador de sincronização quando a aplicação é fechada
        if (syncManager != null) {
            syncManager.stopSync();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 