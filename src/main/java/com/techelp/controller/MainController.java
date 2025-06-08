package com.techelp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;
import com.techelp.config.DatabaseSyncManager;

public class MainController {
    @FXML
    private StackPane contentArea;
    
    private DatabaseSyncManager syncManager;
    private StatusBarController statusBarController;
    
    @FXML
    public void initialize() {
        // Inicializa o gerenciador de sincronização
        syncManager = new DatabaseSyncManager();
        
        // Configura a barra de status
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StatusBar.fxml"));
            loader.load();
            statusBarController = loader.getController();
            syncManager.setStatusBar(statusBarController);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void setContent(javafx.scene.Node node) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(node);
    }
    
    public DatabaseSyncManager getSyncManager() {
        return syncManager;
    }
} 