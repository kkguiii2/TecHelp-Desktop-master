package com.techelp.model.dto;

import com.techelp.model.entity.Interacao;
import com.techelp.model.entity.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteracaoDTO {
    private Long id;
    private String mensagem;
    private UsuarioDTO usuario;
    private LocalDateTime dataHora;
    private Interacao.TipoInteracao tipo;
    
    // Construtor que converte de Interacao para InteracaoDTO
    public InteracaoDTO(Interacao interacao) {
        this.id = interacao.getId();
        this.mensagem = interacao.getMensagem();
        this.usuario = new UsuarioDTO(interacao.getUsuario());
        this.dataHora = interacao.getDataHora();
        this.tipo = interacao.getTipo();
    }
    
    // Método para converter DTO para entidade
    public Interacao toEntity() {
        Interacao interacao = new Interacao();
        interacao.setId(this.id);
        interacao.setMensagem(this.mensagem);
        interacao.setUsuario(this.usuario.toEntity());
        interacao.setDataHora(this.dataHora);
        interacao.setTipo(this.tipo);
        return interacao;
    }
} 