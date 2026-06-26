package com.fornodipietra.model;

/**
 * Status possíveis de um pedido.
 * Espelha o ENUM da coluna status na tabela pedido.
 *
 * Fluxo válido: ABERTO → FINALIZADO
 *               ABERTO → CANCELADO
 * Fluxo inválido: FINALIZADO → qualquer coisa
 *                 CANCELADO  → qualquer coisa
 */
public enum StatusPedido {

    ABERTO("Aberto"),
    FINALIZADO("Finalizado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
