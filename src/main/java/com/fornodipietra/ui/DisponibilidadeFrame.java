package com.fornodipietra.ui;

import com.fornodipietra.model.CategoriaPrato;
import com.fornodipietra.model.Prato;
import com.fornodipietra.service.PratoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de Controle de Disponibilidade de Pratos.
 *
 * Permite ao operador visualizar todos os pratos do cardápio
 * e alternar sua disponibilidade de forma rápida, sem precisar
 * abrir o CRUD completo.
 *
 * Funcionalidades:
 *  - Tabela completa com status visual de disponibilidade
 *  - Filtro por categoria (JComboBox) e por status (todos/disponíveis/indisponíveis)
 *  - Toggle de disponibilidade por linha selecionada
 *  - Ações em massa: habilitar todos / desabilitar todos (com confirmação)
 *  - Contador de disponíveis vs indisponíveis no rodapé
 */
public class DisponibilidadeFrame extends JFrame {

    // ─── Serviço ──────────────────────────────────────────────────────────────
    private final PratoService pratoService = new PratoService();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private List<Prato> todosOsPratos = new ArrayList<>();

    // ─── Filtros ──────────────────────────────────────────────────────────────
    private JComboBox<String> comboCategoria;
    private JComboBox<String> comboStatus;

    // ─── Tabela ───────────────────────────────────────────────────────────────
    private DefaultTableModel modeloTabela;
    private JTable            tabela;

    // ─── Labels de resumo ─────────────────────────────────────────────────────
    private JLabel labelDisponiveis;
    private JLabel labelIndisponiveis;
    private JLabel labelTotal;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public DisponibilidadeFrame(JFrame pai) {
        setTitle("Forno di Pietra — Controle de Disponibilidade");
        setSize(860, 580);
        setMinimumSize(new Dimension(720, 460));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarPratos();
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

        JLabel titulo = new JLabel("✅  Controle de Disponibilidade");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        JLabel sub = new JLabel("Ative ou desative pratos do cardápio rapidamente");
        sub.setFont(AppColors.FONTE_PEQUENA);
        sub.setForeground(new Color(255, 220, 200));

        cab.add(titulo, BorderLayout.WEST);
        cab.add(sub,    BorderLayout.EAST);
        return cab;
    }

    // ── Corpo ─────────────────────────────────────────────────────────────────

    private JPanel criarCorpo() {
        JPanel corpo = new JPanel(new BorderLayout(0, 8));
        corpo.setBackground(AppColors.CREME);
        corpo.setBorder(new EmptyBorder(10, 12, 0, 12));

        corpo.add(criarPainelFiltros(), BorderLayout.NORTH);
        corpo.add(criarPainelTabela(), BorderLayout.CENTER);
        corpo.add(criarBarraBotoes(),  BorderLayout.SOUTH);
        return corpo;
    }

    // ── Filtros ───────────────────────────────────────────────────────────────

    private JPanel criarPainelFiltros() {
        JPanel painel = UIHelper.painelFormulario("Filtros");
        painel.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        // Filtro por categoria
        painel.add(UIHelper.label("Categoria:"));
        comboCategoria = new JComboBox<>();
        comboCategoria.setFont(AppColors.FONTE_CAMPO);
        comboCategoria.setPreferredSize(new Dimension(180, 28));
        comboCategoria.addItem("Todas as categorias");
        for (CategoriaPrato cat : CategoriaPrato.values()) {
            comboCategoria.addItem(cat.getDescricao());
        }
        comboCategoria.addActionListener(e -> aplicarFiltros());
        painel.add(comboCategoria);

        painel.add(Box.createHorizontalStrut(16));

        // Filtro por status
        painel.add(UIHelper.label("Status:"));
        comboStatus = new JComboBox<>(new String[]{
            "Todos", "✅ Disponíveis", "❌ Indisponíveis"
        });
        comboStatus.setFont(AppColors.FONTE_CAMPO);
        comboStatus.setPreferredSize(new Dimension(160, 28));
        comboStatus.addActionListener(e -> aplicarFiltros());
        painel.add(comboStatus);

        painel.add(Box.createHorizontalStrut(16));

        // Botão atualizar
        JButton btnAtualizar = UIHelper.botaoSecundario("🔄 Atualizar");
        btnAtualizar.addActionListener(e -> carregarPratos());
        painel.add(btnAtualizar);

        return painel;
    }

    // ── Tabela ────────────────────────────────────────────────────────────────

