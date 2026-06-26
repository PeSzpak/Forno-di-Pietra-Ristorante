package com.fornodipietra.ui;

import com.fornodipietra.model.Cliente;
import com.fornodipietra.model.Pedido;
import com.fornodipietra.model.Prato;
import com.fornodipietra.service.ClienteService;
import com.fornodipietra.service.FidelidadeService;
import com.fornodipietra.service.PedidoService;
import com.fornodipietra.service.PratoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Tela de Relatórios — Forno di Pietra Ristorante.
 *
 * 8 relatórios disponíveis via JComboBox:
 *  1. Pratos por Categoria
 *  2. Pratos Mais Vendidos
 *  3. Clientes Mais Ativos
 *  4. Pedidos de um Cliente Específico
 *  5. Faturamento Total por Período
 *  6. Pratos Indisponíveis
 *  7. Clientes por Tier (Carta d'Oro)
 *  8. Resgates de Fidelidade por Período
 *
 * Layout:
 *  NORTE  — cabeçalho + painel de filtros dinâmicos
 *  CENTRO — tabela de resultados
 *  SUL    — rodapé com totalizador e botão Gerar
 */
public class RelatorioFrame extends JFrame {

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final PratoService      pratoService      = new PratoService();
    private final ClienteService    clienteService    = new ClienteService();
    private final PedidoService     pedidoService     = new PedidoService();
    private final FidelidadeService fidelidadeService = new FidelidadeService();

    // ─── Nomes dos relatórios (posição = índice no combo) ─────────────────────
    private static final String[] NOMES_RELATORIOS = {
        "1. Pratos por Categoria",
        "2. Pratos Mais Vendidos",
        "3. Clientes Mais Ativos",
        "4. Pedidos de um Cliente Específico",
        "5. Faturamento Total por Período",
        "6. Pratos Indisponíveis",
        "7. Clientes por Tier — Carta d'Oro",
        "8. Resgates de Fidelidade por Período"
    };

    // ─── Componentes principais ───────────────────────────────────────────────
    private JComboBox<String>  comboRelatorios;
    private JPanel             painelFiltros;
    private DefaultTableModel  modeloResultados;
    private JTable             tabelaResultados;
    private JLabel             labelTotalizador;
    private JButton            btnGerar;

    // ─── Filtros dinâmicos ────────────────────────────────────────────────────
    private JTextField  campoDataInicio;
    private JTextField  campoDataFim;
    private JComboBox<String> comboClientes;
    private List<Cliente>     clientes;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public RelatorioFrame(JFrame pai) {
        setTitle("Forno di Pietra — Relatórios");
        setSize(960, 660);
        setMinimumSize(new Dimension(800, 540));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarClientes();
        atualizarFiltros();
    }

    /** Pré-seleciona um relatório pelo índice (usado pelo MainFrame). */
    public void selecionarRelatorio(int indice) {
        if (comboRelatorios != null && indice >= 0
                && indice < comboRelatorios.getItemCount()) {
            comboRelatorios.setSelectedIndex(indice);
        }
    }

    // ─── CONSTRUÇÃO DA UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(criarCabecalho(),  BorderLayout.NORTH);
        add(criarCorpo(),      BorderLayout.CENTER);
        add(criarRodape(),     BorderLayout.SOUTH);
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AppColors.VERMELHO);
        cab.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("📊  Relatórios Gerenciais");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        JLabel sub = new JLabel("8 relatórios disponíveis");
        sub.setFont(AppColors.FONTE_PEQUENA);
        sub.setForeground(new Color(255, 220, 200));

        cab.add(titulo, BorderLayout.WEST);
        cab.add(sub,    BorderLayout.EAST);
        return cab;
    }

    // ── Corpo: seleção + filtros + tabela ─────────────────────────────────────

    private JPanel criarCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setBackground(AppColors.CREME);
        corpo.setBorder(new EmptyBorder(10, 12, 0, 12));

        corpo.add(criarPainelSelecao(), BorderLayout.NORTH);
        corpo.add(criarPainelTabela(), BorderLayout.CENTER);
        return corpo;
    }

    // ── Painel de seleção e filtros ────────────────────────────────────────────

    private JPanel criarPainelSelecao() {
        JPanel painel = UIHelper.painelFormulario("Selecionar Relatório e Filtros");
        painel.setLayout(new BorderLayout(0, 8));

        // Linha 1: combo de relatórios
        JPanel linhaCombo = new JPanel(new BorderLayout(8, 0));
        linhaCombo.setOpaque(false);
        linhaCombo.add(UIHelper.label("Relatório:"), BorderLayout.WEST);

        comboRelatorios = new JComboBox<>(NOMES_RELATORIOS);
        comboRelatorios.setFont(AppColors.FONTE_CAMPO);
        comboRelatorios.addActionListener(e -> atualizarFiltros());

        btnGerar = UIHelper.botaoPrimario("▶  Gerar Relatório");
        btnGerar.addActionListener(e -> gerarRelatorio());

        linhaCombo.add(comboRelatorios, BorderLayout.CENTER);
        linhaCombo.add(btnGerar,        BorderLayout.EAST);

        // Linha 2: filtros dinâmicos (muda conforme relatório)
        painelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        painelFiltros.setOpaque(false);

        painel.add(linhaCombo,    BorderLayout.NORTH);
        painel.add(painelFiltros, BorderLayout.CENTER);
        return painel;
    }

    // ── Painel da tabela de resultados ────────────────────────────────────────

    private JPanel criarPainelTabela() {
        JPanel painel = UIHelper.painelFormulario("Resultados");
        painel.setLayout(new BorderLayout());

        modeloResultados = new DefaultTableModel() {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaResultados = UIHelper.tabela(modeloResultados);

        painel.add(UIHelper.scroll(tabelaResultados), BorderLayout.CENTER);
        return painel;
    }

    // ── Rodapé com totalizador ────────────────────────────────────────────────

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(AppColors.BEGE);
        rodape.setBorder(new EmptyBorder(8, 16, 8, 16));

        labelTotalizador = new JLabel(" Selecione um relatório e clique em Gerar.");
        labelTotalizador.setFont(AppColors.FONTE_LABEL);
        labelTotalizador.setForeground(AppColors.CINZA_TEXTO);

        JButton btnFechar = UIHelper.botaoNeutro("✖ Fechar");
        btnFechar.addActionListener(e -> dispose());

        rodape.add(labelTotalizador, BorderLayout.WEST);
        rodape.add(btnFechar,        BorderLayout.EAST);
        return rodape;
    }

    // ─── FILTROS DINÂMICOS ────────────────────────────────────────────────────

    /**
     * Atualiza os filtros exibidos conforme o relatório selecionado.
     * Relatórios 5 e 8 exigem período; relatório 4 exige cliente.
     */
    private void atualizarFiltros() {
        painelFiltros.removeAll();
        int idx = comboRelatorios.getSelectedIndex();

        // Relatórios que precisam de período (5, 8)
        if (idx == 4 || idx == 7) {
            campoDataInicio = UIHelper.campo(10);
            campoDataFim    = UIHelper.campo(10);
            campoDataInicio.setToolTipText("dd/MM/aaaa");
            campoDataFim.setToolTipText("dd/MM/aaaa");

            painelFiltros.add(UIHelper.label("Período:"));
            painelFiltros.add(UIHelper.label("De:"));
            painelFiltros.add(campoDataInicio);
            painelFiltros.add(UIHelper.label("Até:"));
            painelFiltros.add(campoDataFim);
            painelFiltros.add(UIHelper.label("(dd/MM/aaaa)"));
        }

        // Relatório 4: pedidos por cliente
        if (idx == 3) {
            comboClientes = new JComboBox<>();
            comboClientes.setFont(AppColors.FONTE_CAMPO);
            comboClientes.setPreferredSize(new Dimension(260, 28));
            if (clientes != null) {
                comboClientes.addItem("— Selecione —");
                for (Cliente c : clientes) {
                    comboClientes.addItem(c.getId() + " — " + c.getNome());
                }
            }
            painelFiltros.add(UIHelper.label("Cliente:"));
            painelFiltros.add(comboClientes);
        }

        // Relatórios sem filtro: exibe nota informativa
        if (idx != 4 && idx != 7 && idx != 3) {
            JLabel nota = new JLabel("Sem filtros — clique em Gerar para ver os resultados.");
            nota.setFont(AppColors.FONTE_PEQUENA);
            nota.setForeground(AppColors.CINZA_TEXTO);
            painelFiltros.add(nota);
        }

        painelFiltros.revalidate();
        painelFiltros.repaint();
        limparTabela();
    }

    // ─── GERAÇÃO DOS RELATÓRIOS ───────────────────────────────────────────────

    private void gerarRelatorio() {
        int idx = comboRelatorios.getSelectedIndex();
        limparTabela();

        try {
            switch (idx) {
                case 0: relatorio1PratosPorCategoria();    break;
                case 1: relatorio2MaisVendidos();          break;
                case 2: relatorio3ClientesMaisAtivos();    break;
                case 3: relatorio4PedidosPorCliente();     break;
                case 4: relatorio5FaturamentoPorPeriodo(); break;
                case 5: relatorio6PratosIndisponiveis();   break;
                case 6: relatorio7ClientesPorTier();       break;
                case 7: relatorio8ResgatesPorPeriodo();    break;
                default:
                    UIHelper.avisoDialog(this, "Relatório não implementado.");
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao gerar relatório:\n" + ex.getMessage());
        }
    }

    // ── R1: Pratos por Categoria ──────────────────────────────────────────────

    private void relatorio1PratosPorCategoria() {
        definirColunas("Código", "Nome", "Preço (R$)", "Categoria", "Disponível");
        List<Prato> pratos = pratoService.listarTodos();

        for (Prato p : pratos) {
            modeloResultados.addRow(new Object[]{
                p.getCodigo(),
                p.getNome(),
                String.format("%.2f", p.getPreco()),
                p.getCategoria().getDescricao(),
                p.isDisponivel() ? "✅ Sim" : "❌ Não"
            });
        }
        atualizarTotalizador(pratos.size() + " prato(s) encontrado(s).");
    }

    // ── R2: Pratos Mais Vendidos ──────────────────────────────────────────────

    private void relatorio2MaisVendidos() {
        definirColunas("Nome do Prato", "Categoria", "Total Vendido (unid.)");
        List<Object[]> dados = pratoService.relatorioPratosMaisVendidos();

        for (Object[] linha : dados) {
            modeloResultados.addRow(linha);
        }

        if (dados.isEmpty()) {
            semResultados();
        } else {
            atualizarTotalizador(dados.size() + " prato(s) com venda registrada.");
        }
    }

    // ── R3: Clientes Mais Ativos ──────────────────────────────────────────────

    private void relatorio3ClientesMaisAtivos() {
        definirColunas("Nome", "E-mail", "Telefone", "Total de Pedidos");
        List<Cliente> clientesAtivos = clienteService.relatorioClientesMaisAtivos();

        for (Cliente c : clientesAtivos) {
            modeloResultados.addRow(new Object[]{
                c.getNome(),
                c.getEmail(),
                c.getTelefone(),
                c.getTotalPedidos()
            });
        }

        if (clientesAtivos.isEmpty()) {
            semResultados();
        } else {
            atualizarTotalizador(clientesAtivos.size() + " cliente(s) com pedidos.");
        }
    }

    // ── R4: Pedidos de um Cliente Específico ──────────────────────────────────

    private void relatorio4PedidosPorCliente() {
        if (comboClientes == null || comboClientes.getSelectedIndex() <= 0) {
            UIHelper.erroDialog(this, "Selecione um cliente para gerar este relatório.");
            return;
        }

        Cliente cliente = clientes.get(comboClientes.getSelectedIndex() - 1);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        definirColunas("Pedido #", "Data/Hora", "Valor Total (R$)", "Status");
        List<Pedido> pedidos = pedidoService.listarPorCliente(cliente.getId());

        BigDecimal totalGeral = BigDecimal.ZERO;
        for (Pedido p : pedidos) {
            modeloResultados.addRow(new Object[]{
                p.getId(),
                p.getDataHora() != null ? p.getDataHora().format(fmt) : "—",
                String.format("%.2f", p.getValorTotal()),
                p.getStatus().getDescricao()
            });
            totalGeral = totalGeral.add(p.getValorTotal());
        }

        if (pedidos.isEmpty()) {
            semResultados();
        } else {
            atualizarTotalizador(pedidos.size() + " pedido(s) de " + cliente.getNome()
                + "  |  Total gasto: R$ " + String.format("%.2f", totalGeral));
        }
    }

    // ── R5: Faturamento Total por Período ─────────────────────────────────────

    private void relatorio5FaturamentoPorPeriodo() {
        String dataInicio = obterDataInicio();
        String dataFim    = obterDataFim();
        if (dataInicio == null || dataFim == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        definirColunas("Pedido #", "Cliente ID", "Data/Hora", "Valor Total (R$)", "Status");
        List<Pedido> pedidos = pedidoService.relatorioFaturamentoPorPeriodo(dataInicio, dataFim);

        for (Pedido p : pedidos) {
            modeloResultados.addRow(new Object[]{
                p.getId(),
                p.getClienteId(),
                p.getDataHora() != null ? p.getDataHora().format(fmt) : "—",
                String.format("%.2f", p.getValorTotal()),
                p.getStatus().getDescricao()
            });
        }

        if (pedidos.isEmpty()) {
            semResultados();
        } else {
            BigDecimal total = pedidoService.calcularFaturamentoTotal(pedidos);
            atualizarTotalizador(pedidos.size() + " pedido(s) finalizado(s)"
                + "  |  Faturamento: R$ " + String.format("%.2f", total));
        }
    }

    // ── R6: Pratos Indisponíveis ──────────────────────────────────────────────

    private void relatorio6PratosIndisponiveis() {
        definirColunas("Código", "Nome", "Categoria", "Preço (R$)");
        List<Prato> pratos = pratoService.listarIndisponiveis();

        for (Prato p : pratos) {
            modeloResultados.addRow(new Object[]{
                p.getCodigo(),
                p.getNome(),
                p.getCategoria().getDescricao(),
                String.format("%.2f", p.getPreco())
            });
        }

        if (pratos.isEmpty()) {
            semResultados();
            atualizarTotalizador("Todos os pratos estão disponíveis! ✅");
        } else {
            atualizarTotalizador(pratos.size() + " prato(s) indisponível(is) no momento.");
        }
    }

    // ── R7: Clientes por Tier ─────────────────────────────────────────────────

    private void relatorio7ClientesPorTier() {
        definirColunas("Tier", "Quantidade de Clientes");
        List<Object[]> dados = fidelidadeService.relatorioClientesPorTier();

        // Mapeia nome do tier para ícone + descrição amigável
        for (Object[] linha : dados) {
            String tierDb = (String) linha[0];
            String tierLabel;
            switch (tierDb) {
                case "BRONZE": tierLabel = "🥉 Bronze  (0–499 pts)";  break;
                case "PRATA":  tierLabel = "🥈 Prata   (500–1499 pts)"; break;
                case "OURO":   tierLabel = "🥇 Ouro    (1500+ pts)";   break;
                default:       tierLabel = tierDb;
            }
            modeloResultados.addRow(new Object[]{ tierLabel, linha[1] });
        }

        if (dados.isEmpty()) {
            semResultados();
        } else {
            int total = 0;
            for (Object[] linha : dados) total += (int) linha[1];
            atualizarTotalizador(total + " cliente(s) no programa Carta d'Oro.");
        }
    }

    // ── R8: Resgates de Fidelidade por Período ────────────────────────────────

    private void relatorio8ResgatesPorPeriodo() {
        String dataInicio = obterDataInicio();
        String dataFim    = obterDataFim();
        if (dataInicio == null || dataFim == null) return;

        definirColunas("Cliente", "Tipo de Resgate", "Prato Resgatado",
                        "Desconto (%)", "Pontos Usados", "Data/Hora");
        List<Object[]> dados = fidelidadeService.relatorioResgatesPorPeriodo(dataInicio, dataFim);

        for (Object[] linha : dados) {
            // Tipo de resgate com ícone
            String tipo = "DESCONTO".equals(linha[1]) ? "🎁 Desconto" : "🍕 Prato grátis";
            modeloResultados.addRow(new Object[]{
                linha[0],                                        // cliente_nome
                tipo,
                linha[2] != null ? linha[2] : "—",              // prato_nome
                linha[3] != null ? linha[3] + "%" : "—",        // desconto_aplicado
                linha[4],                                        // pontos_usados
                linha[5] != null ? linha[5].toString() : "—"    // data_resgate
            });
        }

        if (dados.isEmpty()) {
            semResultados();
        } else {
            atualizarTotalizador(dados.size() + " resgate(s) no período informado.");
        }
    }

    // ─── HELPERS DE UI ────────────────────────────────────────────────────────

    /** Define as colunas da tabela de resultados. */
    private void definirColunas(String... nomes) {
        modeloResultados.setColumnCount(0);
        for (String nome : nomes) {
            modeloResultados.addColumn(nome);
        }
        modeloResultados.setRowCount(0);
    }

    private void limparTabela() {
        if (modeloResultados != null) {
            modeloResultados.setRowCount(0);
        }
        if (labelTotalizador != null) {
            labelTotalizador.setText(" Clique em Gerar Relatório para ver os resultados.");
            labelTotalizador.setForeground(AppColors.CINZA_TEXTO);
        }
    }

    private void semResultados() {
        modeloResultados.addRow(new Object[]{ "Nenhum registro encontrado para os critérios informados." });
        atualizarTotalizador("0 registros encontrados.");
    }

    private void atualizarTotalizador(String texto) {
        labelTotalizador.setText("  " + texto);
        labelTotalizador.setForeground(AppColors.TEXTO);
    }

    /** Obtém e valida a data de início dos campos de filtro. */
    private String obterDataInicio() {
        if (campoDataInicio == null || campoDataInicio.getText().trim().isEmpty()) {
            UIHelper.erroDialog(this, "Informe a data inicial (dd/MM/aaaa).");
            return null;
        }
        return campoDataInicio.getText().trim();
    }

    private String obterDataFim() {
        if (campoDataFim == null || campoDataFim.getText().trim().isEmpty()) {
            UIHelper.erroDialog(this, "Informe a data final (dd/MM/aaaa).");
            return null;
        }
        return campoDataFim.getText().trim();
    }

    private void carregarClientes() {
        try {
            clientes = clienteService.listarTodos();
        } catch (Exception ex) {
            clientes = null;
        }
    }
}
