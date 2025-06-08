package com.techelp.service;

import com.techelp.model.entity.Usuario;
import com.techelp.repository.UsuarioRepository;
import com.techelp.cache.CacheManager;
import java.util.List;
import java.util.Optional;

public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final CacheManager cacheManager;
    
    public UsuarioService() {
        this.usuarioRepository = new UsuarioRepository();
        this.cacheManager = CacheManager.getInstance();
    }
    
    public Usuario findById(Long id) {
        return cacheManager.getUsuarioCache().get(id, key -> {
            Usuario usuario = usuarioRepository.findById(id);
            if (usuario != null) {
                cacheManager.getUsuarioCache().put(id, usuario);
            }
            return usuario;
        });
    }
    
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }
    
    public Usuario salvar(Usuario usuario) {
        Usuario saved = usuarioRepository.save(usuario);
        if (saved != null) {
            cacheManager.getUsuarioCache().put(saved.getId(), saved);
        }
        return saved;
    }
    
    public Usuario atualizar(Usuario usuario) {
        Usuario updated = usuarioRepository.save(usuario);
        if (updated != null) {
            cacheManager.getUsuarioCache().put(updated.getId(), updated);
        }
        return updated;
    }
    
    public void excluir(Long id) {
        usuarioRepository.delete(id);
        cacheManager.getUsuarioCache().invalidate(id);
    }
    
    public void limparCache() {
        cacheManager.getUsuarioCache().invalidateAll();
    }

    public List<Usuario> listarTecnicos() {
        try {
            return usuarioRepository.findAll().stream()
                .filter(u -> u.getTipo() == Usuario.TipoUsuario.TECNICO)
                .toList();
        } catch (Exception e) {
            System.err.println("Erro ao listar técnicos: " + e.getMessage());
            return List.of(); // Retorna lista vazia em caso de erro
        }
    }
} 