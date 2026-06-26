package com.fornodipietra.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Tela principal do sistema Forno di Pietra Ristorante.
 *
 * Exibe o painel de navegação central com botões para todas
 * as funcionalidades do sistema. Cada botão abre a tela
 * correspondente como uma nova janela (JFrame) independente.
 *
 * Layout:
 *  - Cabeçalho com logo textual e nome do restaurante
 *  - Grade 3×3 de botões de navegação (emoji + rótulo)
 *  - Rodapé com versão e status da conexão
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        configurarJanela();
        construirUI();
    }

    // ─── CONFIGURAÇÃO DA JANELA ───────────────────────────────────────────────

    private void configurarJanela() {
        setTitle("Forno di Pietra Ristorante — Sistema de Gestão");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setMinimumSize(new Dimension(600, 520));
        setLocationRelativeTo(null); // centraliza na tela
        setResizable(true);
        getContentPane().setBackground(AppColors.CREME);
    }

    // ─── CONSTRUÇÃO DA UI ────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));

        add(criarCabecalho(), BorderLayout.NORTH);
        add(criarPainelNavegacao(), BorderLayout.CENTER);
        add(criarRodape(), BorderLayout.SOUTH);
    }

    // ─── CABEÇALHO ───────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(AppColors.VERMELHO);
        cabecalho.setBorder(new EmptyBorder(16, 24, 16, 24));

        // Logo textual — nome do restaurante
        JLabel logo = new JLabel("🍕 Forno di Pietra Ristorante");
        logo.setFont(new Font("SansSerif", Font.BOLD, 26));
        logo.setForeground(AppColors.BRANCO);

        // Slogan em italiano
        JLabel slogan = new JLabel("Autentica cucina italiana • Sistema de Gestão");
        slogan.setFont(AppColors.FONTE_RODAPE);
        slogan.setForeground(new Color(255, 220, 200));

        JPanel textos = new JPanel(new GridLayout(2, 1, 0, 2));
        textos.setOpaque(false);
        textos.add(logo);
        textos.add(slogan);

        // Elemento decorativo direito — bandeira italiana em texto
        JLabel bandeira = new JLabel("🇮🇹");
        bandeira.setFont(new Font("SansSerif", Font.PLAIN, 42));

        cabecalho.add(textos,  BorderLayout.CENTER);
        cabecalho.add(bandeira, BorderLayout.EAST);

        return cabecalho;
    }

    // ─── PAINEL DE NAVEGAÇÃO ─────────────────────────────────────────────────

    private JPanel criarPainelNavegacao() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(AppColors.CREME);
        container.setBorder(new EmptyBorder(20, 20, 10, 20));

        // Título da seção
        JLabel titulo = new JLabel("Selecione uma funcionalidade:");
        titulo.setFont(AppColors.FONTE_SUBTITULO);
        titulo.setForeground(AppColors.CINZA_TEXTO);
        titulo.setBorder(new EmptyBorder(0, 0, 14, 0));

        // Grade de botões 3 colunas
        JPanel grade = new JPanel(new GridLayout(3, 3, 12, 12));
        grade.setBackground(AppColors.CREME);

        // ── Linha 1: CRUD ─────────────────────────────────────────────────
        JButton btnPratos    = UIHelper.botaoNavegacao("🍽️",  "Pratos");
        JButton btnClientes  = UIHelper.botaoNavegacao("👤",  "Clientes");
        JButton btnPedidos   = UIHelper.botaoNavegacao("📋",  "Novo Pedido");

        // ── Linha 2: Operações ────────────────────────────────────────────
        JButton btnDisponib  = UIHelper.botaoNavegacao("✅",  "Disponibilidade");
        JButton btnFidelidade= UIHelper.botaoNavegacao("🥇",  "Carta d'Oro");
        JButton btnRelatorios= UIHelper.botaoNavegacao("📊",  "Relatórios");

        // ── Linha 3: Gestão ───────────────────────────────────────────────
        JButton btnHistorico = UIHelper.botaoNavegacao("🧾",  "Histórico");
        JPanel  espaco       = new JPanel();
        espaco.setOpaque(false);
        JButton btnSair      = UIHelper.botaoNavegacao("🚪",  "Sair");

        // Adiciona à grade
        grade.add(btnPratos);
        grade.add(btnClientes);
        grade.add(btnPedidos);
        grade.add(btnDisponib);
        grade.add(btnFidelidade);
        grade.add(btnRelatorios);
        grade.add(btnHistorico);
        grade.add(espaco);
        grade.add(btnSair);

        // ── Ações dos botões ──────────────────────────────────────────────

        btnPratos.addActionListener(e -> abrirPratos());
        btnClientes.addActionListener(e -> abrirClientes());
        btnPedidos.addActionListener(e -> abrirPedidos());
        btnDisponib.addActionListener(e -> abrirDisponibilidade());
        btnFidelidade.addActionListener(e -> abrirFidelidade());
        btnRelatorios.addActionListener(e -> abrirRelatorios());
        btnHistorico.addActionListener(e -> abrirHistorico());
        btnSair.addActionListener(e -> confirmarSaida());

        container.add(titulo, BorderLayout.NORTH);
        container.add(grade,  BorderLayout.CENTER);

        return container;
    }

    // ─── RODAPÉ ──────────────────────────────────────────────────────────────

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(AppColors.MARROM);
        rodape.setBorder(new EmptyBorder(6, 16, 6, 16));

        JLabel versao = new JLabel("Forno di Pietra v1.0 — ADS Senac PR • 2025");
        versao.setFont(AppColors.FONTE_PEQUENA);
        versao.setForeground(new Color(230, 210, 185));

        JLabel status = new JLabel("● Conectado");
        status.setFont(AppColors.FONTE_PEQUENA);
        status.setForeground(new Color(120, 220, 120));

        rodape.add(versao, BorderLayout.WEST);
        rodape.add(status, BorderLayout.EAST);

        return rodape;
    }

    // ─── AÇÕES DE NAVEGAÇÃO ──────────────────────────────────────────────────

    private void abrirPratos() {
        new PratoFormFrame(this).setVisible(true);
    }

    private void abrirClientes() {
        new ClienteFormFrame(this).setVisible(true);
    }

    private void abrirPedidos() {
        new PedidoFrame(this).setVisible(true);
    }

    private void abrirDisponibilidade() {
        new DisponibilidadeFrame(this).setVisible(true);
    }

    private void abrirFidelidade() {
        new FidelidadeFrame(this).setVisible(true);
    }

    private void abrirRelatorios() {
        new RelatorioFrame(this).setVisible(true);
    }

    private void abrirHistorico() {
        // Abre a tela de relatórios direto na aba de histórico de pedidos
        RelatorioFrame relatorio = new RelatorioFrame(this);
        relatorio.selecionarRelatorio(3); // índice do relatório "Pedidos por Cliente"
        relatorio.setVisible(true);
    }

    private void confirmarSaida() {
        boolean confirma = UIHelper.confirmarDialog(this,
            "Deseja realmente sair do sistema?\nA conexão com o banco será encerrada.");
        if (confirma) {
            com.fornodipietra.config.DatabaseConnection.getInstance().fechar();
            System.exit(0);
        }
    }
}
