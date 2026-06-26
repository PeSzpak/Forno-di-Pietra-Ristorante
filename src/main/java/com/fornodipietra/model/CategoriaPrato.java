package com.fornodipietra.model;

/**
 * Categorias de prato disponíveis no restaurante.
 * Espelha o ENUM da coluna categoria na tabela prato.
 */
public enum CategoriaPrato {

    ENTRADA("Entrada"),
    PRATO_PRINCIPAL("Prato Principal"),
    SOBREMESA("Sobremesa"),
    BEBIDA("Bebida");

    private final String descricao;

    CategoriaPrato(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Retorna a descrição legível nos JComboBox e tabelas. */
    @Override
    public String toString() {
        return descricao;
    }
}
