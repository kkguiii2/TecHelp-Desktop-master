package com.techelp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import com.techelp.config.DatabaseSyncManager;

public class StatusBarController {
    @FXML
    private Label statusLabel;
    
    private DatabaseSyncManager syncManager;
    
    public void initialize() {
        syncManager = new DatabaseSyncManager();
        updateStatus("Aguardando conexão com SQL Server...", Color.ORANGE);
    }
    
    public void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setTextFill(color);
    }
    
    public void setSyncManager(DatabaseSyncManager manager) {
        this.syncManager = manager;
    }
} 