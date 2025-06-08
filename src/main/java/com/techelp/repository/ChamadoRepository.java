package com.techelp.repository;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;

public class ChamadoRepository extends BaseRepository {
    
    private static final String SELECT_CHAMADO_BASE = 
        "SELECT c.*, " +
        "s.name_user as solicitante_nome, s.email as solicitante_email, s.type_user as solicitante_tipo, " +
        "t.name_user as tecnico_nome, t.email as tecnico_email, t.type_user as tecnico_tipo " +
        "FROM chamados c " +
        "LEFT JOIN usuarios s ON c.solicitante_id = s.id_user " +
        "LEFT JOIN usuarios t ON c.tecnico_id = t.id_user";
    
    public List<Chamado> findBySolicitante(Usuario solicitante) {
        List<Chamado> chamados = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            WHERE c.solicitante_id = ?
            ORDER BY c.data_abertura DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, solicitante.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados do solicitante", e);
        }
        return chamados;
    }
    
    public List<Chamado> findByTecnico(Usuario tecnico) {
        List<Chamado> chamados = new ArrayList<>();
        String sql;

        if (tecnico.getTipo() == Usuario.TipoUsuario.ADMIN) {
            sql = """
                SELECT c.*,
                       s.name_user as solicitante_nome, s.email as solicitante_email,
                       s.type_user as solicitante_tipo,
                       t.name_user as tecnico_nome, t.email as tecnico_email,
                       t.type_user as tecnico_tipo
                FROM chamados c
                INNER JOIN usuarios s ON c.solicitante_id = s.id_user
                LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
                ORDER BY c.data_abertura DESC
            """;
        } else {
            sql = """
                SELECT c.*,
                       s.name_user as solicitante_nome, s.email as solicitante_email,
                       s.type_user as solicitante_tipo,
                       t.name_user as tecnico_nome, t.email as tecnico_email,
                       t.type_user as tecnico_tipo
                FROM chamados c
                INNER JOIN usuarios s ON c.solicitante_id = s.id_user
                LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
                WHERE (c.tecnico_id = ? OR c.tecnico_id IS NULL)
                ORDER BY c.data_abertura DESC
            """;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (tecnico.getTipo() != Usuario.TipoUsuario.ADMIN) {
                stmt.setLong(1, tecnico.getId());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados do técnico", e);
        }
        return chamados;
    }
    
    public List<Chamado> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        List<Chamado> chamados = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            WHERE c.data_abertura BETWEEN ? AND ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados por período", e);
        }
        return chamados;
    }
    
    public List<Chamado> findByTecnicoEStatus(Usuario tecnico, Chamado.StatusChamado status) {
        List<Chamado> chamados = new ArrayList<>();
        String sql;

        if (tecnico.getTipo() == Usuario.TipoUsuario.ADMIN) {
            // Se for ADMIN, buscar chamados por status (para todos os técnicos)
            sql = """
                SELECT c.*,
                       s.name_user as solicitante_nome, s.email as solicitante_email,
                       s.type_user as solicitante_tipo,
                       t.name_user as tecnico_nome, t.email as tecnico_email,
                       t.type_user as tecnico_tipo
                FROM chamados c
                INNER JOIN usuarios s ON c.solicitante_id = s.id_user
                LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
                WHERE c.status = ?
                ORDER BY c.data_abertura DESC
            """;
        } else {
            // Se não for ADMIN, buscar chamados por técnico e status
            sql = """
                SELECT c.*,
                       s.name_user as solicitante_nome, s.email as solicitante_email,
                       s.type_user as solicitante_tipo,
                       t.name_user as tecnico_nome, t.email as tecnico_email,
                       t.type_user as tecnico_tipo
                FROM chamados c
                INNER JOIN usuarios s ON c.solicitante_id = s.id_user
                LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
                WHERE c.tecnico_id = ? AND c.status = ?
                ORDER BY c.data_abertura DESC
            """;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (tecnico.getTipo() == Usuario.TipoUsuario.ADMIN) {
                stmt.setString(1, status.name());
            } else {
                stmt.setLong(1, tecnico.getId());
                stmt.setString(2, status.name());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados por técnico e status", e);
        }
        return chamados;
    }
    
    public Double calcularTempoMedioResolucaoPorTecnico(Usuario tecnico) {
        String sql = "SELECT AVG(tempo_resolucao) FROM chamados WHERE tecnico_id = ? AND status = 'FECHADO'";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tecnico.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular tempo médio de resolução", e);
        }
        return 0.0;
    }
    
    public List<Object[]> contarChamadosPorCategoria() {
        List<Object[]> resultados = new ArrayList<>();
        String sql = "SELECT categoria_ia, COUNT(*) FROM chamados GROUP BY categoria_ia";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                resultados.add(new Object[]{rs.getString(1), rs.getLong(2)});
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados por categoria", e);
        }
        return resultados;
    }
    
    public Chamado save(Chamado chamado) {
        if (chamado.getId() == null) {
            return insert(chamado);
        } else {
            return update(chamado);
        }
    }
    
    private Chamado insert(Chamado chamado) {
        String sql = """
            INSERT INTO chamados (
                titulo, descricao, status, prioridade, categoria, categoria_ia,
                solicitante_id, tecnico_id, data_abertura, data_fechamento,
                avaliacao, tempo_resolucao
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, chamado.getTitulo());
            stmt.setString(2, chamado.getDescricao());
            stmt.setString(3, chamado.getStatus().name());
            stmt.setString(4, chamado.getPrioridade().name());
            stmt.setString(5, chamado.getCategoria());
            stmt.setString(6, chamado.getCategoriaIa());
            stmt.setLong(7, chamado.getSolicitante().getId());
            
            if (chamado.getTecnico() != null) {
                stmt.setLong(8, chamado.getTecnico().getId());
            } else {
                stmt.setNull(8, Types.BIGINT);
            }
            
            stmt.setTimestamp(9, Timestamp.valueOf(chamado.getDataAbertura()));
            
            if (chamado.getDataFechamento() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(chamado.getDataFechamento()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            if (chamado.getAvaliacao() != null) {
                stmt.setInt(11, chamado.getAvaliacao());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            
            if (chamado.getTempoResolucao() != null) {
                stmt.setLong(12, chamado.getTempoResolucao());
            } else {
                stmt.setNull(12, Types.BIGINT);
            }
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    chamado.setId(rs.getLong(1));
                }
            }
            
            return chamado;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir chamado", e);
        }
    }
    
    private Chamado update(Chamado chamado) {
        String sql = """
            UPDATE chamados SET 
                titulo = ?, 
                descricao = ?, 
                status = ?, 
                prioridade = ?, 
                categoria = ?,
                categoria_ia = ?,
                solicitante_id = ?, 
                tecnico_id = ?, 
                data_abertura = ?, 
                data_fechamento = ?,
                avaliacao = ?,
                tempo_resolucao = ?
            WHERE id = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, chamado.getTitulo());
            stmt.setString(2, chamado.getDescricao());
            stmt.setString(3, chamado.getStatus().name());
            stmt.setString(4, chamado.getPrioridade().name());
            stmt.setString(5, chamado.getCategoria());
            stmt.setString(6, chamado.getCategoriaIa());
            stmt.setLong(7, chamado.getSolicitante().getId());
            
            if (chamado.getTecnico() != null) {
                stmt.setLong(8, chamado.getTecnico().getId());
            } else {
                stmt.setNull(8, Types.BIGINT);
            }
            
            stmt.setTimestamp(9, Timestamp.valueOf(chamado.getDataAbertura()));
            
            if (chamado.getDataFechamento() != null) {
                stmt.setTimestamp(10, Timestamp.valueOf(chamado.getDataFechamento()));
            } else {
                stmt.setNull(10, Types.TIMESTAMP);
            }
            
            if (chamado.getAvaliacao() != null) {
                stmt.setInt(11, chamado.getAvaliacao());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            
            if (chamado.getTempoResolucao() != null) {
                stmt.setLong(12, chamado.getTempoResolucao());
            } else {
                stmt.setNull(12, Types.BIGINT);
            }
            
            stmt.setLong(13, chamado.getId());
            
            stmt.executeUpdate();
            return chamado;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar chamado", e);
        }
    }
    
    public Optional<Chamado> findById(Long id) {
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            WHERE c.id = ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToChamado(rs));
                }
                return Optional.empty();
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamado: " + e.getMessage(), e);
        }
    }
    
    public List<Chamado> findAll() {
        List<Chamado> chamados = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            ORDER BY c.data_abertura DESC
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar todos os chamados", e);
        }
        return chamados;
    }
    
    private Chamado mapResultSetToChamado(ResultSet rs) throws SQLException {
        try {
            Chamado chamado = new Chamado();
            chamado.setId(rs.getLong("id"));
            chamado.setTitulo(rs.getString("titulo"));
            chamado.setDescricao(rs.getString("descricao"));
            chamado.setStatus(Chamado.StatusChamado.valueOf(rs.getString("status")));
            chamado.setPrioridade(Chamado.PrioridadeChamado.valueOf(rs.getString("prioridade")));
            chamado.setCategoria(rs.getString("categoria"));
            
            // Criar objeto Usuario para o solicitante
            Usuario solicitante = new Usuario();
            solicitante.setId(rs.getLong("solicitante_id"));
            solicitante.setNome(rs.getString("solicitante_nome"));
            solicitante.setEmail(rs.getString("solicitante_email"));
            solicitante.setTipo(Usuario.TipoUsuario.valueOf(rs.getString("solicitante_tipo")));
            chamado.setSolicitante(solicitante);
            
            // Criar objeto Usuario para o técnico se existir
            Long tecnicoId = rs.getLong("tecnico_id");
            if (!rs.wasNull()) {
                Usuario tecnico = new Usuario();
                tecnico.setId(tecnicoId);
                tecnico.setNome(rs.getString("tecnico_nome"));
                tecnico.setEmail(rs.getString("tecnico_email"));
                tecnico.setTipo(Usuario.TipoUsuario.valueOf(rs.getString("tecnico_tipo")));
                chamado.setTecnico(tecnico);
            }
            
            Timestamp dataAbertura = rs.getTimestamp("data_abertura");
            if (dataAbertura != null) {
                chamado.setDataAbertura(dataAbertura.toLocalDateTime());
            }
            
            Timestamp dataFechamento = rs.getTimestamp("data_fechamento");
            if (dataFechamento != null) {
                chamado.setDataFechamento(dataFechamento.toLocalDateTime());
            }
            
            // Mapear categoria_ia se existir
            String categoriaIa = rs.getString("categoria_ia");
            chamado.setCategoriaIa(rs.wasNull() ? null : categoriaIa);
            
            // Mapear tempo_resolucao se existir
            long tempoResolucaoVal = rs.getLong("tempo_resolucao");
            chamado.setTempoResolucao(rs.wasNull() ? null : tempoResolucaoVal);
            
            return chamado;
        } catch (SQLException e) {
            System.err.println("Erro ao mapear chamado do ResultSet: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void mapUsuarioFromResultSet(ResultSet rs, String prefix, Usuario usuario) throws SQLException {
        usuario.setId(rs.getLong(prefix + "_id"));
        usuario.setNome(rs.getString(prefix + "_nome"));
        usuario.setEmail(rs.getString(prefix + "_email"));
        usuario.setTipo(Usuario.TipoUsuario.valueOf(rs.getString(prefix + "_tipo")));
    }

    public int countByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT COUNT(*) FROM chamados WHERE data_abertura BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados", e);
        }
    }

    public double getTempoMedioResolucao(LocalDateTime inicio, LocalDateTime fim) {
        String sql = """
            SELECT COALESCE(AVG(
                CAST(
                    TIMESTAMPDIFF(HOUR, data_abertura, data_fechamento) 
                    AS DOUBLE
                )
            ), 0.0)
            FROM chamados 
            WHERE data_fechamento IS NOT NULL 
            AND data_abertura BETWEEN ? AND ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular tempo médio", e);
        }
    }

    public double getMediaAvaliacoes(LocalDateTime inicio, LocalDateTime fim) {
        String sql = """
            SELECT COALESCE(AVG(CAST(avaliacao AS DOUBLE)), 0.0) 
            FROM chamados 
            WHERE avaliacao IS NOT NULL 
            AND data_abertura BETWEEN ? AND ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao calcular média de avaliações", e);
        }
    }

    public Map<Chamado.StatusChamado, Long> countByStatus(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT status, COUNT(*) as total FROM chamados WHERE data_abertura BETWEEN ? AND ? GROUP BY status";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            Map<Chamado.StatusChamado, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(
                    Chamado.StatusChamado.valueOf(rs.getString("status")),
                    rs.getLong("total")
                );
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados por status", e);
        }
    }

    public Map<String, Long> countByCategoria(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT categoria, COUNT(*) as total FROM chamados WHERE data_abertura BETWEEN ? AND ? GROUP BY categoria";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            Map<String, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(rs.getString("categoria"), rs.getLong("total"));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados por categoria", e);
        }
    }

    public Map<LocalDate, Long> countByDia(LocalDateTime inicio, LocalDateTime fim) {
        String sql = "SELECT CAST(data_abertura AS DATE) as dia, COUNT(*) as total FROM chamados WHERE data_abertura BETWEEN ? AND ? GROUP BY CAST(data_abertura AS DATE)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fim));
            ResultSet rs = stmt.executeQuery();
            Map<LocalDate, Long> result = new HashMap<>();
            while (rs.next()) {
                result.put(
                    rs.getDate("dia").toLocalDate(),
                    rs.getLong("total")
                );
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar chamados por dia", e);
        }
    }

    public List<Chamado> findByTecnicoEPeriodo(Usuario tecnico, LocalDateTime inicio, LocalDateTime fim) {
        List<Chamado> chamados = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            WHERE c.tecnico_id = ? AND c.data_abertura BETWEEN ? AND ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, tecnico.getId());
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fim));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados por técnico e período", e);
        }
        return chamados;
    }
    
    public List<Chamado> findByCategoriaEPeriodo(String categoria, LocalDateTime inicio, LocalDateTime fim) {
        List<Chamado> chamados = new ArrayList<>();
        String sql = """
            SELECT c.*, 
                   s.name_user as solicitante_nome, s.email as solicitante_email,
                   s.type_user as solicitante_tipo,
                   t.name_user as tecnico_nome, t.email as tecnico_email,
                   t.type_user as tecnico_tipo
            FROM chamados c
            INNER JOIN usuarios s ON c.solicitante_id = s.id_user
            LEFT JOIN usuarios t ON c.tecnico_id = t.id_user
            WHERE c.categoria = ? AND c.data_abertura BETWEEN ? AND ?
        """;
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, categoria);
            stmt.setTimestamp(2, Timestamp.valueOf(inicio));
            stmt.setTimestamp(3, Timestamp.valueOf(fim));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    chamados.add(mapResultSetToChamado(rs));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar chamados por categoria e período", e);
        }
        return chamados;
    }

    public void delete(Long id) {
        String sql = "DELETE FROM chamados WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            stmt.executeUpdate();
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir chamado: " + e.getMessage(), e);
        }
    }
    
    public void deleteAll() {
        String sql = "DELETE FROM chamados";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao excluir todos os chamados: " + e.getMessage(), e);
        }
    }
} 