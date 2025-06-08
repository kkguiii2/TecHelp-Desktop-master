package com.techelp.repository;

import com.techelp.model.entity.Interacao;
import com.techelp.model.entity.Chamado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class InteracaoRepository extends BaseRepository {
    
    public List<Interacao> findByChamadoId(Long chamadoId) {
        String sql = "SELECT * FROM interacoes WHERE chamado_id = ? ORDER BY data_hora";
        List<Interacao> interacoes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, chamadoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interacoes.add(mapResultSetToInteracao(rs));
                }
            }
            
            return interacoes;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar interações: " + e.getMessage(), e);
        }
    }
    
    public Interacao save(Interacao interacao) {
        String sql = """
            INSERT INTO interacoes (
                mensagem, tipo, usuario_id, chamado_id, data_hora
            ) VALUES (?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, interacao.getMensagem());
            stmt.setString(2, interacao.getTipo().name());
            stmt.setLong(3, interacao.getUsuario().getId());
            stmt.setLong(4, interacao.getChamado().getId());
            
            // Garante que a data_hora está definida
            if (interacao.getDataHora() == null) {
                interacao.setDataHora(LocalDateTime.now());
            }
            stmt.setTimestamp(5, Timestamp.valueOf(interacao.getDataHora()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new RuntimeException("Falha ao criar interação, nenhuma linha afetada.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    interacao.setId(generatedKeys.getLong(1));
                } else {
                    throw new RuntimeException("Falha ao criar interação, ID não obtido.");
                }
            }
            
            return interacao;
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar interação: " + e.getMessage(), e);
        }
    }
    
    private Interacao mapResultSetToInteracao(ResultSet rs) throws Exception {
        Interacao interacao = new Interacao();
        interacao.setId(rs.getLong("id"));
        interacao.setMensagem(rs.getString("mensagem"));
        interacao.setTipo(Interacao.TipoInteracao.valueOf(rs.getString("tipo")));
        
        // Busca o usuário
        Long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            UsuarioRepository usuarioRepo = new UsuarioRepository();
            interacao.setUsuario(usuarioRepo.findById(usuarioId));
        }
        
        // Busca o chamado
        Long chamadoId = rs.getLong("chamado_id");
        if (!rs.wasNull()) {
            ChamadoRepository chamadoRepo = new ChamadoRepository();
            interacao.setChamado(chamadoRepo.findById(chamadoId).orElse(null));
        }
        
        // Trata data_hora nula
        Timestamp dataHora = rs.getTimestamp("data_hora");
        if (dataHora != null) {
            interacao.setDataHora(dataHora.toLocalDateTime());
        } else {
            LocalDateTime agora = LocalDateTime.now();
            interacao.setDataHora(agora);
            
            // Atualiza o registro no banco com a data atual
            try (PreparedStatement updateStmt = rs.getStatement().getConnection()
                    .prepareStatement("UPDATE interacoes SET data_hora = ? WHERE id = ?")) {
                updateStmt.setTimestamp(1, Timestamp.valueOf(agora));
                updateStmt.setLong(2, interacao.getId());
                updateStmt.executeUpdate();
            } catch (Exception e) {
                System.err.println("Erro ao atualizar data_hora: " + e.getMessage());
            }
        }
        
        return interacao;
    }

    public List<Interacao> findByChamado(Chamado chamado) {
        String sql = "SELECT * FROM interacoes WHERE chamado_id = ?";
        List<Interacao> interacoes = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, chamado.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    interacoes.add(mapResultSetToInteracao(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar interações do chamado", e);
        }
        return interacoes;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM interacoes WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir interação: " + e.getMessage(), e);
        }
    }
    
    public void deleteAll() {
        String sql = "DELETE FROM interacoes";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir todas as interações: " + e.getMessage(), e);
        }
    }
} 