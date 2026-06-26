package com.fornodipietra.dao;

import com.fornodipietra.config.DatabaseConnection;
import com.fornodipietra.model.Cliente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por todas as operações de banco da tabela cliente.
 */
public class ClienteDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    public void inserir(Cliente cliente) {
        String sql = "INSERT INTO cliente (nome, telefone, email) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getTelefone());
            ps.setString(3, cliente.getEmail());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                cliente.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir cliente: " + e.getMessage(), e);
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public Cliente buscarPorId(int id) {
        String sql = "SELECT * FROM cliente WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por id: " + e.getMessage(), e);
        }
        return null;
    }

    public Cliente buscarPorEmail(String email) {
        String sql = "SELECT * FROM cliente WHERE email = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapear(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por email: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Cliente> listarTodos() {
        String sql = "SELECT * FROM cliente ORDER BY nome";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Relatório: clientes mais ativos ordenados por total de pedidos. */
    public List<Cliente> listarMaisAtivos() {
        String sql = "SELECT * FROM cliente WHERE total_pedidos > 0 ORDER BY total_pedidos DESC";
        List<Cliente> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes mais ativos: " + e.getMessage(), e);
        }
        return lista;
    }

    public boolean emailJaExiste(String email, int idIgnorar) {
        String sql = "SELECT COUNT(*) FROM cliente WHERE email = ? AND id != ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setInt(2, idIgnorar);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar email: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean possuiPedidos(int clienteId) {
        String sql = "SELECT COUNT(*) FROM pedido WHERE cliente_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao verificar pedidos do cliente: " + e.getMessage(), e);
        }
        return false;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public void atualizar(Cliente cliente) {
        String sql = "UPDATE cliente SET nome = ?, telefone = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, cliente.getNome());
            ps.setString(2, cliente.getTelefone());
            ps.setString(3, cliente.getEmail());
            ps.setInt(4, cliente.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cliente: " + e.getMessage(), e);
        }
    }

    public void incrementarTotalPedidos(int clienteId) {
        String sql = "UPDATE cliente SET total_pedidos = total_pedidos + 1 WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao incrementar total de pedidos: " + e.getMessage(), e);
        }
    }

    // ─── DELETE ───────────────────────────────────────────────────────────────

    public void excluir(int id) {
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao excluir cliente: " + e.getMessage(), e);
        }
    }

    // ─── MAPEAMENTO ResultSet → Cliente ───────────────────────────────────────

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id"));
        c.setNome(rs.getString("nome"));
        c.setTelefone(rs.getString("telefone"));
        c.setEmail(rs.getString("email"));
        c.setTotalPedidos(rs.getInt("total_pedidos"));

        Timestamp ts = rs.getTimestamp("criado_em");
        if (ts != null) {
            c.setCriadoEm(ts.toLocalDateTime());
        }
        return c;
    }
}
