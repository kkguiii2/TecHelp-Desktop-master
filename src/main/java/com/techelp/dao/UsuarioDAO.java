package com.techelp.dao;

import com.techelp.db.DatabaseConnection;
import com.techelp.model.entity.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class UsuarioDAO {
    
    public void salvar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email, senha, telefone, tipo, lgpd_aceite, data_criacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getTipo().name());
            stmt.setBoolean(6, usuario.isLgpdAceite());
            stmt.setTimestamp(7, Timestamp.valueOf(usuario.getDataCriacao()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
        }
    }
    
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY nome";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        
        return usuarios;
    }
    
    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, email = ?, telefone = ?, tipo = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTelefone());
            stmt.setString(4, usuario.getTipo().name());
            stmt.setLong(5, usuario.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }
    
    public boolean emailJaExiste(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    public void atualizarUltimoAcesso(Long id) throws SQLException {
        String sql = "UPDATE usuarios SET ultimoAcesso = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(java.time.LocalDateTime.now()));
            stmt.setLong(2, id);
            
            stmt.executeUpdate();
        }
    }
    
    public void atualizarAceiteLgpd(Long id, LocalDateTime dataAceite) throws SQLException {
        String sql = "UPDATE usuarios SET lgpdAceite = true, dataAceiteLgpd = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(dataAceite));
            stmt.setLong(2, id);
            
            stmt.executeUpdate();
        }
    }
    
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setTipo(Usuario.TipoUsuario.valueOf(rs.getString("tipo")));
        usuario.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        
        Timestamp ultimoAcesso = rs.getTimestamp("ultimo_acesso");
        if (ultimoAcesso != null) {
            usuario.setUltimoAcesso(ultimoAcesso.toLocalDateTime());
        }
        
        usuario.setLgpdAceite(rs.getBoolean("lgpd_aceite"));
        
        Timestamp dataAceiteLgpd = rs.getTimestamp("data_aceite_lgpd");
        if (dataAceiteLgpd != null) {
            usuario.setDataAceiteLgpd(dataAceiteLgpd.toLocalDateTime());
        }
        
        return usuario;
    }
} 