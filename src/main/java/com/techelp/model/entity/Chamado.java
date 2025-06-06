package com.techelp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusChamado status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrioridadeChamado prioridade;

    @Column(nullable = false)
    private String categoria;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    @Column(name = "categoria_ia")
    private String categoriaIa;

    @Column(name = "tempo_resolucao")
    private Long tempoResolucao;

    @Column(name = "avaliacao")
    private Integer avaliacao;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interacao> interacoes = new ArrayList<>();

    public enum StatusChamado {
        ABERTO,
        EM_ANDAMENTO,
        FECHADO,
        CANCELADO
    }

    public enum PrioridadeChamado {
        BAIXA,
        MEDIA,
        ALTA,
        CRITICA
    }
    
    public enum CategoriaChamado {
        HARDWARE("Hardware"),
        SOFTWARE("Software"),
        REDE("Rede"),
        ACESSO("Acesso"),
        EMAIL("Email"),
        IMPRESSORA("Impressora"),
        OUTROS("Outros");
        
        private final String descricao;
        
        CategoriaChamado(String descricao) {
            this.descricao = descricao;
        }
        
        public String getDescricao() {
            return descricao;
        }
    }

    @PrePersist
    protected void onCreate() {
        dataAbertura = LocalDateTime.now();
        status = StatusChamado.ABERTO;
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == StatusChamado.FECHADO && dataFechamento == null) {
            dataFechamento = LocalDateTime.now();
            tempoResolucao = java.time.Duration.between(dataAbertura, dataFechamento).toHours();
        }
    }
} 