    private JPanel criarPainelTabela() {
        JPanel painel = UIHelper.painelFormulario("Cardápio — Clique em uma linha para alternar disponibilidade");
        painel.setLayout(new BorderLayout());

        String[] colunas = {"ID", "Código", "Nome", "Categoria", "Preço (R$)", "Status"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabela = new JTable(modeloTabela) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        // Estilização padrão
        tabela.setFont(AppColors.FONTE_TABELA);
        tabela.setRowHeight(28);
        tabela.setGridColor(AppColors.CINZA_CLARO);
        tabela.setShowGrid(true);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Cabeçalho
        tabela.getTableHeader().setFont(AppColors.FONTE_CABECALHO);
        tabela.getTableHeader().setBackground(AppColors.VERMELHO);
        tabela.getTableHeader().setForeground(AppColors.BRANCO);
        tabela.getTableHeader().setReorderingAllowed(false);

        // Larguras
        tabela.getColumnModel().getColumn(0).setMaxWidth(45);
        tabela.getColumnModel().getColumn(1).setPreferredWidth(75);
        tabela.getColumnModel().getColumn(2).setPreferredWidth(200);
        tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(90);
        tabela.getColumnModel().getColumn(5).setPreferredWidth(110);

        // Renderer colorido para a coluna Status
        tabela.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) {
                    String val = value != null ? value.toString() : "";
                    if (val.contains("Disponível")) {
                        setBackground(new Color(220, 245, 220));
                        setForeground(AppColors.VERDE);
                    } else {
                        setBackground(new Color(250, 225, 225));
                        setForeground(AppColors.VERMELHO);
                    }
                }
                return this;
            }
        });

        // Renderer de linhas alternadas para as outras colunas
        DefaultTableCellRenderer rendererAlternado = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object value, boolean selected, boolean focused, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focused, row, col);
                if (!selected) {
                    setBackground(row % 2 == 0 ? AppColors.BRANCO : AppColors.CINZA_CLARO);
                }
                setBorder(new EmptyBorder(2, 8, 2, 8));
                return this;
            }
        };
        for (int i = 0; i < 5; i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(rendererAlternado);
        }

        painel.add(UIHelper.scroll(tabela), BorderLayout.CENTER);
        return painel;
    }

    // ── Barra de botões ───────────────────────────────────────────────────────

    private JPanel criarBarraBotoes() {
        JButton btnToggle       = UIHelper.botaoPrimario("⇄  Alternar Selecionado(s)");
        JButton btnHabilitarTodos  = UIHelper.botaoSecundario("✅  Habilitar Todos");
        JButton btnDesabilitarTodos= UIHelper.botaoPrimario("❌  Desabilitar Todos");
        JButton btnFechar       = UIHelper.botaoNeutro("✖ Fechar");

        btnDesabilitarTodos.setBackground(new Color(160, 40, 40));

        btnToggle.addActionListener(e          -> alternarSelecionados());
        btnHabilitarTodos.addActionListener(e  -> habilitarTodos());
        btnDesabilitarTodos.addActionListener(e-> desabilitarTodos());
        btnFechar.addActionListener(e          -> dispose());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.BEGE);
        barra.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel dica = new JLabel(
            "  Selecione uma ou mais linhas (Ctrl+Click) e use os botões para alternar.");
        dica.setFont(AppColors.FONTE_PEQUENA);
        dica.setForeground(AppColors.CINZA_TEXTO);

        JPanel direita = UIHelper.painelBotoes(
            btnHabilitarTodos, btnDesabilitarTodos, btnToggle, btnFechar);
        direita.setBackground(AppColors.BEGE);

        barra.add(dica,    BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    // ── Rodapé com contadores ──────────────────────────────────────────────────

    private JPanel criarRodape() {
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 6));
        rodape.setBackground(AppColors.MARROM);
        rodape.setBorder(new EmptyBorder(4, 12, 4, 12));

        labelTotal        = new JLabel("Total: 0 pratos");
        labelDisponiveis  = new JLabel("✅ Disponíveis: 0");
        labelIndisponiveis= new JLabel("❌ Indisponíveis: 0");

        for (JLabel lbl : new JLabel[]{ labelTotal, labelDisponiveis, labelIndisponiveis }) {
            lbl.setFont(AppColors.FONTE_PEQUENA);
            lbl.setForeground(new Color(230, 210, 185));
            rodape.add(lbl);
        }
        return rodape;
    }

    // ─── LÓGICA DE NEGÓCIO ────────────────────────────────────────────────────

    /** Carrega todos os pratos do banco e atualiza a tabela. */
    private void carregarPratos() {
        try {
            todosOsPratos = pratoService.listarTodos();
            aplicarFiltros();
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar pratos:\n" + ex.getMessage());
        }
    }

    /** Aplica os filtros de categoria e status sobre a lista carregada. */
    private void aplicarFiltros() {
        modeloTabela.setRowCount(0);

        int idxCategoria = comboCategoria.getSelectedIndex();
        int idxStatus    = comboStatus.getSelectedIndex();

        CategoriaPrato categoriaFiltro = null;
        if (idxCategoria > 0) {
            categoriaFiltro = CategoriaPrato.values()[idxCategoria - 1];
        }

        int disponiveis   = 0;
        int indisponiveis = 0;

        for (Prato p : todosOsPratos) {
            // Filtro de categoria
            if (categoriaFiltro != null && p.getCategoria() != categoriaFiltro) continue;

            // Filtro de status
            if (idxStatus == 1 && !p.isDisponivel()) continue;
            if (idxStatus == 2 &&  p.isDisponivel()) continue;

            modeloTabela.addRow(new Object[]{
                p.getId(),
                p.getCodigo(),
                p.getNome(),
                p.getCategoria().getDescricao(),
                String.format("%.2f", p.getPreco()),
                p.isDisponivel() ? "✅ Disponível" : "❌ Indisponível"
            });

            if (p.isDisponivel()) disponiveis++; else indisponiveis++;
        }

        atualizarContadores(modeloTabela.getRowCount(), disponiveis, indisponiveis);
    }

    /**
     * Alterna a disponibilidade de todos os pratos selecionados na tabela.
     * Se uma linha está disponível, fica indisponível — e vice-versa.
     */
    private void alternarSelecionados() {
        int[] linhas = tabela.getSelectedRows();
        if (linhas.length == 0) {
            UIHelper.avisoDialog(this,
                "Selecione pelo menos uma linha para alternar.\n"
                + "Use Ctrl+Click para seleção múltipla.");
            return;
        }

        boolean confirma = UIHelper.confirmarDialog(this,
            "Alternar disponibilidade de " + linhas.length + " prato(s) selecionado(s)?");
        if (!confirma) return;

        int alterados = 0;
        for (int linha : linhas) {
            int id = (int) modeloTabela.getValueAt(linha, 0);
            Prato prato = buscarPratoCarregado(id);
            if (prato != null) {
                try {
                    pratoService.alterarDisponibilidade(id, !prato.isDisponivel());
                    alterados++;
                } catch (Exception ex) {
                    UIHelper.avisoDialog(this,
                        "Erro ao alterar prato ID " + id + ": " + ex.getMessage());
                }
            }
        }

        carregarPratos();
        UIHelper.sucessoDialog(this, alterados + " prato(s) atualizado(s) com sucesso.");
    }

    /** Habilita todos os pratos visíveis na tabela (com filtro aplicado). */
    private void habilitarTodos() {
        int total = modeloTabela.getRowCount();
        if (total == 0) {
            UIHelper.avisoDialog(this, "Nenhum prato encontrado com os filtros aplicados.");
            return;
        }

        boolean confirma = UIHelper.confirmarDialog(this,
            "Habilitar TODOS os " + total + " prato(s) exibidos?\n"
            + "Todos ficarão disponíveis no cardápio.");
        if (!confirma) return;

        int alterados = alterarDisponibilidadeTodos(true);
        carregarPratos();
        UIHelper.sucessoDialog(this, alterados + " prato(s) habilitado(s) com sucesso. ✅");
    }

    /** Desabilita todos os pratos visíveis na tabela (com filtro aplicado). */
    private void desabilitarTodos() {
        int total = modeloTabela.getRowCount();
        if (total == 0) {
            UIHelper.avisoDialog(this, "Nenhum prato encontrado com os filtros aplicados.");
            return;
        }

        boolean confirma = UIHelper.confirmarDialog(this,
            "Desabilitar TODOS os " + total + " prato(s) exibidos?\n"
            + "Nenhum ficará disponível para novos pedidos.");
        if (!confirma) return;

        int alterados = alterarDisponibilidadeTodos(false);
        carregarPratos();
        UIHelper.sucessoDialog(this, alterados + " prato(s) desabilitado(s). ❌");
    }

    /**
     * Percorre todas as linhas visíveis na tabela e aplica a disponibilidade.
     * @return quantidade de pratos efetivamente alterados
     */
    private int alterarDisponibilidadeTodos(boolean disponivel) {
        int alterados = 0;
        for (int i = 0; i < modeloTabela.getRowCount(); i++) {
            int id = (int) modeloTabela.getValueAt(i, 0);
            Prato prato = buscarPratoCarregado(id);
            if (prato != null && prato.isDisponivel() != disponivel) {
                try {
                    pratoService.alterarDisponibilidade(id, disponivel);
                    alterados++;
                } catch (Exception ex) {
                    // Segue tentando os demais
                }
            }
        }
        return alterados;
    }

    /** Busca um prato na lista carregada pelo id. */
    private Prato buscarPratoCarregado(int id) {
        for (Prato p : todosOsPratos) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    private void atualizarContadores(int total, int disponiveis, int indisponiveis) {
        labelTotal.setText("Total: " + total + " prato(s)");
        labelDisponiveis.setText("✅ Disponíveis: " + disponiveis);
        labelIndisponiveis.setText("❌ Indisponíveis: " + indisponiveis);
    }
}
