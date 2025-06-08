package com.techelp.model.dto;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Interacao;
import com.techelp.model.entity.Usuario;
import com.techelp.model.entity.Usuario.TipoUsuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoDTO {
    private Long id;
    private String titulo;
    private String descricao;
    private Chamado.StatusChamado status;
    private Chamado.PrioridadeChamado prioridade;
    private String categoria;
    private UsuarioDTO solicitante;
    private UsuarioDTO tecnico;
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String categoriaIa;
    private Long tempoResolucao;
    private Integer avaliacao;
    private List<InteracaoDTO> interacoes;
    
    public ChamadoDTO(Chamado chamado) {
        this.id = chamado.getId();
        this.titulo = chamado.getTitulo();
        this.descricao = chamado.getDescricao();
        this.status = chamado.getStatus();
        this.prioridade = chamado.getPrioridade();
        this.categoria = chamado.getCategoria();
        this.solicitante = new UsuarioDTO(chamado.getSolicitante());
        this.tecnico = chamado.getTecnico() != null ? new UsuarioDTO(chamado.getTecnico()) : null;
        this.dataAbertura = chamado.getDataAbertura();
        this.dataFechamento = chamado.getDataFechamento();
        this.categoriaIa = chamado.getCategoriaIa();
        this.tempoResolucao = chamado.getTempoResolucao();
        this.avaliacao = chamado.getAvaliacao();
        this.interacoes = chamado.getInteracoes().stream()
            .map(InteracaoDTO::new)
            .collect(Collectors.toList());
    }
    
    private Usuario convertToModel(com.techelp.model.entity.Usuario entityUsuario) {
        if (entityUsuario == null) return null;
        Usuario usuario = new Usuario();
        usuario.setId(entityUsuario.getId());
        usuario.setNome(entityUsuario.getNome());
        usuario.setEmail(entityUsuario.getEmail());
        usuario.setTipo(TipoUsuario.valueOf(entityUsuario.getTipo().name()));
        usuario.setDepartamento(entityUsuario.getDepartamento());
        return usuario;
    }

    private com.techelp.model.entity.Usuario convertToEntity(Usuario usuario) {
        if (usuario == null) return null;
        com.techelp.model.entity.Usuario entityUsuario = new com.techelp.model.entity.Usuario();
        entityUsuario.setId(usuario.getId());
        entityUsuario.setNome(usuario.getNome());
        entityUsuario.setEmail(usuario.getEmail());
        entityUsuario.setTipo(com.techelp.model.entity.Usuario.TipoUsuario.valueOf(usuario.getTipo().name()));
        entityUsuario.setDepartamento(usuario.getDepartamento());
        return entityUsuario;
    }
    
    // Método para converter DTO para entidade
    public Chamado toEntity() {
        Chamado chamado = new Chamado();
        chamado.setId(this.id);
        chamado.setTitulo(this.titulo);
        chamado.setDescricao(this.descricao);
        chamado.setStatus(this.status);
        chamado.setPrioridade(this.prioridade);
        chamado.setSolicitante(convertToEntity(this.solicitante.toEntity()));
        if (this.tecnico != null) {
            chamado.setTecnico(convertToEntity(this.tecnico.toEntity()));
        }
        chamado.setDataAbertura(this.dataAbertura);
        chamado.setDataFechamento(this.dataFechamento);
        chamado.setCategoriaIa(this.categoriaIa);
        chamado.setTempoResolucao(this.tempoResolucao);
        chamado.setAvaliacao(this.avaliacao);
        return chamado;
    }
    
    // Método para calcular o tempo decorrido em minutos
    public long getTempoDecorrido() {
        LocalDateTime fim = dataFechamento != null ? dataFechamento : LocalDateTime.now();
        return ChronoUnit.HOURS.between(dataAbertura, fim);
    }

    private static UsuarioDTO mapUsuarioToDTO(Usuario entityUsuario) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(entityUsuario.getId());
        usuario.setNome(entityUsuario.getNome());
        usuario.setEmail(entityUsuario.getEmail());
        usuario.setTipo(entityUsuario.getTipo());
        usuario.setDepartamento(entityUsuario.getDepartamento());
        return usuario;
    }

    private static Usuario mapDTOToUsuario(UsuarioDTO usuario) {
        Usuario entityUsuario = new Usuario();
        entityUsuario.setId(usuario.getId());
        entityUsuario.setNome(usuario.getNome());
        entityUsuario.setEmail(usuario.getEmail());
        entityUsuario.setTipo(usuario.getTipo());
        entityUsuario.setDepartamento(usuario.getDepartamento());
        return entityUsuario;
    }
} 