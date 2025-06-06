package com.techelp.util;

import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

public class WindowManager {
    // Tamanhos padrão para diferentes tipos de janelas
    private static final double LOGIN_WIDTH = 400;
    private static final double LOGIN_HEIGHT = 700;
    
    private static final double MAIN_WIDTH = 1024;
    private static final double MAIN_HEIGHT = 768;
    
    private static final double MIN_WIDTH = 400;
    private static final double MIN_HEIGHT = 700;

    public static void setupLoginWindow(Stage stage) {
        // Configura o tamanho da janela de login
        stage.setWidth(LOGIN_WIDTH);
        stage.setHeight(LOGIN_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaximized(false);
        
        // Centraliza a janela
        centerStage(stage);
    }

    public static void setupMainWindow(Stage stage) {
        // Configura o tamanho das janelas principais
        stage.setWidth(MAIN_WIDTH);
        stage.setHeight(MAIN_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setMaximized(false);
        
        // Centraliza a janela
        centerStage(stage);
    }

    private static void centerStage(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
} 