package com.fornodipietra.ui;

import com.fornodipietra.model.Cliente;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.Prato;
import com.fornodipietra.model.ResgatesFidelidade;
import com.fornodipietra.model.TierFidelidade;
import com.fornodipietra.model.TipoResgate;
import com.fornodipietra.service.ClienteService;
import com.fornodipietra.service.FidelidadeService;
import com.fornodipietra.service.PratoService;
import com.fornodipietra.util.TierUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela do Programa de Fidelidade Carta d'Oro.
 *
 * Organizada em duas abas:
 *
 *  Aba 1 — Consulta e Resgate:
 *   - Selecionar cliente via ComboBox
 *   - Exibe card visual com tier, pontos e barra de progresso
 *   - Histórico de resgates do cliente em tabela
 *   - Botão Resgatar Desconto (Prata/Ouro)
 *   - Botão Resgatar Prato Grátis (Ouro, prato ≤ R$35)
 *
 *  Aba 2 — Visão Geral:
 *   - Distribuição de clientes por tier (Bronze/Prata/Ouro)
 *   - Tabela de todos os clientes com seus tiers e pontos
 */
public class FidelidadeFrame extends JFrame {

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final FidelidadeService fidelidadeService = new FidelidadeService();
    private final ClienteService    clienteService    = new ClienteService();
    private final PratoService      pratoService      = new PratoService();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private Cliente    clienteSelecionado = null;
    private Fidelidade fidelidadeAtual   = null;
    private List<Cliente> clientes;

    // ─── Componentes — seleção ────────────────────────────────────────────────
    private JComboBox<String> comboClientes;

    // ─── Componentes — card de tier ──────────────────────────────────────────
    private JPanel  painelCard;
    private JLabel  labelCardIcone;
    private JLabel  labelCardTier;
    private JLabel  labelCardPontos;
    private JLabel  labelCardFaltam;
    private JLabel  labelCardBeneficios;
    private JProgressBar barraProgresso;

    // ─── Componentes — histórico ──────────────────────────────────────────────
    private DefaultTableModel modeloHistorico;
    private JTable            tabelaHistorico;

    // ─── Componentes — visão geral ────────────────────────────────────────────
    private DefaultTableModel modeloGeral;
    private JTable            tabelaGeral;
    private JLabel            labelResumoBronze;
    private JLabel            labelResumoPrata;
    private JLabel            labelResumoOuro;

    // ─── Botões de resgate ────────────────────────────────────────────────────
    private JButton btnResgatarDesconto;
    private JButton btnResgatarPrato;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public FidelidadeFrame(JFrame pai) {
        setTitle("Forno di Pietra — Carta d'Oro Fidelidade");
        setSize(980, 660);
        setMinimumSize(new Dimension(820, 540));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarClientes();
    }

