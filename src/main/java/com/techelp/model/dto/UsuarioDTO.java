package com.techelp.model.dto;

import com.techelp.model.entity.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {
    private Long id;
    private String nome;
    private String email;
    private Usuario.TipoUsuario tipo;
    private String departamento;

    // Construtor que converte de Usuario para UsuarioDTO, omitindo dados sensíveis
    public UsuarioDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.tipo = usuario.getTipo();
        this.departamento = usuario.getDepartamento();
    }

    // Método para converter DTO para entidade (usado em atualizações)
    public Usuario toEntity() {
        Usuario usuario = new Usuario();
        usuario.setId(this.id);
        usuario.setNome(this.nome);
        usuario.setEmail(this.email);
        usuario.setTipo(this.tipo);
        usuario.setDepartamento(this.departamento);
        return usuario;
    }
} 