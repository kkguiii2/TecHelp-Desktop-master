package com.techelp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "interacoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String mensagem;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoInteracao tipo;

    @PrePersist
    protected void onCreate() {
        dataHora = LocalDateTime.now();
    }

    public enum TipoInteracao {
        RESPOSTA_TECNICO,
        COMENTARIO_CLIENTE,
        MUDANCA_STATUS,
        RESPOSTA_CHATBOT,
        NOTA_INTERNA
    }
} 