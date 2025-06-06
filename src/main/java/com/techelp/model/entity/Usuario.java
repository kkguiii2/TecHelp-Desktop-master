package com.techelp.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoUsuario tipo;

    @Column(nullable = false)
    private String departamento;

    private String telefone;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @Column(name = "lgpd_aceite")
    private boolean lgpdAceite;

    @Column(name = "data_aceite_lgpd")
    private LocalDateTime dataAceiteLgpd;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "permissoes_usuario", 
        joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "permissao")
    private Set<String> permissoes = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
    }

    public enum TipoUsuario {
        ADMIN,
        TECNICO,
        SOLICITANTE
    }
} 