package com.fornodipietra.ui;

import com.fornodipietra.model.Cliente;
import com.fornodipietra.model.Fidelidade;
import com.fornodipietra.model.ItemPedido;
import com.fornodipietra.model.Pedido;
import com.fornodipietra.model.Prato;
import com.fornodipietra.model.StatusPedido;
import com.fornodipietra.model.TierFidelidade;
import com.fornodipietra.service.ClienteService;
import com.fornodipietra.service.FidelidadeService;
import com.fornodipietra.service.PedidoService;
import com.fornodipietra.service.PratoService;
import com.fornodipietra.util.TierUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tela de Registro e Gestão de Pedidos.
 *
 * Organizada em duas abas via JTabbedPane:
 *
 *  Aba 1 — Novo Pedido:
 *   - Selecionar cliente (JComboBox) → exibe saldo de fidelidade
 *   - Selecionar prato disponível + quantidade → adicionar ao carrinho
 *   - Tabela do carrinho com subtotais e total ao vivo
 *   - Opção de resgate de desconto (Prata/Ouro) antes de confirmar
 *   - Botão Registrar Pedido → persiste e exibe fatura
 *
 *  Aba 2 — Pedidos em Aberto:
 *   - Tabela com todos os pedidos ABERTO do dia
 *   - Botões Finalizar e Cancelar com confirmação
 *   - Atualiza pontos Carta d'Oro ao finalizar
 */
public class PedidoFrame extends JFrame {

    // ─── Serviços ─────────────────────────────────────────────────────────────
    private final PedidoService     pedidoService     = new PedidoService();
    private final ClienteService    clienteService    = new ClienteService();
    private final PratoService      pratoService      = new PratoService();
    private final FidelidadeService fidelidadeService = new FidelidadeService();

    // ─── Estado do carrinho ────────────────────────────────────────────────────
    private final List<ItemPedido> carrinho = new ArrayList<>();
    private Cliente clienteSelecionado = null;
    private boolean descontoAplicado   = false;
    private double  percentualDesconto = 0.0;

    // ─── Componentes — Aba 1 (Novo Pedido) ───────────────────────────────────
    private JComboBox<String> comboClientes;
    private JLabel            labelFidelidade;
    private JLabel            labelTierBadge;
    private JComboBox<String> comboPratos;
    private JSpinner          spinnerQuantidade;
    private DefaultTableModel modeloCarrinho;
    private JTable            tabelaCarrinho;
    private JLabel            labelTotal;
    private JLabel            labelDesconto;
    private JTextArea         areaObservacao;
    private JButton           btnAplicarDesconto;
    private JButton           btnRegistrar;

    // ─── Componentes — Aba 2 (Pedidos em Aberto) ─────────────────────────────
    private DefaultTableModel modeloPedidos;
    private JTable            tabelaPedidos;
    private JLabel            labelStatusPedido;

    // ─── Dados carregados ─────────────────────────────────────────────────────
    private List<Cliente> clientes;
    private List<Prato>   pratosDisponiveis;
    private List<Pedido>  pedidosAbertos;

    // ─── CONSTRUTOR ───────────────────────────────────────────────────────────

    public PedidoFrame(JFrame pai) {
        setTitle("Forno di Pietra — Pedidos");
        setSize(1000, 680);
        setMinimumSize(new Dimension(860, 580));
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        construirUI();
        carregarDados();
    }

    // ─── CONSTRUÇÃO DA UI ─────────────────────────────────────────────────────

