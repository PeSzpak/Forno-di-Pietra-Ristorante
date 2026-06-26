package com.fornodipietra.ui;

import com.fornodipietra.model.CategoriaPrato;
import com.fornodipietra.model.Prato;
import com.fornodipietra.service.PratoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tela de CRUD completo de Pratos.
 *
 * Layout:
 *  NORTE  — cabeçalho com título
 *  CENTRO — split: tabela (esquerda) | formulário (direita)
 *  SUL    — barra de botões de ação
 *
 * Fluxo:
 *  - Ao abrir: carrega todos os pratos na tabela
 *  - Selecionar linha: preenche o formulário (modo edição)
 *  - Botão Novo: limpa o formulário (modo cadastro)
 *  - Botão Salvar: cadastra ou atualiza conforme o modo
 *  - Botão Excluir: confirma e exclui (com bloqueio de integridade referencial)
 *  - Botão Limpar: limpa o formulário sem alterar a tabela
 */
public class PratoFormFrame extends JFrame {

    // ─── Serviço ──────────────────────────────────────────────────────────────
    private final PratoService pratoService = new PratoService();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private int    idSelecionado = -1; // -1 = modo cadastro, >0 = modo edição
    private List<Prato> pratosCarregados;

    // ─── Tabela ───────────────────────────────────────────────────────────────
    private DefaultTableModel modeloTabela;
    private JTable            tabela;

    // ─── Campos do formulário ─────────────────────────────────────────────────
    private JTextField   campoCodigo;
    private JTextField   campoNome;
    private JTextField   campoPreco;
    private JTextArea    areaIngredientes;
    private JComboBox<CategoriaPrato> comboCategoria;
    private JCheckBox    checkDisponivel;

    // ─── Labels de status ─────────────────────────────────────────────────────
    private JLabel labelStatus;
    private JLabel labelModo;

    // ─── Botões ───────────────────────────────────────────────────────────────
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnNovo;
    private JButton btnLimpar;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public PratoFormFrame(JFrame pai) {
        setTitle("Forno di Pietra — Gestão de Pratos");
        setSize(960, 600);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarTabela();
        modoNovoCadastro();
    }

