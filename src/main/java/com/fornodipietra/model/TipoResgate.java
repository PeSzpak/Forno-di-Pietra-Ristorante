package com.fornodipietra.model;

/**
 * Tipos de resgate disponíveis no programa Carta d'Oro.
 * Espelha o ENUM da coluna tipo_resgate na tabela resgate_fidelidade.
 */
public enum TipoResgate {

    DESCONTO("Desconto percentual"),
    PRATO_GRATIS("Prato grátis");

    private final String descricao;

    TipoResgate(String descricao) {
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
