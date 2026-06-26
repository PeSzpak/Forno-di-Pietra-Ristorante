package com.fornodipietra.model;

/**
 * Tiers do programa de fidelidade Carta d'Oro.
 * Espelha o ENUM da coluna tier na tabela fidelidade.
 *
 * Regras de progressão:
 *   BRONZE: 0    – 499  pontos
 *   PRATA:  500  – 1499 pontos
 *   OURO:   1500+        pontos
 */
public enum TierFidelidade {

    BRONZE("🥉 Bronze",    0,    499,  0.0),
    PRATA ("🥈 Prata",   500,  1499,  5.0),
    OURO  ("🥇 Ouro",   1500, Integer.MAX_VALUE, 10.0);

    private final String descricao;
    private final int pontoMinimo;
    private final int pontoMaximo;
    private final double percentualDesconto;

    TierFidelidade(String descricao, int pontoMinimo, int pontoMaximo, double percentualDesconto) {
        this.descricao           = descricao;
        this.pontoMinimo         = pontoMinimo;
        this.pontoMaximo         = pontoMaximo;
        this.percentualDesconto  = percentualDesconto;
    }

    public String getDescricao()          { return descricao; }
    public int    getPontoMinimo()        { return pontoMinimo; }
    public int    getPontoMaximo()        { return pontoMaximo; }
    public double getPercentualDesconto() { return percentualDesconto; }

    /** Retorna se este tier permite resgate de benefício. */
    public boolean podeResgatar() {
        return this == PRATA || this == OURO;
    }

    /** Retorna se este tier permite prato grátis (exclusivo Ouro). */
    public boolean podeResgatarPratoGratis() {
        return this == OURO;
    }

    /**
     * Calcula o tier correspondente a uma quantidade de pontos.
     * Usado pelo FidelidadeService ao recalcular após cada pedido.
     */
    public static TierFidelidade calcularTier(int pontos) {
        if (pontos >= OURO.pontoMinimo)  return OURO;
        if (pontos >= PRATA.pontoMinimo) return PRATA;
        return BRONZE;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
