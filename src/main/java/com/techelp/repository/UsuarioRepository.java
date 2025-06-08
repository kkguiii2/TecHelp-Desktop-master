package com.techelp.repository;

import com.techelp.model.entity.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository {
    
    private static final String INSERT_USUARIO = 
        "INSERT INTO usuarios (name_user, email, password, type_user, dept, data_criacao) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String SELECT_USUARIO_BY_ID = 
        "SELECT id_user, name_user, email, password, type_user, dept, data_criacao " +
        "FROM usuarios WHERE id_user = ?";
    
    private static final String SELECT_USUARIO_BY_EMAIL = 
        "SELECT id_user, name_user, email, password, type_user, dept, data_criacao " +
        "FROM usuarios WHERE email = ?";
    
    private static final String UPDATE_USUARIO = 
        "UPDATE usuarios SET name_user = ?, email = ?, password = ?, type_user = ?, dept = ? " +
        "WHERE id_user = ?";
    
    public Usuario findById(Long id) {
        String sql = SELECT_USUARIO_BY_ID;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
                return null;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }
    
    public Usuario findByEmail(String email) {
        String sql = SELECT_USUARIO_BY_EMAIL;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
                return null;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }
    
    public Usuario save(Usuario usuario) {
        String sql = usuario.getId() == null ? INSERT_USUARIO : UPDATE_USUARIO;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipo().name());
            stmt.setString(5, usuario.getDepartamento());
            
            if (usuario.getId() == null) {
                stmt.setTimestamp(6, Timestamp.valueOf(usuario.getDataCriacao()));
            } else {
                stmt.setLong(6, usuario.getId());
            }
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new RuntimeException("Falha ao salvar usuário, nenhuma linha afetada.");
            }
            
            if (usuario.getId() == null) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getLong(1));
                    } else {
                        throw new RuntimeException("Falha ao salvar usuário, ID não obtido.");
                    }
                }
            }
            
            return usuario;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        }
    }
    
    public List<Usuario> findAll() {
        String sql = "SELECT id_user, name_user, email, password, type_user, dept, data_criacao FROM usuarios";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
            
            return usuarios;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao listar usuários: " + e.getMessage(), e);
        }
    }
    
    public void delete(Long id) {
        String sql = "DELETE FROM usuarios WHERE id_user = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuário: " + e.getMessage(), e);
        }
    }
    
    public List<Usuario> findByTipo(Usuario.TipoUsuario tipo) {
        String sql = "SELECT id_user, name_user, email, password, type_user, dept, data_criacao FROM usuarios WHERE type_user = ?";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tipo.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
            
            return usuarios;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar usuários por tipo: " + e.getMessage(), e);
        }
    }
    
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
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