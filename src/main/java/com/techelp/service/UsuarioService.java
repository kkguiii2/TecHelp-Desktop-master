package com.techelp.service;

import com.techelp.model.entity.Usuario;
import com.techelp.dao.UsuarioDAO;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public void salvar(Usuario usuario) throws Exception {
        usuarioDAO.salvar(usuario);
    }

    public void atualizar(Usuario usuario) throws Exception {
        usuarioDAO.atualizar(usuario);
    }

    public void excluirUsuario(Long id) throws Exception {
        usuarioDAO.excluir(id);
    }

    public Usuario buscarPorId(Long id) throws Exception {
        Optional<Usuario> usuario = usuarioDAO.buscarPorId(id);
        return usuario.orElseThrow(() -> new Exception("Usuário não encontrado"));
    }

    public Usuario buscarPorEmail(String email) throws Exception {
        Optional<Usuario> usuario = usuarioDAO.buscarPorEmail(email);
        return usuario.orElseThrow(() -> new Exception("Usuário não encontrado"));
    }

    public List<Usuario> listarTodos() throws Exception {
        return usuarioDAO.listarTodos();
    }

    public List<Usuario> listarTecnicos() {
        try {
            return usuarioDAO.listarTodos().stream()
                .filter(u -> u.getTipo() == Usuario.TipoUsuario.TECNICO)
                .toList();
        } catch (Exception e) {
            System.err.println("Erro ao listar técnicos: " + e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }
} 