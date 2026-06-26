package com.fornodipietra.dao;

import com.fornodipietra.config.DatabaseConnection;
import com.fornodipietra.model.CategoriaPrato;
import com.fornodipietra.model.Prato;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por todas as operações de banco da tabela prato.
 * Usa SQL puro via JDBC — sem ORM.
 */
public class PratoDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public void inserir(Prato prato) {
        String sql = "INSERT INTO prato (codigo, nome, preco, ingredientes, categoria, disponivel) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, prato.getCodigo());
            ps.setString(2, prato.getNome());
            ps.setBigDecimal(3, prato.getPreco());
            ps.setString(4, prato.getIngredientes());
            ps.setString(5, prato.getCategoria().name());
            ps.setBoolean(6, prato.isDisponivel());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                prato.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir prato: " + e.getMessage(), e);
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public Prato buscarPorId(int id) {
        String sql = "SELECT * FROM prato WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar prato por id: " + e.getMessage(), e);
        }
        return null;
    }

    public Prato buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM prato WHERE codigo = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapear(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar prato por código: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Prato> listarTodos() {
        String sql = "SELECT * FROM prato ORDER BY categoria, nome";
        List<Prato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Prato> listarDisponiveis() {
        String sql = "SELECT * FROM prato WHERE disponivel = TRUE ORDER BY categoria, nome";
        List<Prato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos disponíveis: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Prato> listarPorCategoria(CategoriaPrato categoria) {
        String sql = "SELECT * FROM prato WHERE categoria = ? ORDER BY nome";
        List<Prato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, categoria.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos por categoria: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Prato> listarIndisponiveis() {
        String sql = "SELECT * FROM prato WHERE disponivel = FALSE ORDER BY categoria, nome";
        List<Prato> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos indisponíveis: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Relatório: pratos mais vendidos por quantidade de itens em pedidos finalizados. */
    public List<Object[]> listarMaisVendidos() {
        String sql = "SELECT p.nome, p.categoria, SUM(ip.quantidade) AS total_vendido "
                   + "FROM item_pedido ip "
                   + "JOIN prato p ON ip.prato_id = p.id "
                   + "JOIN pedido pe ON ip.pedido_id = pe.id "
                   + "WHERE pe.status = 'FINALIZADO' "
                   + "GROUP BY p.id, p.nome, p.categoria "
                   + "ORDER BY total_vendido DESC";
        List<Object[]> resultado = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Object[] linha = {
                    rs.getString("nome"),
                    rs.getString("categoria"),
                    rs.getInt("total_vendido")
                };
                resultado.add(linha);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pratos mais vendidos: " + e.getMessage(), e);
        }
        return resultado;
    }

    public boolean codigoJaExiste(String codigo, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM prato WHERE codigo = ? AND id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, idIgnorar);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar código: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean possuiPedidos(int pratoId) {
        String sql = "SELECT COUNT(*) FROM item_pedido WHERE prato_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, pratoId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar pedidos do prato: " + e.getMessage(), e);
        }
        return false;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public void atualizar(Prato prato) {
        String sql = "UPDATE prato SET codigo = ?, nome = ?, preco = ?, ingredientes = ?, "
                   + "categoria = ?, disponivel = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, prato.getCodigo());
            ps.setString(2, prato.getNome());
            ps.setBigDecimal(3, prato.getPreco());
            ps.setString(4, prato.getIngredientes());
            ps.setString(5, prato.getCategoria().name());
            ps.setBoolean(6, prato.isDisponivel());
            ps.setInt(7, prato.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar prato: " + e.getMessage(), e);
        }
    }

    public void atualizarDisponibilidade(int id, boolean disponivel) {
        String sql = "UPDATE prato SET disponivel = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBoolean(1, disponivel);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar disponibilidade: " + e.getMessage(), e);
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public void excluir(int id) {
        String sql = "DELETE FROM prato WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir prato: " + e.getMessage(), e);
        }
    }

    // ─── MAPEAMENTO ResultSet → Prato ─────────────────────────────────────────

    private Prato mapear(ResultSet rs) throws SQLException {
        Prato p = new Prato();
        p.setId(rs.getInt("id"));
        p.setCodigo(rs.getString("codigo"));
        p.setNome(rs.getString("nome"));
        p.setPreco(rs.getBigDecimal("preco"));
        p.setIngredientes(rs.getString("ingredientes"));
        p.setCategoria(CategoriaPrato.valueOf(rs.getString("categoria")));
        p.setDisponivel(rs.getBoolean("disponivel"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) {
            p.setCriadoEm(ts.toLocalDateTime());
        }
        return p;
    }
}
