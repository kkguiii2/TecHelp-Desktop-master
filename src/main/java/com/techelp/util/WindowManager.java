package com.techelp.util;

import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.application.Platform;

public class WindowManager {
    // Configurações de transição
    private static final Duration TRANSITION_DURATION = Duration.millis(400);
    private static final double BLUR_AMOUNT = 5;

    public static void setupLoginWindow(Stage stage) {
        // Obtém as dimensões da tela
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Configura a janela para ocupar toda a tela
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        
        // Força maximização
        stage.setMaximized(true);
        
        // Aplica efeito de transição
        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            Platform.runLater(() -> {
                // Garante que a janela esteja maximizada
                stage.setMaximized(true);
                
                // Aplica os efeitos visuais
                applyTransitionEffect(stage);
            });
        }
    }

    public static void setupMainWindow(Stage stage) {
        // Obtém as dimensões da tela
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        
        // Configura a janela para ocupar toda a tela
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());
        
        // Força maximização
        stage.setMaximized(true);
        
        // Aplica efeito de transição
        if (stage.getScene() != null && stage.getScene().getRoot() != null) {
            Platform.runLater(() -> {
                // Garante que a janela esteja maximizada
                stage.setMaximized(true);
                
                // Aplica os efeitos visuais
                applyTransitionEffect(stage);
            });
        }
    }
    
    private static void applyTransitionEffect(Stage stage) {
        // Configura o efeito de blur
        GaussianBlur blur = new GaussianBlur(0);
        stage.getScene().getRoot().setEffect(blur);
        
        // Configura a opacidade inicial
        stage.getScene().getRoot().setOpacity(0);
        
        // Cria a timeline para animar o blur e a opacidade simultaneamente
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(stage.getScene().getRoot().opacityProperty(), 0),
                new KeyValue(blur.radiusProperty(), BLUR_AMOUNT)
            ),
            new KeyFrame(TRANSITION_DURATION.multiply(0.7),
                new KeyValue(stage.getScene().getRoot().opacityProperty(), 1),
                new KeyValue(blur.radiusProperty(), BLUR_AMOUNT)
            ),
            new KeyFrame(TRANSITION_DURATION,
                new KeyValue(stage.getScene().getRoot().opacityProperty(), 1),
                new KeyValue(blur.radiusProperty(), 0)
            )
        );
        
        timeline.setOnFinished(event -> {
            stage.getScene().getRoot().setEffect(null);
            // Garante que a janela permaneça maximizada após a transição
            stage.setMaximized(true);
        });
        
        timeline.play();
    }
} 