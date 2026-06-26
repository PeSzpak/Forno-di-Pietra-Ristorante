package com.fornodipietra.dao;

import com.fornodipietra.config.DatabaseConnection;
import com.fornodipietra.model.ItemPedido;
import com.fornodipietra.model.Pedido;
import com.fornodipietra.model.StatusPedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Responsável por todas as operações de banco das tabelas pedido e item_pedido.
 * A inserção de um pedido é feita em transação: insere o pedido e todos os itens
 * de uma só vez, garantindo consistência.
 */
public class PedidoDAO {

    private Connection getConn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ─── CREATE ───────────────────────────────────────────────────────────────

    /**
     * Insere o pedido e todos os seus itens em uma única transação.
     * Em caso de falha em qualquer item, tudo é revertido (rollback).
     */
    public void inserir(Pedido pedido) {
        String sqlPedido = "INSERT INTO pedido (cliente_id, data_hora, valor_total, status, observacao) "
                         + "VALUES (?, ?, ?, ?, ?)";
        String sqlItem   = "INSERT INTO item_pedido (pedido_id, prato_id, quantidade, preco_unitario) "
                         + "VALUES (?, ?, ?, ?)";

        Connection conn = getConn();
        try {
            conn.setAutoCommit(false);

            // 1. Insere o pedido
            try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, pedido.getClienteId());
                ps.setTimestamp(2, Timestamp.valueOf(pedido.getDataHora()));
                ps.setBigDecimal(3, pedido.getValorTotal());
                ps.setString(4, pedido.getStatus().name());
                ps.setString(5, pedido.getObservacao());
                ps.executeUpdate();

                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) {
                    pedido.setId(keys.getInt(1));
                }
            }

            // 2. Insere cada item
            try (PreparedStatement ps = conn.prepareStatement(sqlItem, Statement.RETURN_GENERATED_KEYS)) {
                for (ItemPedido item : pedido.getItens()) {
                    ps.setInt(1, pedido.getId());
                    ps.setInt(2, item.getPratoId());
                    ps.setInt(3, item.getQuantidade());
                    ps.setBigDecimal(4, item.getPrecoUnitario());
                    ps.executeUpdate();

                    ResultSet keys = ps.getGeneratedKeys();
                    if (keys.next()) {
                        item.setId(keys.getInt(1));
                    }
                    item.setPedidoId(pedido.getId());
                }
            }

            conn.commit();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) { /* ignora erro de rollback */ }
            throw new RuntimeException("Erro ao inserir pedido (transação revertida): " + e.getMessage(), e);
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) { /* ignora */ }
        }
    }

    // ─── READ ─────────────────────────────────────────────────────────────────

    public Pedido buscarPorId(int id) {
        String sql = "SELECT * FROM pedido WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Pedido pedido = mapear(rs);
                pedido.setItens(buscarItensPorPedido(pedido.getId()));
                return pedido;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido: " + e.getMessage(), e);
        }
        return null;
    }

    public List<Pedido> listarTodos() {
        String sql = "SELECT * FROM pedido ORDER BY data_hora DESC";
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<Pedido> listarPorCliente(int clienteId) {
        String sql = "SELECT * FROM pedido WHERE cliente_id = ? ORDER BY data_hora DESC";
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Pedido pedido = mapear(rs);
                pedido.setItens(buscarItensPorPedido(pedido.getId()));
                lista.add(pedido);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos do cliente: " + e.getMessage(), e);
        }
        return lista;
    }

    /** Relatório: faturamento total entre duas datas (apenas pedidos FINALIZADOS). */
    public List<Pedido> listarPorPeriodo(String dataInicio, String dataFim) {
        String sql = "SELECT * FROM pedido "
                   + "WHERE status = 'FINALIZADO' "
                   + "AND DATE(data_hora) BETWEEN ? AND ? "
                   + "ORDER BY data_hora DESC";
        List<Pedido> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, dataInicio);
            ps.setString(2, dataFim);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos por período: " + e.getMessage(), e);
        }
        return lista;
    }

    public List<ItemPedido> buscarItensPorPedido(int pedidoId) {
        String sql = "SELECT ip.*, p.nome AS prato_nome, p.codigo AS prato_codigo, "
                   + "p.categoria AS prato_categoria, p.disponivel AS prato_disponivel, "
                   + "p.ingredientes AS prato_ingredientes "
                   + "FROM item_pedido ip "
                   + "JOIN prato p ON ip.prato_id = p.id "
                   + "WHERE ip.pedido_id = ?";
        List<ItemPedido> lista = new ArrayList<>();
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, pedidoId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearItem(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar itens do pedido: " + e.getMessage(), e);
        }
        return lista;
    }

    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public void atualizarStatus(int pedidoId, StatusPedido novoStatus) {
        String sql = "UPDATE pedido SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, novoStatus.name());
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar status do pedido: " + e.getMessage(), e);
        }
    }

    public void atualizarValorTotal(int pedidoId, java.math.BigDecimal valorTotal) {
        String sql = "UPDATE pedido SET valor_total = ? WHERE id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setBigDecimal(1, valorTotal);
            ps.setInt(2, pedidoId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar valor total do pedido: " + e.getMessage(), e);
        }
    }

    // ─── MAPEAMENTO ResultSet → Pedido ────────────────────────────────────────

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id"));
        p.setClienteId(rs.getInt("cliente_id"));
        p.setValorTotal(rs.getBigDecimal("valor_total"));
        p.setStatus(StatusPedido.valueOf(rs.getString("status")));
        p.setObservacao(rs.getString("observacao"));

        Timestamp ts = rs.getTimestamp("data_hora");
        if (ts != null) {
            p.setDataHora(ts.toLocalDateTime());
        }
        return p;
    }

    private ItemPedido mapearItem(ResultSet rs) throws SQLException {
        ItemPedido item = new ItemPedido();
        item.setId(rs.getInt("id"));
        item.setPedidoId(rs.getInt("pedido_id"));
        item.setPratoId(rs.getInt("prato_id"));
        item.setQuantidade(rs.getInt("quantidade"));
        item.setPrecoUnitario(rs.getBigDecimal("preco_unitario"));

        // Monta objeto Prato simplificado para exibição na fatura
        com.fornodipietra.model.Prato prato = new com.fornodipietra.model.Prato();
        prato.setId(rs.getInt("prato_id"));
        prato.setNome(rs.getString("prato_nome"));
        prato.setCodigo(rs.getString("prato_codigo"));
        prato.setCategoria(com.fornodipietra.model.CategoriaPrato.valueOf(rs.getString("prato_categoria")));
        prato.setDisponivel(rs.getBoolean("prato_disponivel"));
        prato.setIngredientes(rs.getString("prato_ingredientes"));
        prato.setPreco(rs.getBigDecimal("preco_unitario"));
        item.setPrato(prato);

        return item;
    }
}
