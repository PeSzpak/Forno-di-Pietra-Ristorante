package com.fornodipietra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um resgate do programa de fidelidade Carta d'Oro.
 * Mapeado para a tabela: resgate_fidelidade
 *
 * Dois tipos possíveis:
 *  - DESCONTO:      desconto_aplicado preenchido, prato_id nulo
 *  - PRATO_GRATIS:  prato_id preenchido, desconto_aplicado nulo
 */
public class ResgatesFidelidade {

    private int           id;
    private int           clienteId;
    private Cliente       cliente;           // populado pelo DAO
    private TipoResgate   tipoResgate;
    private Integer       pratoId;           // nullable — só para PRATO_GRATIS
    private Prato         prato;             // populado pelo DAO quando não nulo
    private BigDecimal    descontoAplicado;  // nullable — só para DESCONTO
    private int           pontosUsados;
    private LocalDateTime dataResgate;

    // ─── Construtor vazio ─────────────────────────────────────────────────────

    public ResgatesFidelidade() {
        this.dataResgate = LocalDateTime.now();
    }

    // ─── Construtor para resgate de desconto ─────────────────────────────────

    public ResgatesFidelidade(int clienteId, BigDecimal descontoAplicado, int pontosUsados) {
        this();
        this.clienteId        = clienteId;
        this.tipoResgate      = TipoResgate.DESCONTO;
        this.descontoAplicado = descontoAplicado;
        this.pontosUsados     = pontosUsados;
    }

    // ─── Construtor para resgate de prato grátis ──────────────────────────────

    public ResgatesFidelidade(int clienteId, int pratoId, int pontosUsados) {
        this();
        this.clienteId    = clienteId;
        this.tipoResgate  = TipoResgate.PRATO_GRATIS;
        this.pratoId      = pratoId;
        this.pontosUsados = pontosUsados;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int           getId()                { return id; }
    public int           getClienteId()         { return clienteId; }
    public Cliente       getCliente()           { return cliente; }
    public TipoResgate   getTipoResgate()       { return tipoResgate; }
    public Integer       getPratoId()           { return pratoId; }
    public Prato         getPrato()             { return prato; }
    public BigDecimal    getDescontoAplicado()  { return descontoAplicado; }
    public int           getPontosUsados()      { return pontosUsados; }
    public LocalDateTime getDataResgate()       { return dataResgate; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                                  { this.id = id; }
    public void setClienteId(int clienteId)                     { this.clienteId = clienteId; }
    public void setCliente(Cliente cliente)                     { this.cliente = cliente; }
    public void setTipoResgate(TipoResgate tipoResgate)         { this.tipoResgate = tipoResgate; }
    public void setPratoId(Integer pratoId)                     { this.pratoId = pratoId; }
    public void setPrato(Prato prato)                           { this.prato = prato; }
    public void setDescontoAplicado(BigDecimal desconto)        { this.descontoAplicado = desconto; }
    public void setPontosUsados(int pontosUsados)               { this.pontosUsados = pontosUsados; }
    public void setDataResgate(LocalDateTime dataResgate)       { this.dataResgate = dataResgate; }

    @Override
    public String toString() {
        if (tipoResgate == TipoResgate.DESCONTO) {
            return String.format("Resgate{id=%d, clienteId=%d, tipo=DESCONTO, desconto=%s%%, pontos=%d}",
                    id, clienteId, descontoAplicado, pontosUsados);
        }
        return String.format("Resgate{id=%d, clienteId=%d, tipo=PRATO_GRATIS, pratoId=%d, pontos=%d}",
                id, clienteId, pratoId, pontosUsados);
    }
}
