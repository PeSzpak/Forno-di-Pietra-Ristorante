package com.fornodipietra.model;

import java.time.LocalDateTime;

/**
 * Representa um cliente do Forno di Pietra.
 * Mapeado para a tabela: cliente
 *
 * Nota: totalPedidos é um campo interno de controle — não exibido ao cliente,
 * mas usado nos relatórios de clientes mais ativos.
 */
public class Cliente {

    private int           id;
    private String        nome;
    private String        telefone;
    private String        email;
    private int           totalPedidos;
    private LocalDateTime criadoEm;

    // ─── Construtor vazio ─────────────────────────────────────────────────────

    public Cliente() {}

    // ─── Construtor para criação (sem id e sem totalPedidos — gerados pelo banco) ──

    public Cliente(String nome, String telefone, String email) {
        this.nome     = nome;
        this.telefone = telefone;
        this.email    = email;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int           getId()           { return id; }
    public String        getNome()         { return nome; }
    public String        getTelefone()     { return telefone; }
    public String        getEmail()        { return email; }
    public int           getTotalPedidos() { return totalPedidos; }
    public LocalDateTime getCriadoEm()    { return criadoEm; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                       { this.id = id; }
    public void setNome(String nome)                 { this.nome = nome; }
    public void setTelefone(String telefone)         { this.telefone = telefone; }
    public void setEmail(String email)               { this.email = email; }
    public void setTotalPedidos(int totalPedidos)    { this.totalPedidos = totalPedidos; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return String.format("Cliente{id=%d, nome='%s', email='%s', totalPedidos=%d}",
                id, nome, email, totalPedidos);
    }
}
