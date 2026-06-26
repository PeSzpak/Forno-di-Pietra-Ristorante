package com.fornodipietra.ui;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de cores e fontes centralizadas do Forno di Pietra.
 *
 * Inspirada nas cores da bandeira italiana e na estética rústica
 * de um restaurante a lenha: vermelho vibrante, verde escuro e creme.
 *
 * Usar estas constantes em vez de Color() espalhados pela UI garante
 * que qualquer mudança de tema seja feita em um único lugar.
 */
public class AppColors {

    // ─── Cores principais ─────────────────────────────────────────────────────

    /** Vermelho italiano — botões primários, cabeçalho */
    public static final Color VERMELHO        = new Color(180, 30, 30);

    /** Vermelho escuro — hover e bordas de destaque */
    public static final Color VERMELHO_ESCURO = new Color(130, 20, 20);

    /** Verde italiano — botões secundários, ícones de sucesso */
    public static final Color VERDE           = new Color(34, 100, 54);

    /** Verde escuro — hover de botões secundários */
    public static final Color VERDE_ESCURO    = new Color(20, 70, 35);

    /** Creme — fundo geral da aplicação */
    public static final Color CREME           = new Color(252, 248, 240);

    /** Bege — fundo dos painéis de conteúdo */
    public static final Color BEGE            = new Color(245, 238, 220);

    /** Marrom madeira — bordas e separadores */
    public static final Color MARROM          = new Color(120, 72, 30);

    /** Branco puro — fundo de formulários e tabelas */
    public static final Color BRANCO          = Color.WHITE;

    /** Cinza claro — linhas de tabela alternadas */
    public static final Color CINZA_CLARO     = new Color(240, 235, 225);

    /** Cinza texto — texto secundário e placeholders */
    public static final Color CINZA_TEXTO     = new Color(100, 90, 80);

    /** Texto principal — quase preto, mais suave que preto puro */
    public static final Color TEXTO           = new Color(40, 30, 20);

    // ─── Cores de feedback ────────────────────────────────────────────────────

    /** Verde sucesso — confirmações e mensagens positivas */
    public static final Color SUCESSO         = new Color(40, 120, 60);

    /** Vermelho erro — mensagens de erro */
    public static final Color ERRO            = new Color(180, 30, 30);

    /** Amarelo aviso — alertas e avisos */
    public static final Color AVISO           = new Color(200, 150, 0);

    // ─── Cores dos tiers da Carta d'Oro ──────────────────────────────────────

    public static final Color BRONZE          = new Color(150, 90, 40);
    public static final Color PRATA           = new Color(120, 120, 130);
    public static final Color OURO            = new Color(180, 140, 0);

    // ─── Fontes ───────────────────────────────────────────────────────────────

    /** Fonte padrão do sistema — usada como base */
    private static final String FAMILIA = "SansSerif";

    public static final Font FONTE_TITULO     = new Font(FAMILIA, Font.BOLD,  22);
    public static final Font FONTE_SUBTITULO  = new Font(FAMILIA, Font.BOLD,  16);
    public static final Font FONTE_BOTAO      = new Font(FAMILIA, Font.BOLD,  13);
    public static final Font FONTE_LABEL      = new Font(FAMILIA, Font.PLAIN, 13);
    public static final Font FONTE_CAMPO      = new Font(FAMILIA, Font.PLAIN, 13);
    public static final Font FONTE_TABELA     = new Font(FAMILIA, Font.PLAIN, 12);
    public static final Font FONTE_CABECALHO  = new Font(FAMILIA, Font.BOLD,  12);
    public static final Font FONTE_PEQUENA    = new Font(FAMILIA, Font.PLAIN, 11);
    public static final Font FONTE_RODAPE     = new Font(FAMILIA, Font.ITALIC,11);

    // Construtor privado — classe utilitária
    private AppColors() {}
}
