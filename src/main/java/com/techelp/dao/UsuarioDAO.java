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
        String sql = "INSERT INTO usuarios (name_user, email, password, type_user, dept, data_criacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
                    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipo().name());
            stmt.setString(5, usuario.getDepartamento());
            stmt.setTimestamp(6, Timestamp.valueOf(usuario.getDataCriacao()));
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getLong(1));
                }
            }
        }
    }
    
    public Optional<Usuario> buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT id_user, name_user, email, password, type_user, dept, data_criacao FROM usuarios WHERE email = ?";
        
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
        String sql = "SELECT id_user, name_user, email, password, type_user, dept, data_criacao FROM usuarios WHERE id_user = ?";
        
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
        String sql = "SELECT id_user, name_user, email, password, type_user, dept, data_criacao FROM usuarios ORDER BY name_user";
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
        String sql = "UPDATE usuarios SET name_user = ?, email = ?, type_user = ?, dept = ? WHERE id_user = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getTipo().name());
            stmt.setString(4, usuario.getDepartamento());
            stmt.setLong(5, usuario.getId());
            
            stmt.executeUpdate();
        }
    }
    
    public void excluir(Long id) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_user = ?";
        
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
    
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id_user"));
        usuario.setNome(rs.getString("name_user"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("password"));
        usuario.setTipo(Usuario.TipoUsuario.valueOf(rs.getString("type_user")));
        usuario.setDepartamento(rs.getString("dept"));
        usuario.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        return usuario;
    }
} 