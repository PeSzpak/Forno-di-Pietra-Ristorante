package com.fornodipietra.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa um prato do cardápio do Forno di Pietra.
 * Mapeado para a tabela: prato
 */
public class Prato {

    private int            id;
    private String         codigo;
    private String         nome;
    private BigDecimal     preco;
    private String         ingredientes;
    private CategoriaPrato categoria;
    private boolean        disponivel;
    private LocalDateTime  criadoEm;

    // ─── Construtor vazio (usado pelo DAO ao popular via ResultSet) ───────────

    public Prato() {}

    // ─── Construtor para criação (sem id, sem criadoEm — gerados pelo banco) ──

    public Prato(String codigo, String nome, BigDecimal preco,
                 String ingredientes, CategoriaPrato categoria, boolean disponivel) {
        this.codigo       = codigo;
        this.nome         = nome;
        this.preco        = preco;
        this.ingredientes = ingredientes;
        this.categoria    = categoria;
        this.disponivel   = disponivel;
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public int            getId()          { return id; }
    public String         getCodigo()      { return codigo; }
    public String         getNome()        { return nome; }
    public BigDecimal     getPreco()       { return preco; }
    public String         getIngredientes(){ return ingredientes; }
    public CategoriaPrato getCategoria()   { return categoria; }
    public boolean        isDisponivel()   { return disponivel; }
    public LocalDateTime  getCriadoEm()   { return criadoEm; }

    // ─── Setters ──────────────────────────────────────────────────────────────

    public void setId(int id)                        { this.id = id; }
    public void setCodigo(String codigo)              { this.codigo = codigo; }
    public void setNome(String nome)                  { this.nome = nome; }
    public void setPreco(BigDecimal preco)            { this.preco = preco; }
    public void setIngredientes(String ingredientes)  { this.ingredientes = ingredientes; }
    public void setCategoria(CategoriaPrato categoria){ this.categoria = categoria; }
    public void setDisponivel(boolean disponivel)     { this.disponivel = disponivel; }
    public void setCriadoEm(LocalDateTime criadoEm)  { this.criadoEm = criadoEm; }

    // ─── toString para debug e JTable ────────────────────────────────────────

    @Override
    public String toString() {
        return String.format("Prato{id=%d, codigo='%s', nome='%s', preco=%s, categoria=%s, disponivel=%b}",
                id, codigo, nome, preco, categoria, disponivel);
    }
}
