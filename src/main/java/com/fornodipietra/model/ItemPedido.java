package com.fornodipietra.model;

import java.math.BigDecimal;

/**
 * Representa um item dentro de um pedido.
 * Mapeado para a tabela: item_pedido
 *
 * precoUnitario é gravado no momento do pedido para preservar
 * o histórico mesmo que o preço do prato seja alterado depois.
 */
public class ItemPedido {

    private int        id;
    private int        pedidoId;
    private int        pratoId;
    private Prato      prato;        // objeto completo — populado pelo DAO
    private int        quantidade;
    private BigDecimal precoUnitario;

    // ─── Construtor vazio ─────────────────────────────────────────────────────

    public ItemPedido() {}

    // ─── Construtor para criação ──────────────────────────────────────────────

    public ItemPedido(int pratoId, int quantidade, BigDecimal precoUnitario) {
        this.pratoId       = pratoId;
        this.quantidade    = quantidade;
        this.precoUnitario = precoUnitario;
    }

    // ─── Construtor com objeto Prato (conveniência na UI) ─────────────────────

    public ItemPedido(Prato prato, int quantidade) {
        this.prato         = prato;
        this.pratoId       = prato.getId();
        this.quantidade    = quantidade;
        this.precoUnitario = prato.getPreco();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int        getId()            { return id; }
    public int        getPedidoId()      { return pedidoId; }
    public int        getPratoId()       { return pratoId; }
    public Prato      getPrato()         { return prato; }
    public int        getQuantidade()    { return quantidade; }
    public BigDecimal getPrecoUnitario() { return precoUnitario; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                          { this.id = id; }
    public void setPedidoId(int pedidoId)               { this.pedidoId = pedidoId; }
    public void setPratoId(int pratoId)                 { this.pratoId = pratoId; }
    public void setPrato(Prato prato)                   { this.prato = prato; }
    public void setQuantidade(int quantidade)           { this.quantidade = quantidade; }
    public void setPrecoUnitario(BigDecimal preco)      { this.precoUnitario = preco; }

    // ─── Helper ───────────────────────────────────────────────────────────────

    /** Subtotal deste item = precoUnitario * quantidade. */
    public BigDecimal getSubtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    @Override
    public String toString() {
        String nomePrato = (prato != null) ? prato.getNome() : "pratoId=" + pratoId;
        return String.format("ItemPedido{id=%d, prato='%s', qtd=%d, precoUnit=%s, subtotal=%s}",
                id, nomePrato, quantidade, precoUnitario, getSubtotal());
    }
}
