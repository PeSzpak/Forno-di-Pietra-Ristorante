package com.fornodipietra.model;

import java.time.LocalDateTime;

/**
 * Representa o saldo de fidelidade de um cliente no programa Carta d'Oro.
 * Mapeado para a tabela: fidelidade
 *
 * Relação 1:1 com Cliente.
 * Criado automaticamente pelo FidelidadeService ao cadastrar um novo cliente.
 */
public class Fidelidade {

    private int            id;
    private int            clienteId;
    private Cliente        cliente;        // objeto completo — populado pelo DAO
    private int            pontosAcumulados;
    private TierFidelidade tier;
    private LocalDateTime  atualizadoEm;

    // ─── Construtor vazio ─────────────────────────────────────────────────────

    public Fidelidade() {
        this.tier             = TierFidelidade.BRONZE;
        this.pontosAcumulados = 0;
    }

    // ─── Construtor para criação (novo cliente) ───────────────────────────────

    public Fidelidade(int clienteId) {
        this();
        this.clienteId = clienteId;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int            getId()                { return id; }
    public int            getClienteId()         { return clienteId; }
    public Cliente        getCliente()           { return cliente; }
    public int            getPontosAcumulados()  { return pontosAcumulados; }
    public TierFidelidade getTier()              { return tier; }
    public LocalDateTime  getAtualizadoEm()     { return atualizadoEm; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                                  { this.id = id; }
    public void setClienteId(int clienteId)                     { this.clienteId = clienteId; }
    public void setCliente(Cliente cliente)                     { this.cliente = cliente; }
    public void setPontosAcumulados(int pontosAcumulados)       { this.pontosAcumulados = pontosAcumulados; }
    public void setTier(TierFidelidade tier)                    { this.tier = tier; }
    public void setAtualizadoEm(LocalDateTime atualizadoEm)    { this.atualizadoEm = atualizadoEm; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Adiciona pontos e recalcula o tier automaticamente.
     * Chamado pelo FidelidadeService após finalizar um pedido.
     *
     * @param pontos quantidade de pontos a adicionar (R$1 = 1 ponto)
     */
    public void adicionarPontos(int pontos) {
        this.pontosAcumulados += pontos;
        this.tier = TierFidelidade.calcularTier(this.pontosAcumulados);
    }

    /**
     * Desconta pontos após um resgate.
     * Não deixa o saldo negativo.
     */
    public void descontarPontos(int pontos) {
        this.pontosAcumulados = Math.max(0, this.pontosAcumulados - pontos);
        this.tier = TierFidelidade.calcularTier(this.pontosAcumulados);
    }

    /** Pontos necessários para atingir o próximo tier. */
    public int pontosParaProximoTier() {
        if (tier == TierFidelidade.OURO) return 0;
        TierFidelidade[] tiers = TierFidelidade.values();
        int proximoMinimo = tiers[tier.ordinal() + 1].getPontoMinimo();
        return Math.max(0, proximoMinimo - pontosAcumulados);
    }

    @Override
    public String toString() {
        return String.format("Fidelidade{clienteId=%d, pontos=%d, tier=%s}",
                clienteId, pontosAcumulados, tier);
    }
}
