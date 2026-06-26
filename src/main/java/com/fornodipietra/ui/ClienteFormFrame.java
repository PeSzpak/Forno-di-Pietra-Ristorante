package com.fornodipietra.ui;

import com.fornodipietra.model.Cliente;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.TierFidelidade;
import com.fornodipietra.service.ClienteService;
import com.fornodipietra.service.FidelidadeService;
import com.fornodipietra.util.TierUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Tela de CRUD completo de Clientes.
 *
 * Diferencial em relação ao PratoFormFrame:
 *  - Painel lateral de fidelidade Carta d'Oro — exibe tier,
 *    pontos acumulados e benefícios disponíveis ao selecionar um cliente.
 *  - totalPedidos exibido na tabela como campo informativo (somente leitura).
 *
 * Layout:
 *  NORTE  — cabeçalho vermelho com título
 *  CENTRO — split: tabela (esquerda) | formulário + fidelidade (direita)
 *  SUL    — barra de botões de ação
 */
public class ClienteFormFrame extends JFrame {

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final ClienteService    clienteService    = new ClienteService();
    private final FidelidadeService fidelidadeService = new FidelidadeService();

    // ─── Estado ───────────────────────────────────────────────────────────────
    private int           idSelecionado = -1;
    private List<Cliente> clientesCarregados;

    // ─── Tabela ───────────────────────────────────────────────────────────────
    private DefaultTableModel modeloTabela;
    private JTable            tabela;

    // ─── Campos do formulário ─────────────────────────────────────────────────
    private JTextField campoNome;
    private JTextField campoTelefone;
    private JTextField campoEmail;

    // ─── Painel de fidelidade ─────────────────────────────────────────────────
    private JLabel labelTierIcone;
    private JLabel labelTierNome;
    private JLabel labelPontos;
    private JLabel labelBeneficios;
    private JPanel painelTierIndicador;

    // ─── Labels de status ─────────────────────────────────────────────────────
    private JLabel labelStatus;
    private JLabel labelModo;

    // ─── Botões ───────────────────────────────────────────────────────────────
    private JButton btnSalvar;
    private JButton btnExcluir;
    private JButton btnNovo;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public ClienteFormFrame(JFrame pai) {
        setTitle("Forno di Pietra — Gestão de Clientes");
        setSize(980, 620);
        setMinimumSize(new Dimension(820, 520));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarTabela();
        modoNovoCadastro();
    }

