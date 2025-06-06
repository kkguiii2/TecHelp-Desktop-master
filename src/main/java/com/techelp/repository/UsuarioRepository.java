package com.techelp.repository;

import com.techelp.model.entity.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UsuarioRepository extends BaseRepository {
    
    public Usuario findById(Long id) {
        String sql = "SELECT * FROM [dbo].[usuarios] WHERE id = ?";
        
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
        String sql = "SELECT * FROM [dbo].[usuarios] WHERE email = ?";
        System.out.println("Buscando usuário por email: " + email);
        System.out.println("SQL: " + sql);
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            System.out.println("Executando consulta...");
            
            try (ResultSet rs = stmt.executeQuery()) {
                System.out.println("Consulta executada!");
                if (rs.next()) {
                    System.out.println("Usuário encontrado!");
                    return mapResultSetToUsuario(rs);
                }
                System.out.println("Usuário não encontrado!");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao buscar usuário: " + e.getMessage(), e);
        }
    }
    
    public Usuario save(Usuario usuario) {
        String sql = """
            INSERT INTO [dbo].[usuarios] (
                nome, email, senha, telefone, tipo,
                data_criacao, lgpd_aceite, data_aceite_lgpd
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTelefone());
            stmt.setString(5, usuario.getTipo().name());
            stmt.setTimestamp(6, Timestamp.valueOf(usuario.getDataCriacao()));
            stmt.setBoolean(7, usuario.isLgpdAceite());
            stmt.setTimestamp(8, usuario.getDataAceiteLgpd() != null ? 
                Timestamp.valueOf(usuario.getDataAceiteLgpd()) : null);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new RuntimeException("Falha ao criar usuário, nenhuma linha afetada.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    usuario.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Falha ao criar usuário, ID não obtido.");
                }
            }
            
            return usuario;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar usuário: " + e.getMessage(), e);
        }
    }
    
    public List<Usuario> findAll() {
        String sql = "SELECT * FROM [dbo].[usuarios]";
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
        String sql = "DELETE FROM [dbo].[usuarios] WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir usuário: " + e.getMessage(), e);
        }
    }
    
    public List<Usuario> findByTipo(Usuario.TipoUsuario tipo) {
        String sql = "SELECT * FROM [dbo].[usuarios] WHERE tipo = ?";
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
        usuario.setId(rs.getLong("id"));
        usuario.setNome(rs.getString("nome"));
        usuario.setEmail(rs.getString("email"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTelefone(rs.getString("telefone"));
        usuario.setTipo(Usuario.TipoUsuario.valueOf(rs.getString("tipo")));
        usuario.setDataCriacao(rs.getTimestamp("data_criacao").toLocalDateTime());
        usuario.setLgpdAceite(rs.getBoolean("lgpd_aceite"));
        
        Timestamp dataAceiteLgpd = rs.getTimestamp("data_aceite_lgpd");
        if (dataAceiteLgpd != null) {
            usuario.setDataAceiteLgpd(dataAceiteLgpd.toLocalDateTime());
        }
        
        return usuario;
    }
} 