    private void construirUI() {
        setLayout(new BorderLayout());
        add(criarCabecalho(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.setFont(AppColors.FONTE_BOTAO);
        abas.setBackground(AppColors.CREME);
        abas.addTab("📋  Novo Pedido",      criarAbaNovoPedido());
        abas.addTab("📂  Pedidos em Aberto", criarAbaPedidosAbertos());

        // Ao trocar para aba de pedidos, recarrega
        abas.addChangeListener(e -> {
            if (abas.getSelectedIndex() == 1) {
                carregarPedidosAbertos();
            }
        });

        add(abas, BorderLayout.CENTER);
    }

    // ── Cabeçalho ─────────────────────────────────────────────────────────────

    private JPanel criarCabecalho() {
        JPanel cab = new JPanel(new BorderLayout());
        cab.setBackground(AppColors.VERMELHO);
        cab.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel titulo = new JLabel("📋  Registro de Pedidos");
        titulo.setFont(AppColors.FONTE_TITULO);
        titulo.setForeground(AppColors.BRANCO);

        JLabel sub = new JLabel("Novo pedido • Finalizar • Cancelar");
        sub.setFont(AppColors.FONTE_PEQUENA);
        sub.setForeground(new Color(255, 220, 200));

        cab.add(titulo, BorderLayout.WEST);
        cab.add(sub,    BorderLayout.EAST);
        return cab;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ABA 1 — NOVO PEDIDO
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarAbaNovoPedido() {
        JPanel aba = new JPanel(new BorderLayout(10, 10));
        aba.setBackground(AppColors.CREME);
        aba.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Split: seleção (esquerda) | carrinho (direita)
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            criarPainelSelecao(),
            criarPainelCarrinho()
        );
        split.setDividerLocation(400);
        split.setResizeWeight(0.4);
        split.setBackground(AppColors.CREME);

        aba.add(split, BorderLayout.CENTER);
        return aba;
    }

    // ── Painel de seleção: cliente + prato ────────────────────────────────────

    private JPanel criarPainelSelecao() {
        JPanel painel = new JPanel(new BorderLayout(0, 10));
        painel.setBackground(AppColors.CREME);

        // ── Bloco: cliente ────────────────────────────────────────────────
        JPanel blocoCliente = UIHelper.painelFormulario("1. Selecionar Cliente");
        blocoCliente.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        blocoCliente.add(UIHelper.label("Cliente:"), gbc);

        comboClientes = new JComboBox<>();
        comboClientes.setFont(AppColors.FONTE_CAMPO);
        gbc.gridx = 1; gbc.weightx = 1;
        blocoCliente.add(comboClientes, gbc);

        // Badge do tier
        labelTierBadge = new JLabel("—");
        labelTierBadge.setFont(new Font("SansSerif", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        blocoCliente.add(labelTierBadge, gbc);

        // Resumo de fidelidade
        labelFidelidade = new JLabel("Selecione um cliente");
        labelFidelidade.setFont(AppColors.FONTE_PEQUENA);
        labelFidelidade.setForeground(AppColors.CINZA_TEXTO);
        gbc.gridx = 1; gbc.weightx = 1;
        blocoCliente.add(labelFidelidade, gbc);

        // Botão de aplicar desconto (habilitado conforme tier)
        btnAplicarDesconto = UIHelper.botaoSecundario("🎁 Aplicar desconto Carta d'Oro");
        btnAplicarDesconto.setEnabled(false);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        blocoCliente.add(btnAplicarDesconto, gbc);

        comboClientes.addActionListener(e -> onClienteSelecionado());
        btnAplicarDesconto.addActionListener(e -> aplicarDesconto());

        // ── Bloco: prato ──────────────────────────────────────────────────
        JPanel blocoPrato = UIHelper.painelFormulario("2. Adicionar Prato ao Carrinho");
        blocoPrato.setLayout(new GridBagLayout());
        GridBagConstraints gbcP = new GridBagConstraints();
        gbcP.insets = new Insets(5, 4, 5, 4);
        gbcP.fill   = GridBagConstraints.HORIZONTAL;

        gbcP.gridx = 0; gbcP.gridy = 0; gbcP.weightx = 0;
        blocoPrato.add(UIHelper.label("Prato:"), gbcP);
        comboPratos = new JComboBox<>();
        comboPratos.setFont(AppColors.FONTE_CAMPO);
        gbcP.gridx = 1; gbcP.weightx = 1;
        blocoPrato.add(comboPratos, gbcP);

        gbcP.gridx = 0; gbcP.gridy = 1; gbcP.weightx = 0;
        blocoPrato.add(UIHelper.label("Quantidade:"), gbcP);
        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        spinnerQuantidade.setFont(AppColors.FONTE_CAMPO);
        gbcP.gridx = 1; gbcP.weightx = 1;
        blocoPrato.add(spinnerQuantidade, gbcP);

        JButton btnAdicionar = UIHelper.botaoSecundario("➕ Adicionar ao carrinho");
        gbcP.gridx = 0; gbcP.gridy = 2; gbcP.gridwidth = 2;
        blocoPrato.add(btnAdicionar, gbcP);
        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());

        // ── Bloco: observação ─────────────────────────────────────────────
        JPanel blocoObs = UIHelper.painelFormulario("3. Observação (opcional)");
        blocoObs.setLayout(new BorderLayout());
        areaObservacao = UIHelper.areaTexto(3, 20);
        blocoObs.add(UIHelper.scroll(areaObservacao), BorderLayout.CENTER);

        painel.add(blocoCliente, BorderLayout.NORTH);
        painel.add(blocoPrato,   BorderLayout.CENTER);
        painel.add(blocoObs,     BorderLayout.SOUTH);
        return painel;
    }

    // ── Painel do carrinho ────────────────────────────────────────────────────

    private JPanel criarPainelCarrinho() {
        JPanel painel = UIHelper.painelFormulario("Carrinho de Compras");
        painel.setLayout(new BorderLayout(0, 8));

        // Tabela do carrinho
        String[] colunas = {"Prato", "Qtd", "Preço Unit.", "Subtotal"};
        modeloCarrinho = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaCarrinho = UIHelper.tabela(modeloCarrinho);
        tabelaCarrinho.getColumnModel().getColumn(0).setPreferredWidth(200);
        tabelaCarrinho.getColumnModel().getColumn(1).setMaxWidth(50);
        tabelaCarrinho.getColumnModel().getColumn(2).setPreferredWidth(90);
        tabelaCarrinho.getColumnModel().getColumn(3).setPreferredWidth(90);

        // Rodapé com totais
        JPanel rodapeTotais = new JPanel(new GridLayout(3, 1, 2, 2));
        rodapeTotais.setBackground(AppColors.BEGE);
        rodapeTotais.setBorder(new EmptyBorder(8, 8, 8, 8));

        labelDesconto = new JLabel("Desconto Carta d'Oro: R$ 0,00");
        labelDesconto.setFont(AppColors.FONTE_LABEL);
        labelDesconto.setForeground(AppColors.VERDE);

        labelTotal = new JLabel("Total: R$ 0,00");
        labelTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        labelTotal.setForeground(AppColors.VERMELHO);

        JLabel pontosLabel = new JLabel("Pontos a ganhar: 0");
        pontosLabel.setFont(AppColors.FONTE_PEQUENA);
        pontosLabel.setForeground(AppColors.CINZA_TEXTO);

        rodapeTotais.add(labelDesconto);
        rodapeTotais.add(labelTotal);
        rodapeTotais.add(pontosLabel);

        // Botões do carrinho
        JButton btnRemover = UIHelper.botaoNeutro("🗑️ Remover item");
        JButton btnLimpar  = UIHelper.botaoNeutro("🧹 Limpar carrinho");
        btnRegistrar = UIHelper.botaoPrimario("✅ Registrar Pedido");

        btnRemover.addActionListener(e -> removerDoCarrinho());
        btnLimpar.addActionListener(e  -> limparCarrinho());
        btnRegistrar.addActionListener(e -> registrarPedido());

        JPanel botoesCar = UIHelper.painelBotoes(btnRemover, btnLimpar, btnRegistrar);
        botoesCar.setBackground(AppColors.CREME);

        painel.add(rodapeTotais,                    BorderLayout.SOUTH);

        JPanel sul = new JPanel(new BorderLayout());
        sul.setBackground(AppColors.CREME);
        sul.add(rodapeTotais, BorderLayout.NORTH);
        sul.add(botoesCar,    BorderLayout.SOUTH);

        painel.add(UIHelper.scroll(tabelaCarrinho), BorderLayout.CENTER);
        painel.add(sul, BorderLayout.SOUTH);

        return painel;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ABA 2 — PEDIDOS EM ABERTO
    // ══════════════════════════════════════════════════════════════════════════

    private JPanel criarAbaPedidosAbertos() {
        JPanel aba = new JPanel(new BorderLayout(0, 10));
        aba.setBackground(AppColors.CREME);
        aba.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Tabela de pedidos
        String[] colunas = {"ID", "Cliente", "Data/Hora", "Valor Total", "Status"};
        modeloPedidos = new DefaultTableModel(colunas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabelaPedidos = UIHelper.tabela(modeloPedidos);
        tabelaPedidos.getColumnModel().getColumn(0).setMaxWidth(50);
        tabelaPedidos.getColumnModel().getColumn(1).setPreferredWidth(180);
        tabelaPedidos.getColumnModel().getColumn(2).setPreferredWidth(140);
        tabelaPedidos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabelaPedidos.getColumnModel().getColumn(4).setPreferredWidth(90);

        // Status do pedido selecionado
        labelStatusPedido = new JLabel(" ");
        labelStatusPedido.setFont(AppColors.FONTE_PEQUENA);
        labelStatusPedido.setForeground(AppColors.CINZA_TEXTO);

        // Botões de ação
        JButton btnFinalizar  = UIHelper.botaoPrimario("✅ Finalizar Pedido");
        JButton btnCancelar   = UIHelper.botaoPrimario("❌ Cancelar Pedido");
        JButton btnAtualizar  = UIHelper.botaoSecundario("🔄 Atualizar lista");
        JButton btnVerItens   = UIHelper.botaoNeutro("🧾 Ver itens");

        btnCancelar.setBackground(new Color(160, 40, 40));

        btnFinalizar.addActionListener(e  -> finalizarPedido());
        btnCancelar.addActionListener(e   -> cancelarPedido());
        btnAtualizar.addActionListener(e  -> carregarPedidosAbertos());
        btnVerItens.addActionListener(e   -> verItensDoPedido());

        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(AppColors.BEGE);
        barra.setBorder(new EmptyBorder(8, 8, 8, 8));
        barra.add(labelStatusPedido, BorderLayout.WEST);
        barra.add(UIHelper.painelBotoes(btnVerItens, btnAtualizar, btnCancelar, btnFinalizar),
                  BorderLayout.EAST);

        aba.add(UIHelper.scroll(tabelaPedidos), BorderLayout.CENTER);
        aba.add(barra,                          BorderLayout.SOUTH);
        return aba;
    }

    // ─── LÓGICA — CARREGAMENTO DE DADOS ──────────────────────────────────────

    private void carregarDados() {
        carregarClientes();
        carregarPratos();
        carregarPedidosAbertos();
    }

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

    private void carregarPratos() {
        comboPratos.removeAllItems();
        try {
            pratosDisponiveis = pratoService.listarDisponiveis();
            for (Prato p : pratosDisponiveis) {
                comboPratos.addItem(p.getId() + " — " + p.getNome()
                    + "  (R$ " + String.format("%.2f", p.getPreco()) + ")");
            }
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar pratos:\n" + ex.getMessage());
        }
    }

    private void carregarPedidosAbertos() {
        modeloPedidos.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        try {
            pedidosAbertos = pedidoService.listarTodos();
            int abertos = 0;
            for (Pedido p : pedidosAbertos) {
                if (p.getStatus() == StatusPedido.ABERTO) {
                    // Busca nome do cliente
                    String nomeCliente = "Cliente " + p.getClienteId();
                    if (clientes != null) {
                        for (Cliente c : clientes) {
                            if (c.getId() == p.getClienteId()) {
                                nomeCliente = c.getNome();
                                break;
                            }
                        }
                    }
                    modeloPedidos.addRow(new Object[]{
                        p.getId(),
                        nomeCliente,
                        p.getDataHora() != null ? p.getDataHora().format(fmt) : "—",
                        String.format("R$ %.2f", p.getValorTotal()),
                        p.getStatus().getDescricao()
                    });
                    abertos++;
                }
            }
            labelStatusPedido.setText(" " + abertos + " pedido(s) em aberto.");
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao carregar pedidos:\n" + ex.getMessage());
        }
    }

    // ─── LÓGICA — NOVO PEDIDO ─────────────────────────────────────────────────

    private void onClienteSelecionado() {
        int idx = comboClientes.getSelectedIndex();
        if (idx <= 0) {
            clienteSelecionado = null;
            labelFidelidade.setText("Selecione um cliente");
            labelTierBadge.setText("—");
            btnAplicarDesconto.setEnabled(false);
            descontoAplicado   = false;
            percentualDesconto = 0.0;
            atualizarTotal();
            return;
        }

        clienteSelecionado = clientes.get(idx - 1);

        try {
            Fidelidade fid = fidelidadeService.buscarPorCliente(clienteSelecionado.getId());
            labelFidelidade.setText(TierUtil.resumoFidelidade(fid));

            switch (fid.getTier()) {
                case BRONZE:
                    labelTierBadge.setText("🥉");
                    labelTierBadge.setForeground(AppColors.BRONZE);
                    btnAplicarDesconto.setEnabled(false);
                    break;
                case PRATA:
                    labelTierBadge.setText("🥈");
                    labelTierBadge.setForeground(AppColors.PRATA);
                    btnAplicarDesconto.setEnabled(!descontoAplicado);
                    break;
                case OURO:
                    labelTierBadge.setText("🥇");
                    labelTierBadge.setForeground(AppColors.OURO);
                    btnAplicarDesconto.setEnabled(!descontoAplicado);
                    break;
            }
        } catch (Exception ex) {
            labelFidelidade.setText("Sem cadastro de fidelidade.");
            labelTierBadge.setText("—");
        }
    }

    private void adicionarAoCarrinho() {
        if (pratosDisponiveis == null || pratosDisponiveis.isEmpty()) {
            UIHelper.avisoDialog(this, "Nenhum prato disponível.");
            return;
        }
        int idxPrato = comboPratos.getSelectedIndex();
        if (idxPrato < 0) {
            UIHelper.avisoDialog(this, "Selecione um prato.");
            return;
        }

        Prato prato = pratosDisponiveis.get(idxPrato);
        int quantidade = (int) spinnerQuantidade.getValue();

        // Verifica se o prato já está no carrinho — soma quantidade
        for (ItemPedido item : carrinho) {
            if (item.getPratoId() == prato.getId()) {
                item.setQuantidade(item.getQuantidade() + quantidade);
                atualizarTabelaCarrinho();
                return;
            }
        }

        ItemPedido item = new ItemPedido(prato, quantidade);
        carrinho.add(item);
        atualizarTabelaCarrinho();
    }

    private void removerDoCarrinho() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha < 0) {
            UIHelper.avisoDialog(this, "Selecione um item do carrinho para remover.");
            return;
        }
        carrinho.remove(linha);
        atualizarTabelaCarrinho();
    }

    private void limparCarrinho() {
        if (carrinho.isEmpty()) return;
        if (UIHelper.confirmarDialog(this, "Limpar todos os itens do carrinho?")) {
            carrinho.clear();
            descontoAplicado   = false;
            percentualDesconto = 0.0;
            atualizarTabelaCarrinho();
            if (clienteSelecionado != null) {
                btnAplicarDesconto.setEnabled(true);
            }
        }
    }

    private void atualizarTabelaCarrinho() {
        modeloCarrinho.setRowCount(0);
        for (ItemPedido item : carrinho) {
            String nomePrato = item.getPrato() != null
                ? item.getPrato().getNome() : "Prato " + item.getPratoId();
            modeloCarrinho.addRow(new Object[]{
                nomePrato,
                item.getQuantidade(),
                String.format("R$ %.2f", item.getPrecoUnitario()),
                String.format("R$ %.2f", item.getSubtotal())
            });
        }
        atualizarTotal();
    }

    private void atualizarTotal() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ItemPedido item : carrinho) {
            subtotal = subtotal.add(item.getSubtotal());
        }

        BigDecimal valorFinal = subtotal;
        BigDecimal valorDescontoAplicado = BigDecimal.ZERO;

        if (descontoAplicado && percentualDesconto > 0) {
            valorDescontoAplicado = subtotal
                .multiply(BigDecimal.valueOf(percentualDesconto / 100))
                .setScale(2, java.math.RoundingMode.HALF_UP);
            valorFinal = subtotal.subtract(valorDescontoAplicado);
        }

        labelDesconto.setText(descontoAplicado
            ? String.format("Desconto Carta d'Oro (%s%%): - R$ %.2f",
                (int) percentualDesconto, valorDescontoAplicado)
            : "Desconto Carta d'Oro: não aplicado");
        labelDesconto.setForeground(descontoAplicado ? AppColors.VERDE : AppColors.CINZA_TEXTO);

        labelTotal.setText(String.format("Total: R$ %.2f", valorFinal));

        int pontosEstimados = TierUtil.calcularPontosGanhos(valorFinal);
        // Atualiza label de pontos se existir no painel
    }

    private void aplicarDesconto() {
        if (clienteSelecionado == null) {
            UIHelper.avisoDialog(this, "Selecione um cliente primeiro.");
            return;
        }
        if (descontoAplicado) {
            UIHelper.avisoDialog(this, "Desconto já aplicado a este pedido.");
            return;
        }

        try {
            Fidelidade fid = fidelidadeService.buscarPorCliente(clienteSelecionado.getId());
            String erro = TierUtil.validarElegibilidadeDesconto(fid);
            if (erro != null) { UIHelper.erroDialog(this, erro); return; }

            TierFidelidade tier = fid.getTier();
            percentualDesconto  = TierUtil.obterPercentualDesconto(tier);

            boolean confirma = UIHelper.confirmarDialog(this,
                "Aplicar desconto de " + (int) percentualDesconto + "% (Carta d'Oro "
                + tier.getDescricao() + ")?\n\n"
                + "Serão descontados " + TierUtil.PONTOS_PARA_DESCONTO
                + " pontos do saldo do cliente.\n"
                + "Esta ação será confirmada ao registrar o pedido.");
            if (!confirma) return;

            descontoAplicado = true;
            btnAplicarDesconto.setEnabled(false);
            atualizarTotal();
            UIHelper.sucessoDialog(this,
                "Desconto de " + (int) percentualDesconto
                + "% será aplicado ao registrar o pedido.");

        } catch (Exception ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        }
    }

    private void registrarPedido() {
        if (clienteSelecionado == null) {
            UIHelper.erroDialog(this, "Selecione um cliente para o pedido.");
            return;
        }
        if (carrinho.isEmpty()) {
            UIHelper.erroDialog(this, "Adicione pelo menos um prato ao carrinho.");
            return;
        }

        try {
            String obs = areaObservacao.getText();
            Pedido pedido = pedidoService.registrar(
                clienteSelecionado.getId(), carrinho, obs
            );

            // Se desconto foi solicitado, processa o resgate após registrar
            if (descontoAplicado) {
                try {
                    fidelidadeService.resgatarDesconto(clienteSelecionado.getId());
                } catch (Exception exDesc) {
                    UIHelper.avisoDialog(this,
                        "Pedido registrado, mas houve erro ao aplicar o desconto:\n"
                        + exDesc.getMessage());
                }
            }

            exibirFatura(pedido);
            limparEstadoNovoPedido();

        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao registrar pedido:\n" + ex.getMessage());
        }
    }

    /** Exibe a fatura do pedido em uma janela de diálogo. */
    private void exibirFatura(Pedido pedido) {
        StringBuilder fatura = new StringBuilder();
        fatura.append("════════════════════════════════════\n");
        fatura.append("       FORNO DI PIETRA RISTORANTE   \n");
        fatura.append("════════════════════════════════════\n");
        fatura.append(String.format("Pedido #%d\n", pedido.getId()));
        fatura.append(String.format("Cliente: %s\n", clienteSelecionado.getNome()));
        fatura.append("────────────────────────────────────\n");

        for (ItemPedido item : pedido.getItens()) {
            String nome = item.getPrato() != null
                ? item.getPrato().getNome() : "Item " + item.getPratoId();
            fatura.append(String.format("%-20s x%d  R$ %.2f\n",
                nome, item.getQuantidade(), item.getSubtotal()));
        }

        fatura.append("────────────────────────────────────\n");
        if (descontoAplicado) {
            fatura.append(String.format("Desconto Carta d'Oro: - %d%%\n",
                (int) percentualDesconto));
        }
        fatura.append(String.format("TOTAL:  R$ %.2f\n", pedido.getValorTotal()));
        fatura.append("════════════════════════════════════\n");
        fatura.append("  Finalize para acumular pontos!   \n");
        fatura.append(String.format("  Pontos estimados: +%d pts  🥇\n",
            TierUtil.calcularPontosGanhos(pedido.getValorTotal())));
        fatura.append("════════════════════════════════════\n");

        JTextArea areaFatura = new JTextArea(fatura.toString());
        areaFatura.setFont(new Font("Monospaced", Font.PLAIN, 12));
        areaFatura.setEditable(false);
        areaFatura.setBackground(AppColors.CREME);

        JOptionPane.showMessageDialog(this,
            new JScrollPane(areaFatura),
            "Fatura — Pedido #" + pedido.getId(),
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void limparEstadoNovoPedido() {
        carrinho.clear();
        descontoAplicado   = false;
        percentualDesconto = 0.0;
        comboClientes.setSelectedIndex(0);
        areaObservacao.setText("");
        atualizarTabelaCarrinho();
        carregarPedidosAbertos();
    }

    // ─── LÓGICA — PEDIDOS EM ABERTO ───────────────────────────────────────────

    private int getIdPedidoSelecionado() {
        int linha = tabelaPedidos.getSelectedRow();
        if (linha < 0) return -1;
        return (int) modeloPedidos.getValueAt(linha, 0);
    }

    private void finalizarPedido() {
        int pedidoId = getIdPedidoSelecionado();
        if (pedidoId == -1) {
            UIHelper.avisoDialog(this, "Selecione um pedido na tabela.");
            return;
        }

        boolean confirma = UIHelper.confirmarDialog(this,
            "Finalizar o pedido #" + pedidoId + "?\n\n"
            + "Os pontos de fidelidade serão acumulados automaticamente.");
        if (!confirma) return;

        try {
            pedidoService.finalizar(pedidoId);
            UIHelper.sucessoDialog(this,
                "Pedido #" + pedidoId + " finalizado!\nPontos Carta d'Oro acumulados. 🥇");
            carregarPedidosAbertos();
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao finalizar:\n" + ex.getMessage());
        }
    }

    private void cancelarPedido() {
        int pedidoId = getIdPedidoSelecionado();
        if (pedidoId == -1) {
            UIHelper.avisoDialog(this, "Selecione um pedido na tabela.");
            return;
        }

        boolean confirma = UIHelper.confirmarDialog(this,
            "Cancelar o pedido #" + pedidoId + "?\n\nEsta ação não pode ser desfeita.");
        if (!confirma) return;

        try {
            pedidoService.cancelar(pedidoId);
            UIHelper.sucessoDialog(this, "Pedido #" + pedidoId + " cancelado.");
            carregarPedidosAbertos();
        } catch (IllegalArgumentException ex) {
            UIHelper.erroDialog(this, ex.getMessage());
        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao cancelar:\n" + ex.getMessage());
        }
    }

    private void verItensDoPedido() {
        int pedidoId = getIdPedidoSelecionado();
        if (pedidoId == -1) {
            UIHelper.avisoDialog(this, "Selecione um pedido na tabela.");
            return;
        }

        try {
            List<ItemPedido> itens = pedidoService.buscarItensDoPedido(pedidoId);
            if (itens.isEmpty()) {
                UIHelper.avisoDialog(this, "Este pedido não possui itens.");
                return;
            }

            StringBuilder sb = new StringBuilder("Itens do Pedido #" + pedidoId + ":\n\n");
            BigDecimal total = BigDecimal.ZERO;
            for (ItemPedido item : itens) {
                String nome = item.getPrato() != null
                    ? item.getPrato().getNome() : "Item " + item.getPratoId();
                sb.append(String.format("• %-22s x%d  R$ %.2f\n",
                    nome, item.getQuantidade(), item.getSubtotal()));
                total = total.add(item.getSubtotal());
            }
            sb.append(String.format("\nTotal: R$ %.2f", total));

            JOptionPane.showMessageDialog(this, sb.toString(),
                "Itens do Pedido #" + pedidoId, JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            UIHelper.erroDialog(this, "Erro ao buscar itens:\n" + ex.getMessage());
        }
    }
}
