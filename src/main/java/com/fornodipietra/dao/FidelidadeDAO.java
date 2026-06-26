package com.fornodipietra.dao;

import com.fornodipietra.config.DatabaseConnection;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.ResgatesFidelidade;
import com.fornodipietra.model.TierFidelidade;
import com.fornodipietra.model.TipoResgate;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por todas as operações de banco das tabelas
 * fidelidade e resgate_fidelidade (programa Carta d'Oro).
 */
public class FidelidadeDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── FIDELIDADE — CREATE ──────────────────────────────────────────────────

    /** Cria o registro de fidelidade para um novo cliente (sempre Bronze, 0 pontos). */
    public void criarParaCliente(int clienteId) {
        String sql = "INSERT INTO fidelidade (cliente_id, pontos_acumulados, tier) VALUES (?, 0, 'BRONZE')";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao criar fidelidade para cliente: " + e.getMessage(), e);
        }
    }

    // ─── FIDELIDADE — READ ────────────────────────────────────────────────────

    public Fidelidade buscarPorCliente(int clienteId) {
        String sql = "SELECT * FROM fidelidade WHERE cliente_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar fidelidade do cliente: " + e.getMessage(), e);
        }
        return null;
    }

    /** Relatório: distribuição de clientes por tier. */
    public List<Object[]> contarPorTier() {
        String sql = "SELECT tier, COUNT(*) AS total FROM fidelidade GROUP BY tier ORDER BY tier";
        List<Object[]> resultado = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] linha = { rs.getString("tier"), rs.getInt("total") };
                resultado.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao contar clientes por tier: " + e.getMessage(), e);
        }
        return resultado;
    }

    /** Relatório: clientes por tier com nome — para a tela de fidelidade. */
    public List<Object[]> listarClientesPorTier(TierFidelidade tier) {
        String sql = "SELECT c.nome, c.email, f.pontos_acumulados, f.tier "
                   + "FROM fidelidade f "
                   + "JOIN cliente c ON f.cliente_id = c.id "
                   + "WHERE f.tier = ? "
                   + "ORDER BY f.pontos_acumulados DESC";
        List<Object[]> resultado = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, tier.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] linha = {
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getInt("pontos_acumulados"),
                    rs.getString("tier")
                };
                resultado.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes por tier: " + e.getMessage(), e);
        }
        return resultado;
    }

    // ─── FIDELIDADE — UPDATE ──────────────────────────────────────────────────

    public void atualizarPontosETier(int clienteId, int pontosAcumulados, TierFidelidade tier) {
        String sql = "UPDATE fidelidade SET pontos_acumulados = ?, tier = ? WHERE cliente_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, pontosAcumulados);
            ps.setString(2, tier.name());
            ps.setInt(3, clienteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pontos e tier: " + e.getMessage(), e);
        }
    }

    // ─── RESGATES — CREATE ────────────────────────────────────────────────────

    public void registrarResgate(ResgatesFidelidade resgate) {
        String sql = "INSERT INTO resgate_fidelidade "
                   + "(cliente_id, tipo_resgate, prato_id, desconto_aplicado, pontos_usados, data_resgate) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, resgate.getClienteId());
            ps.setString(2, resgate.getTipoResgate().name());

            if (resgate.getPratoId() != null) {
                ps.setInt(3, resgate.getPratoId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (resgate.getDescontoAplicado() != null) {
                ps.setBigDecimal(4, resgate.getDescontoAplicado());
            } else {
                ps.setNull(4, Types.DECIMAL);
            }

            ps.setInt(5, resgate.getPontosUsados());
            ps.setTimestamp(6, Timestamp.valueOf(resgate.getDataResgate()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                resgate.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao registrar resgate: " + e.getMessage(), e);
        }
    }

    // ─── RESGATES — READ ──────────────────────────────────────────────────────

    public List<ResgatesFidelidade> listarResgatesPorCliente(int clienteId) {
        String sql = "SELECT * FROM resgate_fidelidade WHERE cliente_id = ? ORDER BY data_resgate DESC";
        List<ResgatesFidelidade> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResgate(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar resgates do cliente: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Relatório: histórico de resgates entre duas datas. */
    public List<Object[]> listarResgatesPorPeriodo(String dataInicio, String dataFim) {
        String sql = "SELECT c.nome AS cliente_nome, r.tipo_resgate, "
                   + "p.nome AS prato_nome, r.desconto_aplicado, r.pontos_usados, r.data_resgate "
                   + "FROM resgate_fidelidade r "
                   + "JOIN cliente c ON r.cliente_id = c.id "
                   + "LEFT JOIN prato p ON r.prato_id = p.id "
                   + "WHERE DATE(r.data_resgate) BETWEEN ? AND ? "
                   + "ORDER BY r.data_resgate DESC";
        List<Object[]> resultado = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dataInicio);
            ps.setString(2, dataFim);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] linha = {
                    rs.getString("cliente_nome"),
                    rs.getString("tipo_resgate"),
                    rs.getString("prato_nome"),
                    rs.getBigDecimal("desconto_aplicado"),
                    rs.getInt("pontos_usados"),
                    rs.getTimestamp("data_resgate")
                };
                resultado.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar resgates por período: " + e.getMessage(), e);
        }
        return resultado;
    }

    // ─── MAPEAMENTO ───────────────────────────────────────────────────────────

    private Fidelidade mapear(ResultSet rs) throws SQLException {
        Fidelidade f = new Fidelidade();
        f.setId(rs.getInt("id"));
        f.setClienteId(rs.getInt("cliente_id"));
        f.setPontosAcumulados(rs.getInt("pontos_acumulados"));
        f.setTier(TierFidelidade.valueOf(rs.getString("tier")));

        Timestamp ts = rs.getTimestamp("atualizado_em");
        if (ts != null) {
            f.setAtualizadoEm(ts.toLocalDateTime());
        }
        return f;
    }

    private ResgatesFidelidade mapearResgate(ResultSet rs) throws SQLException {
        ResgatesFidelidade r = new ResgatesFidelidade();
        r.setId(rs.getInt("id"));
        r.setClienteId(rs.getInt("cliente_id"));
        r.setTipoResgate(TipoResgate.valueOf(rs.getString("tipo_resgate")));
        r.setPontosUsados(rs.getInt("pontos_usados"));

        int pratoId = rs.getInt("prato_id");
        if (!rs.wasNull()) {
            r.setPratoId(pratoId);
        }

        java.math.BigDecimal desconto = rs.getBigDecimal("desconto_aplicado");
        if (desconto != null) {
            r.setDescontoAplicado(desconto);
        }

        Timestamp ts = rs.getTimestamp("data_resgate");
        if (ts != null) {
            r.setDataResgate(ts.toLocalDateTime());
        }
        return r;
    }
}