    // ─── CONSTRUÇÃO DA UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(criarCabecalho(),   BorderLayout.NORTH);
        add(criarCorpo(),       BorderLayout.CENTER);
        add(criarBarraBotoes(), BorderLayout.SOUTH);
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AppColors.VERMELHO);
        cab.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("👤  Gestão de Clientes");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        labelModo = new JLabel("Modo: Novo cadastro");
        labelModo.setFont(AppColors.FONTE_PEQUENA);
        labelModo.setForeground(new Color(255, 220, 200));

        cab.add(titulo,    BorderLayout.WEST);
        cab.add(labelModo, BorderLayout.EAST);
        return cab;
    }

    // ── Corpo: tabela + formulário + fidelidade ────────────────────────────────

    private JSplitPane criarCorpo() {
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            criarPainelTabela(),
            criarPainelDireito()
        );
        split.setDividerLocation(430);
        split.setResizeWeight(0.45);
        split.setBorder(new EmptyBorder(10, 10, 0, 10));
        split.setBackground(AppColors.CREME);
        return split;
    }

    // ── Painel da tabela ──────────────────────────────────────────────────────

    private JPanel criarPainelTabela() {
        JPanel painel = UIHelper.painelFormulario("Clientes Cadastrados");
        painel.setLayout(new BorderLayout(0, 8));

        // Filtro rápido
        JPanel barraBusca = new JPanel(new BorderLayout(6, 0));
        barraBusca.setOpaque(false);
        JTextField campoBusca = UIHelper.campo(15);
        JButton btnBuscar = UIHelper.botaoSecundario("🔍");
        barraBusca.add(UIHelper.label("Filtrar:"), BorderLayout.WEST);
        barraBusca.add(campoBusca,                 BorderLayout.CENTER);
        barraBusca.add(btnBuscar,                  BorderLayout.EAST);

        // Tabela
        String[] colunas = {"ID", "Nome", "Telefone", "E-mail", "Pedidos"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabela = UIHelper.tabela(modeloTabela);

        // Larguras das colunas
        tabela.getColumnModel().getColumn(0).setMaxWidth(40);    // ID
        tabela.getColumnModel().getColumn(1).setPreferredWidth(160); // Nome
        tabela.getColumnModel().getColumn(2).setPreferredWidth(110); // Telefone
        tabela.getColumnModel().getColumn(3).setPreferredWidth(170); // E-mail
        tabela.getColumnModel().getColumn(4).setMaxWidth(60);   // Pedidos

        // Selecionar linha preenche o formulário e o painel de fidelidade
        tabela.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int linha = tabela.getSelectedRow();
                if (linha >= 0) preencherFormulario(linha);
            }
        });

        btnBuscar.addActionListener(e -> filtrarTabela(campoBusca.getText()));
        campoBusca.addActionListener(e -> filtrarTabela(campoBusca.getText()));

        labelStatus = new JLabel(" ");
        labelStatus.setFont(AppColors.FONTE_PEQUENA);
        labelStatus.setForeground(AppColors.CINZA_TEXTO);

        painel.add(barraBusca,             BorderLayout.NORTH);
        painel.add(UIHelper.scroll(tabela), BorderLayout.CENTER);
        painel.add(labelStatus,            BorderLayout.SOUTH);
        return painel;
    }

    // ── Painel direito: formulário + fidelidade ────────────────────────────────

    private JPanel criarPainelDireito() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(AppColors.CREME);
        painel.add(criarPainelFormulario(), BorderLayout.CENTER);
        painel.add(criarPainelFidelidade(), BorderLayout.SOUTH);
        return painel;
    }

    // ── Painel do formulário ──────────────────────────────────────────────────

    private JPanel criarPainelFormulario() {
        JPanel painel = UIHelper.painelFormulario("Dados do Cliente");
        painel.setLayout(new BorderLayout(0, 0));

        JPanel campos = new JPanel(new GridBagLayout());
        campos.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(7, 4, 7, 4);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        // ── Nome ─────────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        campos.add(UIHelper.label("Nome completo *:"), gbc);
        campoNome = UIHelper.campo(22);
        campoNome.setToolTipText("Mínimo 3 caracteres, não pode ser só números");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoNome, gbc);

        // ── Telefone ──────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        campos.add(UIHelper.label("Telefone *:"), gbc);
        campoTelefone = UIHelper.campo(16);
        campoTelefone.setToolTipText("Ex: (41) 99999-9999");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoTelefone, gbc);

        // ── E-mail ────────────────────────────────────────────────────────
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        campos.add(UIHelper.label("E-mail *:"), gbc);
        campoEmail = UIHelper.campo(22);
        campoEmail.setToolTipText("Deve ser único no sistema");
        gbc.gridx = 1; gbc.weightx = 1;
        campos.add(campoEmail, gbc);

        painel.add(campos, BorderLayout.CENTER);

        JLabel nota = new JLabel("  * Campos obrigatórios");
        nota.setFont(AppColors.FONTE_PEQUENA);
        nota.setForeground(AppColors.CINZA_TEXTO);
        painel.add(nota, BorderLayout.SOUTH);

        return painel;
    }

    // ── Painel de fidelidade Carta d'Oro ──────────────────────────────────────

    private JPanel criarPainelFidelidade() {
        JPanel painel = UIHelper.painelFormulario("🥇 Carta d'Oro — Fidelidade");
        painel.setLayout(new BorderLayout(0, 6));

        // Indicador de tier — barra colorida no topo
        painelTierIndicador = new JPanel();
        painelTierIndicador.setPreferredSize(new Dimension(0, 6));
        painelTierIndicador.setBackground(AppColors.CINZA_CLARO);

        // Linha 1: ícone do tier + nome
        JPanel linhaTier = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        linhaTier.setOpaque(false);
        labelTierIcone = new JLabel("—");
        labelTierIcone.setFont(new Font("SansSerif", Font.BOLD, 22));
        labelTierNome  = new JLabel("Selecione um cliente");
        labelTierNome.setFont(AppColors.FONTE_SUBTITULO);
        labelTierNome.setForeground(AppColors.CINZA_TEXTO);
        linhaTier.add(labelTierIcone);
        linhaTier.add(labelTierNome);

        // Linha 2: pontos
        labelPontos = new JLabel("Pontos: —");
        labelPontos.setFont(AppColors.FONTE_LABEL);
        labelPontos.setForeground(AppColors.TEXTO);
        labelPontos.setBorder(new EmptyBorder(0, 4, 0, 0));

        // Linha 3: benefícios
        labelBeneficios = new JLabel(" ");
        labelBeneficios.setFont(AppColors.FONTE_PEQUENA);
        labelBeneficios.setForeground(AppColors.CINZA_TEXTO);
        labelBeneficios.setBorder(new EmptyBorder(0, 4, 0, 0));

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setOpaque(false);
        conteudo.add(painelTierIndicador);
        conteudo.add(Box.createVerticalStrut(6));
        conteudo.add(linhaTier);
        conteudo.add(Box.createVerticalStrut(4));
        conteudo.add(labelPontos);
        conteudo.add(Box.createVerticalStrut(4));
        conteudo.add(labelBeneficios);

        painel.add(conteudo, BorderLayout.CENTER);
        return painel;
    }

    // ── Barra de botões ───────────────────────────────────────────────────────

    private JPanel criarBarraBotoes() {
        btnNovo    = UIHelper.botaoSecundario("➕ Novo");
        btnSalvar  = UIHelper.botaoPrimario("💾 Salvar");
        btnExcluir = UIHelper.botaoPrimario("🗑️ Excluir");
        JButton btnLimpar  = UIHelper.botaoNeutro("🧹 Limpar");
        JButton btnFechar  = UIHelper.botaoNeutro("✖ Fechar");

        btnExcluir.setBackground(new Color(160, 40, 40));

        btnNovo.addActionListener(e   -> modoNovoCadastro());
        btnSalvar.addActionListener(e -> salvar());
        btnExcluir.addActionListener(e-> excluir());
        btnLimpar.addActionListener(e -> { limparFormulario(); modoNovoCadastro(); });
        btnFechar.addActionListener(e -> dispose());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.BEGE);
        barra.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel dica = new JLabel("Selecione um cliente para ver sua fidelidade e editar.");
        dica.setFont(AppColors.FONTE_PEQUENA);
        dica.setForeground(AppColors.CINZA_TEXTO);

        JPanel direita = UIHelper.painelBotoes(btnNovo, btnLimpar, btnExcluir, btnSalvar, btnFechar);
        direita.setBackground(AppColors.BEGE);

        barra.add(dica,    BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);
        return barra;
    }

    // ─── LÓGICA DE NEGÓCIO ────────────────────────────────────────────────────

    private void carregarTabela() {
        modeloTabela.setRowCount(0);
        try {
            clientesCarregados = clienteService.listarTodos();
            for (Cliente c : clientesCarregados) {
                modeloTabela.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getTelefone(),
                    c.getEmail(),
                    c.getTotalPedidos()
                });
            }
            atualizarStatus(clientesCarregados.size() + " cliente(s) cadastrado(s).");
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar clientes:\n" + ex.getMessage());
        }
    }

    private void filtrarTabela(String texto) {
        modeloTabela.setRowCount(0);
        if (clientesCarregados == null) return;
        String filtro = texto.trim().toLowerCase();
        int encontrados = 0;
        for (Cliente c : clientesCarregados) {
            if (filtro.isEmpty()
                || c.getNome().toLowerCase().contains(filtro)
                || c.getEmail().toLowerCase().contains(filtro)
                || c.getTelefone().contains(filtro)) {
                modeloTabela.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getTelefone(),
                    c.getEmail(),
                    c.getTotalPedidos()
                });
                encontrados++;
            }
        }
        atualizarStatus(encontrados + " cliente(s) encontrado(s).");
    }

    private void preencherFormulario(int linhaSelecionada) {
        if (clientesCarregados == null || linhaSelecionada < 0) return;

        int idDaLinha = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
        Cliente cliente = null;
        for (Cliente c : clientesCarregados) {
            if (c.getId() == idDaLinha) { cliente = c; break; }
        }
        if (cliente == null) return;

        idSelecionado = cliente.getId();
        campoNome.setText(cliente.getNome());
        campoTelefone.setText(cliente.getTelefone());
        campoEmail.setText(cliente.getEmail());

        labelModo.setText("Modo: Edição — ID " + idSelecionado);
        btnExcluir.setEnabled(true);

        // Carrega painel de fidelidade
        atualizarPainelFidelidade(idSelecionado);
    }

    /** Atualiza o painel Carta d'Oro com os dados do cliente selecionado. */
    private void atualizarPainelFidelidade(int clienteId) {
        try {
            Fidelidade fid = fidelidadeService.buscarPorCliente(clienteId);
            TierFidelidade tier = fid.getTier();

            // Ícone e nome do tier
            switch (tier) {
                case BRONZE:
                    labelTierIcone.setText("🥉");
                    labelTierNome.setForeground(AppColors.BRONZE);
                    painelTierIndicador.setBackground(AppColors.BRONZE);
                    break;
                case PRATA:
                    labelTierIcone.setText("🥈");
                    labelTierNome.setForeground(AppColors.PRATA);
                    painelTierIndicador.setBackground(AppColors.PRATA);
                    break;
                case OURO:
                    labelTierIcone.setText("🥇");
                    labelTierNome.setForeground(AppColors.OURO);
                    painelTierIndicador.setBackground(AppColors.OURO);
                    break;
            }

            labelTierNome.setText(tier.getDescricao());
            labelPontos.setText("Pontos: " + fid.getPontosAcumulados()
                + "  |  Faltam " + fid.pontosParaProximoTier()
                + " para o próximo tier");
            labelBeneficios.setText(TierUtil.beneficiosDisponiveis(fid));

        } catch (Exception ex) {
            // Não crítico — apenas limpa o painel
            limparPainelFidelidade();
        }
    }

    private void limparPainelFidelidade() {
        labelTierIcone.setText("—");
        labelTierNome.setText("Selecione um cliente");
        labelTierNome.setForeground(AppColors.CINZA_TEXTO);
        labelPontos.setText("Pontos: —");
        labelBeneficios.setText(" ");
        painelTierIndicador.setBackground(AppColors.CINZA_CLARO);
    }

    private void modoNovoCadastro() {
        idSelecionado = -1;
        tabela.clearSelection();
        limparFormulario();
        limparPainelFidelidade();
        labelModo.setText("Modo: Novo cadastro");
        btnExcluir.setEnabled(false);
    }

    private void limparFormulario() {
        campoNome.setText("");
        campoTelefone.setText("");
        campoEmail.setText("");
        campoNome.requestFocus();
    }

    private void salvar() {
        try {
            String nome      = campoNome.getText();
            String telefone  = campoTelefone.getText();
            String email     = campoEmail.getText();

            if (idSelecionado == -1) {
                clienteService.cadastrar(nome, telefone, email);
                UIHelper.sucessoDialog(this,
                    "Cliente cadastrado com sucesso!\n"
                    + "Registro de fidelidade Carta d'Oro criado automaticamente. 🥉");
            } else {
                clienteService.atualizar(idSelecionado, nome, telefone, email);
                UIHelper.sucessoDialog(this, "Cliente atualizado com sucesso!");
            }

            carregarTabela();
            modoNovoCadastro();

        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro inesperado:\n" + ex.getMessage());
        }
    }

    private void excluir() {
        if (idSelecionado == -1) {
            UIHelper.avisoDialog(this, "Selecione um cliente na tabela para excluir.");
            return;
        }

        int linha = tabela.getSelectedRow();
        String nomeCliente = (linha >= 0)
            ? (String) modeloTabela.getValueAt(linha, 1)
            : "ID " + idSelecionado;

        boolean confirma = UIHelper.confirmarDialog(this,
            "Deseja excluir o cliente:\n\"" + nomeCliente + "\"?\n\n"
            + "O cadastro de fidelidade também será removido.\n"
            + "Esta ação não pode ser desfeita.");
        if (!confirma) return;

        try {
            clienteService.excluir(idSelecionado);
            UIHelper.sucessoDialog(this, "Cliente excluído com sucesso.");
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
