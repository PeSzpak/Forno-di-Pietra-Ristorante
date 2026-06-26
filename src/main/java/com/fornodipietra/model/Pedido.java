package com.fornodipietra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa um pedido feito por um cliente no Forno di Pietra.
 * Mapeado para a tabela: pedido
 *
 * Agrega uma lista de ItemPedido (relação 1:N).
 * O valorTotal é calculado pelo PedidoService com base nos itens.
 */
public class Pedido {

    private int           id;
    private int           clienteId;
    private Cliente       cliente;       // objeto completo — populado pelo DAO quando necessário
    private LocalDateTime dataHora;
    private BigDecimal    valorTotal;
    private StatusPedido  status;
    private String        observacao;

    private List<ItemPedido> itens;     // itens do pedido (agregado)

    // ─── Construtor vazio ─────────────────────────────────────────────────────

    public Pedido() {
        this.itens      = new ArrayList<>();
        this.status     = StatusPedido.ABERTO;
        this.valorTotal = BigDecimal.ZERO;
        this.dataHora   = LocalDateTime.now();
    }

    // ─── Construtor para criação ──────────────────────────────────────────────

    public Pedido(int clienteId, String observacao) {
        this();
        this.clienteId  = clienteId;
        this.observacao = observacao;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int              getId()         { return id; }
    public int              getClienteId()  { return clienteId; }
    public Cliente          getCliente()    { return cliente; }
    public LocalDateTime    getDataHora()   { return dataHora; }
    public BigDecimal       getValorTotal() { return valorTotal; }
    public StatusPedido     getStatus()     { return status; }
    public String           getObservacao() { return observacao; }
    public List<ItemPedido> getItens()      { return itens; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                          { this.id = id; }
    public void setClienteId(int clienteId)             { this.clienteId = clienteId; }
    public void setCliente(Cliente cliente)             { this.cliente = cliente; }
    public void setDataHora(LocalDateTime dataHora)     { this.dataHora = dataHora; }
    public void setValorTotal(BigDecimal valorTotal)    { this.valorTotal = valorTotal; }
    public void setStatus(StatusPedido status)          { this.status = status; }
    public void setObservacao(String observacao)        { this.observacao = observacao; }
    public void setItens(List<ItemPedido> itens)        { this.itens = itens; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /** Adiciona um item à lista e recalcula o valor total. */
    public void adicionarItem(ItemPedido item) {
        this.itens.add(item);
        recalcularTotal();
    }

    /** Recalcula valorTotal somando preco_unitario * quantidade de cada item. */
    public void recalcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemPedido item : itens) {
            BigDecimal subtotal = item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade()));
            total = total.add(subtotal);
        }
        this.valorTotal = total;
    }

    /** Retorna true se o pedido ainda pode ser cancelado. */
    public boolean podeCancelar() {
        return this.status == StatusPedido.ABERTO;
    }

    /** Retorna true se o pedido ainda pode ser finalizado. */
    public boolean podeFinalizar() {
        return this.status == StatusPedido.ABERTO && !this.itens.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Pedido{id=%d, clienteId=%d, status=%s, valorTotal=%s, itens=%d}",
                id, clienteId, status, valorTotal, itens.size());
    }
}
