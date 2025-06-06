package com.techelp.model.dto;

import com.techelp.model.entity.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private Usuario.TipoUsuario tipo;
    private boolean lgpdAceite;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAceiteLgpd;
    private LocalDateTime ultimoAcesso;
    
    // Construtor que converte de Usuario para UsuarioDTO, omitindo dados sensíveis
    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.telefone = usuario.getTelefone();
        this.tipo = usuario.getTipo();
        this.lgpdAceite = usuario.isLgpdAceite();
        this.dataCriacao = usuario.getDataCriacao();
        this.dataAceiteLgpd = usuario.getDataAceiteLgpd();
        this.ultimoAcesso = usuario.getUltimoAcesso();
    }
    
    // Método para converter DTO para entidade (usado em atualizações)
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setId(this.id);
        usuario.setNome(this.nome);
        usuario.setEmail(this.email);
        usuario.setTelefone(this.telefone);
        usuario.setTipo(this.tipo);
        usuario.setLgpdAceite(this.lgpdAceite);
        usuario.setDataCriacao(this.dataCriacao);
        usuario.setDataAceiteLgpd(this.dataAceiteLgpd);
        usuario.setUltimoAcesso(this.ultimoAcesso);
        return usuario;
    }
} 