    // ─── CONSTRUÇÃO DA UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(AppColors.FONTE_BOTAO);
        abas.addTab("🥇  Consulta e Resgate", criarAbaConsulta());
        abas.addTab("📊  Visão Geral",         criarAbaVisaoGeral());

        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 1) carregarVisaoGeral();
        });

        add(abas, BorderLayout.CENTER);
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(new Color(150, 110, 0)); // dourado
        cab.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("🥇  Carta d'Oro — Programa de Fidelidade");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        JLabel sub = new JLabel("Bronze • Prata • Ouro");
        sub.setFont(AppColors.FONTE_PEQUENA);
        sub.setForeground(new Color(255, 235, 150));

        cab.add(titulo, BorderLayout.WEST);
        cab.add(sub,    BorderLayout.EAST);
        return cab;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ABA 1 — CONSULTA E RESGATE
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarAbaConsulta() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBackground(AppColors.CREME);
        aba.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Topo: seleção de cliente
        aba.add(criarPainelSelecao(), BorderLayout.NORTH);

        // Split: card (esquerda) | histórico (direita)
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            criarPainelCard(),
            criarPainelHistorico()
        );
        split.setDividerLocation(360);
        split.setResizeWeight(0.37);
        split.setBackground(AppColors.CREME);

        aba.add(split,               BorderLayout.CENTER);
        aba.add(criarBarraResgates(), BorderLayout.SOUTH);
        return aba;
    }

    // ── Seleção de cliente ────────────────────────────────────────────────────

    private JPanel criarPainelSelecao() {
        JPanel painel = UIHelper.painelFormulario("Selecionar Cliente");
        painel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 4));

        comboClientes = new JComboBox<>();
        comboClientes.setFont(AppColors.FONTE_CAMPO);
        comboClientes.setPreferredSize(new Dimension(340, 30));
        comboClientes.addActionListener(e -> onClienteSelecionado());

        JButton btnAtualizar = UIHelper.botaoSecundario("🔄");
        btnAtualizar.addActionListener(e -> {
            carregarClientes();
            if (clienteSelecionado != null) onClienteSelecionado();
        });

        painel.add(UIHelper.label("Cliente:"));
        painel.add(comboClientes);
        painel.add(btnAtualizar);
        return painel;
    }

    // ── Card visual de tier ───────────────────────────────────────────────────

    private JPanel criarPainelCard() {
        painelCard = UIHelper.painelFormulario("Saldo de Fidelidade");
        painelCard.setLayout(new BoxLayout(painelCard, BoxLayout.Y_AXIS));

        // Ícone grande do tier
        labelCardIcone = new JLabel("🏆", SwingConstants.CENTER);
        labelCardIcone.setFont(new Font("SansSerif", Font.PLAIN, 52));
        labelCardIcone.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Nome do tier
        labelCardTier = new JLabel("Selecione um cliente", SwingConstants.CENTER);
        labelCardTier.setFont(new Font("SansSerif", Font.BOLD, 20));
        labelCardTier.setForeground(AppColors.CINZA_TEXTO);
        labelCardTier.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Pontos
        labelCardPontos = new JLabel("— pontos", SwingConstants.CENTER);
        labelCardPontos.setFont(new Font("SansSerif", Font.BOLD, 15));
        labelCardPontos.setForeground(AppColors.TEXTO);
        labelCardPontos.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Barra de progresso para próximo tier
        barraProgresso = new JProgressBar(0, 100);
        barraProgresso.setValue(0);
        barraProgresso.setStringPainted(true);
        barraProgresso.setString("—");
        barraProgresso.setFont(AppColors.FONTE_PEQUENA);
        barraProgresso.setForeground(AppColors.OURO);
        barraProgresso.setBackground(AppColors.CINZA_CLARO);
        barraProgresso.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        barraProgresso.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Faltam X pontos
        labelCardFaltam = new JLabel(" ", SwingConstants.CENTER);
        labelCardFaltam.setFont(AppColors.FONTE_PEQUENA);
        labelCardFaltam.setForeground(AppColors.CINZA_TEXTO);
        labelCardFaltam.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Separador
        JSeparator sep = UIHelper.separador();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sep.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Benefícios disponíveis
        labelCardBeneficios = new JLabel("<html><center>—</center></html>", SwingConstants.CENTER);
        labelCardBeneficios.setFont(AppColors.FONTE_LABEL);
        labelCardBeneficios.setForeground(AppColors.CINZA_TEXTO);
        labelCardBeneficios.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelCard.add(Box.createVerticalStrut(12));
        painelCard.add(labelCardIcone);
        painelCard.add(Box.createVerticalStrut(8));
        painelCard.add(labelCardTier);
        painelCard.add(Box.createVerticalStrut(10));
        painelCard.add(labelCardPontos);
        painelCard.add(Box.createVerticalStrut(10));
        painelCard.add(barraProgresso);
        painelCard.add(Box.createVerticalStrut(4));
        painelCard.add(labelCardFaltam);
        painelCard.add(Box.createVerticalStrut(12));
        painelCard.add(sep);
        painelCard.add(Box.createVerticalStrut(10));
        painelCard.add(labelCardBeneficios);
        painelCard.add(Box.createVerticalGlue());

        return painelCard;
    }

    // ── Histórico de resgates ─────────────────────────────────────────────────

    private JPanel criarPainelHistorico() {
        JPanel painel = UIHelper.painelFormulario("Histórico de Resgates");
        painel.setLayout(new BorderLayout(0, 6));

        String[] colunas = {"Data", "Tipo", "Detalhe", "Pontos Usados"};
        modeloHistorico = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaHistorico = UIHelper.tabela(modeloHistorico);
        tabelaHistorico.getColumnModel().getColumn(0).setPreferredWidth(120);
        tabelaHistorico.getColumnModel().getColumn(1).setPreferredWidth(110);
        tabelaHistorico.getColumnModel().getColumn(2).setPreferredWidth(180);
        tabelaHistorico.getColumnModel().getColumn(3).setPreferredWidth(90);

        JLabel dica = new JLabel(" Selecione um cliente para ver o histórico.");
        dica.setFont(AppColors.FONTE_PEQUENA);
        dica.setForeground(AppColors.CINZA_TEXTO);

        painel.add(UIHelper.scroll(tabelaHistorico), BorderLayout.CENTER);
        painel.add(dica,                             BorderLayout.SOUTH);
        return painel;
    }

    // ── Barra de botões de resgate ────────────────────────────────────────────

    private JPanel criarBarraResgates() {
        btnResgatarDesconto = UIHelper.botaoPrimario("🎁 Resgatar Desconto (500 pts)");
        btnResgatarPrato    = UIHelper.botaoPrimario("🍕 Resgatar Prato Grátis (500 pts)");
        JButton btnFechar   = UIHelper.botaoNeutro("✖ Fechar");

        // Cor dourada para os botões de resgate
        btnResgatarDesconto.setBackground(new Color(160, 120, 0));
        btnResgatarPrato.setBackground(new Color(160, 120, 0));

        btnResgatarDesconto.setEnabled(false);
        btnResgatarPrato.setEnabled(false);

        btnResgatarDesconto.addActionListener(e -> resgatarDesconto());
        btnResgatarPrato.addActionListener(e    -> resgatarPratoGratis());
        btnFechar.addActionListener(e           -> dispose());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.BEGE);
        barra.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel dica = new JLabel(" Prata: desconto  |  Ouro: desconto ou prato grátis");
        dica.setFont(AppColors.FONTE_PEQUENA);
        dica.setForeground(AppColors.CINZA_TEXTO);

        JPanel direita = UIHelper.painelBotoes(btnResgatarDesconto, btnResgatarPrato, btnFechar);
        direita.setBackground(AppColors.BEGE);

        barra.add(dica,    BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ABA 2 — VISÃO GERAL
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarAbaVisaoGeral() {
        JPanel aba = new JPanel(new BorderLayout(0, 12));
        aba.setBackground(AppColors.CREME);
        aba.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Cards de contagem por tier
        aba.add(criarPainelContagens(), BorderLayout.NORTH);

        // Tabela com todos os clientes e seus tiers
        JPanel painelTabela = UIHelper.painelFormulario("Todos os Clientes — Ranking de Pontos");
        painelTabela.setLayout(new BorderLayout());

        String[] colunas = {"Cliente", "E-mail", "Tier", "Pontos", "Próx. Tier"};
        modeloGeral = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaGeral = UIHelper.tabela(modeloGeral);
        tabelaGeral.getColumnModel().getColumn(0).setPreferredWidth(180);
        tabelaGeral.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabelaGeral.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabelaGeral.getColumnModel().getColumn(3).setPreferredWidth(80);
        tabelaGeral.getColumnModel().getColumn(4).setPreferredWidth(100);

        JButton btnAtualizar = UIHelper.botaoSecundario("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarVisaoGeral());

        JPanel barraGeral = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barraGeral.setBackground(AppColors.CREME);
        barraGeral.add(btnAtualizar);

        painelTabela.add(UIHelper.scroll(tabelaGeral), BorderLayout.CENTER);
        painelTabela.add(barraGeral,                   BorderLayout.SOUTH);

        aba.add(painelTabela, BorderLayout.CENTER);
        return aba;
    }

    // ── Cards de contagem por tier ────────────────────────────────────────────

    private JPanel criarPainelContagens() {
        JPanel painel = new JPanel(new GridLayout(1, 3, 12, 0));
        painel.setBackground(AppColors.CREME);
        painel.setPreferredSize(new Dimension(0, 100));

        labelResumoBronze = criarCardContagem("🥉", "Bronze", "0", AppColors.BRONZE);
        labelResumoPrata  = criarCardContagem("🥈", "Prata",  "0", AppColors.PRATA);
        labelResumoOuro   = criarCardContagem("🥇", "Ouro",   "0", AppColors.OURO);

        painel.add(labelResumoBronze.getParent());
        painel.add(labelResumoPrata.getParent());
        painel.add(labelResumoOuro.getParent());
        return painel;
    }

    /** Cria um card colorido de contagem de clientes por tier. */
    private JLabel criarCardContagem(String icone, String tier, String count, Color cor) {
        JPanel card = new JPanel(new GridLayout(3, 1, 0, 2));
        card.setBackground(cor);
        card.setBorder(new EmptyBorder(10, 16, 10, 16));

        JLabel lIcone = new JLabel(icone + "  " + tier, SwingConstants.CENTER);
        lIcone.setFont(new Font("SansSerif", Font.BOLD, 14));
        lIcone.setForeground(AppColors.BRANCO);

        JLabel lCount = new JLabel(count, SwingConstants.CENTER);
        lCount.setFont(new Font("SansSerif", Font.BOLD, 28));
        lCount.setForeground(AppColors.BRANCO);

        JLabel lLabel = new JLabel("clientes", SwingConstants.CENTER);
        lLabel.setFont(AppColors.FONTE_PEQUENA);
        lLabel.setForeground(new Color(255, 255, 255, 200));

        card.add(lIcone);
        card.add(lCount);
        card.add(lLabel);

        return lCount; // retorna o label de contagem para atualizar depois
    }

    // ─── LÓGICA — CARREGAMENTO ────────────────────────────────────────────────

    private void carregarClientes() {
        comboClientes.removeAllItems();
        comboClientes.addItem("— Selecione um cliente —");
        try {
            clientes = clienteService.listarTodos();
            for (Cliente c : clientes) {
                comboClientes.addItem(c.getId() + " — " + c.getNome());
            }
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar clientes:\n" + ex.getMessage());
        }
    }

    private void carregarVisaoGeral() {
        modeloGeral.setRowCount(0);
        try {
            // Distribuição por tier
            List<Object[]> contagens = fidelidadeService.relatorioClientesPorTier();
            int bronze = 0, prata = 0, ouro = 0;
            for (Object[] linha : contagens) {
                String tier  = (String) linha[0];
                int    total = (int) linha[1];
                if ("BRONZE".equals(tier)) bronze = total;
                if ("PRATA".equals(tier))  prata  = total;
                if ("OURO".equals(tier))   ouro   = total;
            }
            labelResumoBronze.setText(String.valueOf(bronze));
            labelResumoPrata.setText(String.valueOf(prata));
            labelResumoOuro.setText(String.valueOf(ouro));

            // Tabela completa ordenada por pontos
            if (clientes == null) clientes = clienteService.listarTodos();
            List<Object[]> linhasTabela = new ArrayList<>();
            for (Cliente c : clientes) {
                try {
                    Fidelidade fid = fidelidadeService.buscarPorCliente(c.getId());
                    int pontosParaSubir = fid.pontosParaProximoTier();
                    String proximoTier  = fid.getTier() == TierFidelidade.OURO
                        ? "Nível máximo"
                        : "+" + pontosParaSubir + " pts";
                    linhasTabela.add(new Object[]{
                        c.getNome(),
                        c.getEmail(),
                        fid.getTier().getDescricao(),
                        fid.getPontosAcumulados(),
                        proximoTier
                    });
                } catch (Exception ignored) {
                    // cliente sem fidelidade — não exibe
                }
            }

            // Ordena por pontos decrescente (selection sort — sem streams)
            for (int i = 0; i < linhasTabela.size() - 1; i++) {
                int maxIdx = i;
                for (int j = i + 1; j < linhasTabela.size(); j++) {
                    int pontosJ   = (int) linhasTabela.get(j)[3];
                    int pontosMax = (int) linhasTabela.get(maxIdx)[3];
                    if (pontosJ > pontosMax) maxIdx = j;
                }
                Object[] temp = linhasTabela.get(i);
                linhasTabela.set(i, linhasTabela.get(maxIdx));
                linhasTabela.set(maxIdx, temp);
            }

            for (Object[] linha : linhasTabela) {
                modeloGeral.addRow(linha);
            }

        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar visão geral:\n" + ex.getMessage());
        }
    }

    // ─── LÓGICA — CONSULTA ────────────────────────────────────────────────────

    private void onClienteSelecionado() {
        int idx = comboClientes.getSelectedIndex();
        if (idx <= 0) {
            limparCard();
            return;
        }
        clienteSelecionado = clientes.get(idx - 1);
        atualizarCard();
        carregarHistorico();
    }

    private void atualizarCard() {
        if (clienteSelecionado == null) return;
        try {
            fidelidadeAtual = fidelidadeService.buscarPorCliente(clienteSelecionado.getId());
            TierFidelidade tier = fidelidadeAtual.getTier();
            int pontos          = fidelidadeAtual.getPontosAcumulados();
            int faltam          = fidelidadeAtual.pontosParaProximoTier();

            // Ícone e cor conforme tier
            Color corTier;
            switch (tier) {
                case PRATA: corTier = AppColors.PRATA; break;
                case OURO:  corTier = AppColors.OURO;  break;
                default:    corTier = AppColors.BRONZE; break;
            }

            labelCardIcone.setText(obterIconeTier(tier));
            labelCardTier.setText(tier.getDescricao());
            labelCardTier.setForeground(corTier);
            labelCardPontos.setText(pontos + " pontos acumulados");

            // Barra de progresso para próximo tier
            if (tier == TierFidelidade.OURO) {
                barraProgresso.setValue(100);
                barraProgresso.setString("Nível máximo atingido! 🎉");
                barraProgresso.setForeground(AppColors.OURO);
                labelCardFaltam.setText(" ");
            } else {
                TierFidelidade proximo = TierFidelidade.values()[tier.ordinal() + 1];
                int minProximo = proximo.getPontoMinimo();
                int minAtual   = tier.getPontoMinimo();
                int range      = minProximo - minAtual;
                int progresso  = pontos - minAtual;
                int percentual = (range > 0) ? Math.min(100, (progresso * 100) / range) : 0;

                barraProgresso.setValue(percentual);
                barraProgresso.setString(percentual + "% para " + proximo.getDescricao());
                barraProgresso.setForeground(corTier);
                labelCardFaltam.setText("Faltam " + faltam + " pontos para " + proximo.getDescricao());
            }

            // Benefícios
            labelCardBeneficios.setText(
                "<html><center>" + TierUtil.beneficiosDisponiveis(fidelidadeAtual)
                    .replace("\n", "<br>") + "</center></html>"
            );

            // Habilita botões conforme tier
            boolean podePrata = tier == TierFidelidade.PRATA || tier == TierFidelidade.OURO;
            boolean podeOuro  = tier == TierFidelidade.OURO;
            boolean temPontos = pontos >= TierUtil.PONTOS_PARA_DESCONTO;

            btnResgatarDesconto.setEnabled(podePrata && temPontos);
            btnResgatarPrato.setEnabled(podeOuro && temPontos);

        } catch (Exception ex) {
            limparCard();
            UIHelper.erroDialog(this, "Erro ao carregar fidelidade:\n" + ex.getMessage());
        }
    }

    private void carregarHistorico() {
        modeloHistorico.setRowCount(0);
        if (clienteSelecionado == null) return;
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try {
            List<ResgatesFidelidade> resgates =
                fidelidadeService.listarResgatesDoCliente(clienteSelecionado.getId());

            for (ResgatesFidelidade r : resgates) {
                String data    = r.getDataResgate() != null ? r.getDataResgate().format(fmt) : "—";
                String tipo    = r.getTipoResgate().getDescricao();
                String detalhe;
                if (r.getTipoResgate() == TipoResgate.DESCONTO) {
                    detalhe = r.getDescontoAplicado() != null
                        ? r.getDescontoAplicado().intValue() + "% de desconto"
                        : "Desconto aplicado";
                } else {
                    detalhe = r.getPrato() != null
                        ? "Prato: " + r.getPrato().getNome()
                        : "Prato grátis (ID " + r.getPratoId() + ")";
                }
                modeloHistorico.addRow(new Object[]{
                    data, tipo, detalhe, r.getPontosUsados()
                });
            }

            if (resgates.isEmpty()) {
                modeloHistorico.addRow(new Object[]{
                    "—", "—", "Nenhum resgate realizado ainda", "—"
                });
            }
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar histórico:\n" + ex.getMessage());
        }
    }

    // ─── LÓGICA — RESGATES ────────────────────────────────────────────────────

    private void resgatarDesconto() {
        if (clienteSelecionado == null || fidelidadeAtual == null) {
            UIHelper.avisoDialog(this, "Selecione um cliente primeiro.");
            return;
        }

        String erro = TierUtil.validarElegibilidadeDesconto(fidelidadeAtual);
        if (erro != null) { UIHelper.erroDialog(this, erro); return; }

        double pct = TierUtil.obterPercentualDesconto(fidelidadeAtual.getTier());

        boolean confirma = UIHelper.confirmarDialog(this,
            "Confirmar resgate de desconto para " + clienteSelecionado.getNome() + "?\n\n"
            + "Benefício: " + (int) pct + "% de desconto no próximo pedido\n"
            + "Custo: " + TierUtil.PONTOS_PARA_DESCONTO + " pontos\n"
            + "Saldo atual: " + fidelidadeAtual.getPontosAcumulados() + " pontos");
        if (!confirma) return;

        try {
            fidelidadeService.resgatarDesconto(clienteSelecionado.getId());
            UIHelper.sucessoDialog(this,
                "Resgate efetuado com sucesso!\n"
                + (int) pct + "% de desconto disponível para o próximo pedido. 🎉");
            atualizarCard();
            carregarHistorico();
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao resgatar desconto:\n" + ex.getMessage());
        }
    }

    private void resgatarPratoGratis() {
        if (clienteSelecionado == null || fidelidadeAtual == null) {
            UIHelper.avisoDialog(this, "Selecione um cliente primeiro.");
            return;
        }

        String erro = TierUtil.validarElegibilidadePratoGratis(fidelidadeAtual);
        if (erro != null) { UIHelper.erroDialog(this, erro); return; }

        // Carrega pratos elegíveis (disponíveis e preço ≤ R$35)
        List<Prato> pratosElegiveis = new ArrayList<>();
        try {
            for (Prato p : pratoService.listarDisponiveis()) {
                if (TierUtil.validarPratoParaResgate(p.getPreco()) == null) {
                    pratosElegiveis.add(p);
                }
            }
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar pratos:\n" + ex.getMessage());
            return;
        }

        if (pratosElegiveis.isEmpty()) {
            UIHelper.avisoDialog(this,
                "Nenhum prato disponível elegível para resgate.\n"
                + "O prato deve custar até R$ " + TierUtil.VALOR_MAXIMO_PRATO_GRATIS + ".");
            return;
        }

        // Monta opções para JComboBox dentro de JOptionPane
        String[] opcoes = new String[pratosElegiveis.size()];
        for (int i = 0; i < pratosElegiveis.size(); i++) {
            Prato p = pratosElegiveis.get(i);
            opcoes[i] = p.getNome() + "  (R$ " + String.format("%.2f", p.getPreco()) + ")";
        }

        JComboBox<String> comboPratos = new JComboBox<>(opcoes);
        comboPratos.setFont(AppColors.FONTE_CAMPO);

        JPanel painelEscolha = new JPanel(new BorderLayout(0, 8));
        painelEscolha.add(new JLabel("Escolha o prato grátis (máx R$ "
            + TierUtil.VALOR_MAXIMO_PRATO_GRATIS + "):"), BorderLayout.NORTH);
        painelEscolha.add(comboPratos, BorderLayout.CENTER);

        int opcao = JOptionPane.showConfirmDialog(this, painelEscolha,
            "Resgate de Prato Grátis — Carta d'Oro",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (opcao != JOptionPane.OK_OPTION) return;

        Prato pratoEscolhido = pratosElegiveis.get(comboPratos.getSelectedIndex());

        boolean confirma = UIHelper.confirmarDialog(this,
            "Confirmar resgate de prato grátis?\n\n"
            + "Cliente: " + clienteSelecionado.getNome() + "\n"
            + "Prato: " + pratoEscolhido.getNome() + "\n"
            + "Valor: R$ " + String.format("%.2f", pratoEscolhido.getPreco()) + "\n"
            + "Custo: " + TierUtil.PONTOS_PARA_PRATO_GRATIS + " pontos");
        if (!confirma) return;

        try {
            fidelidadeService.resgatarPratoGratis(
                clienteSelecionado.getId(), pratoEscolhido.getId());
            UIHelper.sucessoDialog(this,
                "Prato grátis resgatado com sucesso!\n"
                + "\"" + pratoEscolhido.getNome() + "\" liberado para o cliente. 🍕🥇");
            atualizarCard();
            carregarHistorico();
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao resgatar prato:\n" + ex.getMessage());
        }
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────

    private void limparCard() {
        fidelidadeAtual = null;
        labelCardIcone.setText("🏆");
        labelCardTier.setText("Selecione um cliente");
        labelCardTier.setForeground(AppColors.CINZA_TEXTO);
        labelCardPontos.setText("— pontos");
        barraProgresso.setValue(0);
        barraProgresso.setString("—");
        labelCardFaltam.setText(" ");
        labelCardBeneficios.setText("<html><center>—</center></html>");
        modeloHistorico.setRowCount(0);
        btnResgatarDesconto.setEnabled(false);
        btnResgatarPrato.setEnabled(false);
    }

    private String obterIconeTier(TierFidelidade tier) {
        switch (tier) {
            case PRATA: return "🥈";
            case OURO:  return "🥇";
            default:    return "🥉";
        }
    }
}
