package com.techelp.service;

import com.techelp.model.entity.Usuario;
import com.techelp.model.entity.Chamado;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NotificacaoService {
    private static final Map<Long, Consumer<String>> LISTENERS = new ConcurrentHashMap<>();
    
    public void notificarUsuario(Usuario usuario, String mensagem) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Notificação");
            alert.setHeaderText(null);
            alert.setContentText(mensagem);
            alert.show();
            
            if (LISTENERS.containsKey(usuario.getId())) {
                LISTENERS.get(usuario.getId()).accept(mensagem);
            }
        });
    }
    
    public void notificarNovoChamado(Usuario tecnico, Chamado chamado) {
        String mensagem = String.format("Novo chamado atribuído: %s", chamado.getTitulo());
        notificarUsuario(tecnico, mensagem);
    }
    
    public void notificarAtualizacaoChamado(Usuario usuario, Chamado chamado) {
        String mensagem = String.format("Chamado atualizado: %s - Status: %s", 
            chamado.getTitulo(), chamado.getStatus());
        notificarUsuario(usuario, mensagem);
    }
    
    public void notificarInteracao(Usuario usuario, Chamado chamado) {
        String mensagem = String.format("Nova interação no chamado: %s", chamado.getTitulo());
        notificarUsuario(usuario, mensagem);
    }
    
    public void notificarNovaInteracao(Usuario usuario, Long chamadoId) {
        notificarUsuario(usuario, String.format("Nova interação no chamado #%d", chamadoId));
    }
    
    public void notificarAvaliacao(Usuario tecnico, Long chamadoId, Integer avaliacao) {
        notificarUsuario(tecnico, String.format("O chamado #%d recebeu avaliação %d/5", chamadoId, avaliacao));
    }
    
    public void registrarListener(Usuario usuario, Consumer<String> listener) {
        LISTENERS.put(usuario.getId(), listener);
    }
    
    public void removerListener(Usuario usuario) {
        LISTENERS.remove(usuario.getId());
    }
} 