    // ─── CONSTRUÇÃO DA UI ────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(criarCabecalho(),  BorderLayout.NORTH);
        add(criarCorpo(),      BorderLayout.CENTER);
        add(criarBarraBotoes(),BorderLayout.SOUTH);
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AppColors.VERMELHO);
        cab.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("🍽️  Gestão de Pratos");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        labelModo = new JLabel("Modo: Novo cadastro");
        labelModo.setFont(AppColors.FONTE_PEQUENA);
        labelModo.setForeground(new Color(255, 220, 200));

        cab.add(titulo,    BorderLayout.WEST);
        cab.add(labelModo, BorderLayout.EAST);
        return cab;
    }

    // ── Corpo: tabela + formulário ─────────────────────────────────────────────

    private JSplitPane criarCorpo() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            criarPainelTabela(),
            criarPainelFormulario()
        );
        split.setDividerLocation(420);
        split.setResizeWeight(0.5);
        split.setBorder(new EmptyBorder(10, 10, 0, 10));
        split.setBackground(AppColors.CREME);
        return split;
    }

    // ── Painel da tabela ──────────────────────────────────────────────────────

    private JPanel criarPainelTabela() {
        JPanel painel = UIHelper.painelFormulario("Cardápio Cadastrado");
        painel.setLayout(new BorderLayout(0, 8));

        // Barra de pesquisa/filtro rápido
        JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
        barraBusca.setOpaque(false);
        JTextField campoBusca = UIHelper.campo(15);
        campoBusca.putClientProperty("placeholder", "Buscar por nome...");
        JButton btnBuscar = UIHelper.botaoSecundario("🔍");
        barraBusca.add(UIHelper.label("Filtrar:"), BorderLayout.WEST);
        barraBusca.add(campoBusca, BorderLayout.CENTER);
        barraBusca.add(btnBuscar,  BorderLayout.EAST);

        // Tabela
        String[] colunas = {"ID", "Código", "Nome", "Preço", "Categoria", "Disp."};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                return c == 5 ? Boolean.class : Object.class;
            }
        };
        tabela = UIHelper.tabela(modeloTabela);

        // Larguras das colunas
        tabela.getColumnModel().getColumn(0).setMaxWidth(40);   // ID
        tabela.getColumnModel().getColumn(1).setPreferredWidth(70);  // Código
        tabela.getColumnModel().getColumn(2).setPreferredWidth(160); // Nome
        tabela.getColumnModel().getColumn(3).setPreferredWidth(70);  // Preço
        tabela.getColumnModel().getColumn(4).setPreferredWidth(100); // Categoria
        tabela.getColumnModel().getColumn(5).setMaxWidth(50);   // Disp.

        // Selecionar linha preenche o formulário
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabela.getSelectedRow();
                if (linha >= 0) {
                    preencherFormulario(linha);
                }
            }
        });

        // Filtro de busca
        btnBuscar.addActionListener(e -> filtrarTabela(campoBusca.getText()));
        campoBusca.addActionListener(e -> filtrarTabela(campoBusca.getText()));

        labelStatus = new JLabel(" ");
        labelStatus.setFont(AppColors.FONTE_PEQUENA);
        labelStatus.setForeground(AppColors.CINZA_TEXTO);

        painel.add(barraBusca,            BorderLayout.NORTH);
        painel.add(UIHelper.scroll(tabela), BorderLayout.CENTER);
        painel.add(labelStatus,           BorderLayout.SOUTH);
        return painel;
    }

    // ── Painel do formulário ──────────────────────────────────────────────────

    private JPanel criarPainelFormulario() {
        JPanel painel = UIHelper.painelFormulario("Dados do Prato");
        painel.setLayout(new BorderLayout(0, 0));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 4, 5, 4);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // ── Linha 0: Código ───────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        campos.add(UIHelper.label("Código *:"), gbc);
        campoCodigo = UIHelper.campo(8);
        campoCodigo.setToolTipText("Ex: PIZ001  (máx 10 chars, sem espaços)");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoCodigo, gbc);

        // ── Linha 1: Nome ────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        campos.add(UIHelper.label("Nome *:"), gbc);
        campoNome = UIHelper.campo(20);
        campoNome.setToolTipText("Mínimo 3 caracteres");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoNome, gbc);

        // ── Linha 2: Preço ────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        campos.add(UIHelper.label("Preço (R$) *:"), gbc);
        campoPreco = UIHelper.campo(10);
        campoPreco.setToolTipText("Ex: 49,90");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoPreco, gbc);

        // ── Linha 3: Categoria ────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        campos.add(UIHelper.label("Categoria *:"), gbc);
        comboCategoria = new JComboBox<>(CategoriaPrato.values());
        comboCategoria.setFont(AppColors.FONTE_CAMPO);
        comboCategoria.setBackground(AppColors.BRANCO);
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(comboCategoria, gbc);

        // ── Linha 4: Disponível ───────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        campos.add(UIHelper.label("Disponível:"), gbc);
        checkDisponivel = new JCheckBox("Sim — prato ativo no cardápio");
        checkDisponivel.setFont(AppColors.FONTE_LABEL);
        checkDisponivel.setOpaque(false);
        checkDisponivel.setSelected(true);
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(checkDisponivel, gbc);

        // ── Linha 5: Ingredientes ─────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        campos.add(UIHelper.label("Ingredientes *:"), gbc);
        areaIngredientes = UIHelper.areaTexto(5, 20);
        areaIngredientes.setToolTipText("Máximo 500 caracteres");
        JScrollPane scrollIngr = UIHelper.scroll(areaIngredientes);
        scrollIngr.setPreferredSize(new Dimension(0, 100));
        gbc.gridx = 1; gbc.weightx = 1; gbc.fill = GridBagConstraints.BOTH;
        campos.add(scrollIngr, gbc);

        painel.add(campos, BorderLayout.CENTER);

        // Nota de campo obrigatório
        JLabel nota = new JLabel("  * Campos obrigatórios");
        nota.setFont(AppColors.FONTE_PEQUENA);
        nota.setForeground(AppColors.CINZA_TEXTO);
        painel.add(nota, BorderLayout.SOUTH);

        return painel;
    }

    // ── Barra de botões ───────────────────────────────────────────────────────

    private JPanel criarBarraBotoes() {
        btnNovo    = UIHelper.botaoSecundario("➕ Novo");
        btnSalvar  = UIHelper.botaoPrimario("💾 Salvar");
        btnExcluir = UIHelper.botaoPrimario("🗑️ Excluir");
        btnLimpar  = UIHelper.botaoNeutro("🧹 Limpar");
        JButton btnFechar = UIHelper.botaoNeutro("✖ Fechar");

        // Excluir começa em vermelho mais suave
        btnExcluir.setBackground(new Color(160, 40, 40));

        btnNovo.addActionListener(e    -> modoNovoCadastro());
        btnSalvar.addActionListener(e  -> salvar());
        btnExcluir.addActionListener(e -> excluir());
        btnLimpar.addActionListener(e  -> limparFormulario());
        btnFechar.addActionListener(e  -> dispose());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.BEGE);
        barra.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Dica de uso à esquerda
        JLabel dica = new JLabel("Selecione um prato na tabela para editar ou excluir.");
        dica.setFont(AppColors.FONTE_PEQUENA);
        dica.setForeground(AppColors.CINZA_TEXTO);

        JPanel direita = UIHelper.painelBotoes(btnNovo, btnLimpar, btnExcluir, btnSalvar, btnFechar);
        direita.setBackground(AppColors.BEGE);

        barra.add(dica,    BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    // ─── LÓGICA DE NEGÓCIO ────────────────────────────────────────────────────

    /** Carrega todos os pratos do banco na tabela. */
    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        try {
            pratosCarregados = pratoService.listarTodos();
            for (Prato p : pratosCarregados) {
                modeloTabela.addRow(new Object[]{
                    p.getId(),
                    p.getCodigo(),
                    p.getNome(),
                    String.format("R$ %.2f", p.getPreco()),
                    p.getCategoria().getDescricao(),
                    p.isDisponivel()
                });
            }
            atualizarStatus(pratosCarregados.size() + " prato(s) cadastrado(s).");
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar pratos:\n" + ex.getMessage());
        }
    }

    /** Filtra a tabela pelo texto digitado no campo de busca. */
    private void filtrarTabela(String texto) {
        modeloTabela.setRowCount(0);
        if (pratosCarregados == null) return;
        String filtro = texto.trim().toLowerCase();
        int encontrados = 0;
        for (Prato p : pratosCarregados) {
            if (filtro.isEmpty()
                || p.getNome().toLowerCase().contains(filtro)
                || p.getCodigo().toLowerCase().contains(filtro)) {
                modeloTabela.addRow(new Object[]{
                    p.getId(),
                    p.getCodigo(),
                    p.getNome(),
                    String.format("R$ %.2f", p.getPreco()),
                    p.getCategoria().getDescricao(),
                    p.isDisponivel()
                });
                encontrados++;
            }
        }
        atualizarStatus(encontrados + " prato(s) encontrado(s).");
    }

    /** Preenche o formulário com os dados da linha selecionada na tabela. */
    private void preencherFormulario(int linhaSelecionada) {
        if (pratosCarregados == null || linhaSelecionada < 0) return;

        // Mapeia linha exibida → índice real na lista (considerando filtro)
        int idDaLinha = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
        Prato prato = null;
        for (Prato p : pratosCarregados) {
            if (p.getId() == idDaLinha) { prato = p; break; }
        }
        if (prato == null) return;

        // Preenche os campos
        idSelecionado = prato.getId();
        campoCodigo.setText(prato.getCodigo());
        campoNome.setText(prato.getNome());
        campoPreco.setText(prato.getPreco().toPlainString().replace(".", ","));
        comboCategoria.setSelectedItem(prato.getCategoria());
        checkDisponivel.setSelected(prato.isDisponivel());
        areaIngredientes.setText(prato.getIngredientes());

        labelModo.setText("Modo: Edição — ID " + idSelecionado);
        btnExcluir.setEnabled(true);
    }

    /** Limpa os campos e coloca a tela em modo de novo cadastro. */
    private void modoNovoCadastro() {
        idSelecionado = -1;
        tabela.clearSelection();
        limparFormulario();
        labelModo.setText("Modo: Novo cadastro");
        btnExcluir.setEnabled(false);
    }

    private void limparFormulario() {
        campoCodigo.setText("");
        campoNome.setText("");
        campoPreco.setText("");
        comboCategoria.setSelectedIndex(0);
        checkDisponivel.setSelected(true);
        areaIngredientes.setText("");
        campoCodigo.requestFocus();
    }

    /** Salva (cadastra ou atualiza) conforme o modo atual. */
    private void salvar() {
        try {
            String codigo       = campoCodigo.getText();
            String nome         = campoNome.getText();
            String preco        = campoPreco.getText();
            String ingredientes = areaIngredientes.getText();
            CategoriaPrato cat  = (CategoriaPrato) comboCategoria.getSelectedItem();
            boolean disponivel  = checkDisponivel.isSelected();

            if (idSelecionado == -1) {
                // ── Modo cadastro ────────────────────────────────────────
                pratoService.cadastrar(codigo, nome, preco, ingredientes, cat);
                UIHelper.sucessoDialog(this, "Prato cadastrado com sucesso! 🍕");
            } else {
                // ── Modo edição ──────────────────────────────────────────
                pratoService.atualizar(idSelecionado, codigo, nome, preco,
                                       ingredientes, cat, disponivel);
                UIHelper.sucessoDialog(this, "Prato atualizado com sucesso!");
            }

            carregarTabela();
            modoNovoCadastro();

        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro inesperado:\n" + ex.getMessage());
        }
    }

    /** Exclui o prato selecionado após confirmação. */
    private void excluir() {
        if (idSelecionado == -1) {
            UIHelper.avisoDialog(this, "Selecione um prato na tabela para excluir.");
            return;
        }

        int linha = tabela.getSelectedRow();
        String nomePrato = (linha >= 0)
            ? (String) modeloTabela.getValueAt(linha, 2)
            : "ID " + idSelecionado;

        boolean confirma = UIHelper.confirmarDialog(this,
            "Deseja excluir o prato:\n\"" + nomePrato + "\"?\n\n"
            + "Esta ação não pode ser desfeita.");
        if (!confirma) return;

        try {
            pratoService.excluir(idSelecionado);
            UIHelper.sucessoDialog(this, "Prato excluído com sucesso.");
            carregarTabela();
            modoNovoCadastro();
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao excluir:\n" + ex.getMessage());
        }
    }

    private void atualizarStatus(String mensagem) {
        if (labelStatus != null) {
            labelStatus.setText(" " + mensagem);
        }
    }
}
