package com.fornodipietra.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Fábrica de componentes Swing padronizados.
 *
 * Centraliza a criação de botões, campos, tabelas e painéis com o
 * visual do Forno di Pietra. Todas as telas usam estes métodos
 * em vez de criar componentes do zero, garantindo consistência.
 */
public class UIHelper {

    // Construtor privado — classe utilitária
    private UIHelper() {}

    // ─── BOTÕES ───────────────────────────────────────────────────────────────

    /**
     * Botão primário — fundo vermelho, texto branco.
     * Usado para ações principais: Salvar, Finalizar, Registrar.
     */
    public static JButton botaoPrimario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(AppColors.FONTE_BOTAO);
        btn.setBackground(AppColors.VERMELHO);
        btn.setForeground(AppColors.BRANCO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        // Efeito hover
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(AppColors.VERMELHO_ESCURO);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(AppColors.VERMELHO);
            }
        });
        return btn;
    }

    /**
     * Botão secundário — fundo verde, texto branco.
     * Usado para: Novo, Editar, Adicionar.
     */
    public static JButton botaoSecundario(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(AppColors.FONTE_BOTAO);
        btn.setBackground(AppColors.VERDE);
        btn.setForeground(AppColors.BRANCO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(AppColors.VERDE_ESCURO);
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(AppColors.VERDE);
            }
        });
        return btn;
    }

    /**
     * Botão neutro — fundo cinza, texto escuro.
     * Usado para: Cancelar, Fechar, Voltar.
     */
    public static JButton botaoNeutro(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(AppColors.FONTE_BOTAO);
        btn.setBackground(new Color(180, 170, 155));
        btn.setForeground(AppColors.TEXTO);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(150, 140, 125));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(180, 170, 155));
            }
        });
        return btn;
    }

    /**
     * Botão de navegação do painel principal — grande, com ícone emoji.
     * Usado exclusivamente no MainFrame.
     */
    public static JButton botaoNavegacao(String emoji, String texto) {
        JButton btn = new JButton("<html><center><span style='font-size:24px'>"
                + emoji + "</span><br><b>" + texto + "</b></center></html>");
        btn.setFont(AppColors.FONTE_LABEL);
        btn.setBackground(AppColors.BRANCO);
        btn.setForeground(AppColors.TEXTO);
        btn.setFocusPainted(false);
        btn.setBorder(new LineBorder(AppColors.MARROM, 1, true));
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 110));

        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                btn.setBackground(AppColors.BEGE);
                btn.setBorder(new LineBorder(AppColors.VERMELHO, 2, true));
            }
            @Override public void mouseExited(MouseEvent e) {
                btn.setBackground(AppColors.BRANCO);
                btn.setBorder(new LineBorder(AppColors.MARROM, 1, true));
            }
        });
        return btn;
    }

    // ─── CAMPOS DE TEXTO ──────────────────────────────────────────────────────

    /** Campo de texto padrão com bordas arredondadas e fonte definida. */
    public static JTextField campo(int colunas) {
        JTextField campo = new JTextField(colunas);
        campo.setFont(AppColors.FONTE_CAMPO);
        campo.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(AppColors.MARROM, 1, true),
            new EmptyBorder(4, 8, 4, 8)
        ));
        campo.setBackground(AppColors.BRANCO);
        campo.setForeground(AppColors.TEXTO);
        return campo;
    }

    /** Área de texto multilinha padronizada (ingredientes, observações). */
    public static JTextArea areaTexto(int linhas, int colunas) {
        JTextArea area = new JTextArea(linhas, colunas);
        area.setFont(AppColors.FONTE_CAMPO);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBackground(AppColors.BRANCO);
        area.setForeground(AppColors.TEXTO);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        return area;
    }

    /** Scroll pane padronizado para JTextArea ou JTable. */
    public static JScrollPane scroll(Component componente) {
        JScrollPane scroll = new JScrollPane(componente);
        scroll.setBorder(new LineBorder(AppColors.MARROM, 1, true));
        scroll.getViewport().setBackground(AppColors.BRANCO);
        return scroll;
    }

    // ─── LABELS ───────────────────────────────────────────────────────────────

    /** Label de campo de formulário (texto à esquerda). */
    public static JLabel label(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppColors.FONTE_LABEL);
        label.setForeground(AppColors.TEXTO);
        return label;
    }

    /** Label de título de seção (negrito, maior). */
    public static JLabel labelTitulo(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(AppColors.FONTE_SUBTITULO);
        label.setForeground(AppColors.VERMELHO);
        return label;
    }

    // ─── TABELAS ──────────────────────────────────────────────────────────────

    /**
     * Tabela padrão com cabeçalho estilizado, linhas alternadas e
     * seleção de linha inteira. Não é editável pelo usuário.
     */
    public static JTable tabela(javax.swing.table.TableModel model) {
        JTable tabela = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // tabelas são somente leitura na UI
            }
        };

        tabela.setFont(AppColors.FONTE_TABELA);
        tabela.setRowHeight(26);
        tabela.setGridColor(AppColors.CINZA_CLARO);
        tabela.setSelectionBackground(new Color(180, 30, 30, 60));
        tabela.setSelectionForeground(AppColors.TEXTO);
        tabela.setShowGrid(true);
        tabela.setFillsViewportHeight(true);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Cabeçalho
        JTableHeader header = tabela.getTableHeader();
        header.setFont(AppColors.FONTE_CABECALHO);
        header.setBackground(AppColors.VERMELHO);
        header.setForeground(AppColors.BRANCO);
        header.setReorderingAllowed(false);

        // Linhas alternadas
        tabela.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
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
        });

        return tabela;
    }

    // ─── PAINÉIS ──────────────────────────────────────────────────────────────

    /** Painel com fundo creme e padding interno padrão. */
    public static JPanel painelPrincipal() {
        JPanel painel = new JPanel();
        painel.setBackground(AppColors.CREME);
        painel.setBorder(new EmptyBorder(16, 16, 16, 16));
        return painel;
    }

    /** Painel de formulário com borda e título embutido. */
    public static JPanel painelFormulario(String titulo) {
        JPanel painel = new JPanel();
        painel.setBackground(AppColors.BEGE);
        painel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                new LineBorder(AppColors.MARROM, 1, true),
                "  " + titulo + "  ",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                AppColors.FONTE_SUBTITULO,
                AppColors.MARROM
            ),
            new EmptyBorder(8, 12, 8, 12)
        ));
        return painel;
    }

    /** Painel de botões alinhado à direita. */
    public static JPanel painelBotoes(JButton... botoes) {
        JPanel painel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        painel.setBackground(AppColors.CREME);
        for (JButton btn : botoes) {
            painel.add(btn);
        }
        return painel;
    }

    // ─── SEPARADOR ────────────────────────────────────────────────────────────

    /** Linha separadora horizontal com cor da paleta. */
    public static JSeparator separador() {
        JSeparator sep = new JSeparator();
        sep.setForeground(AppColors.MARROM);
        sep.setBackground(AppColors.CREME);
        return sep;
    }

    // ─── DIÁLOGOS ────────────────────────────────────────────────────────────

    /** Exibe mensagem de erro padronizada. */
    public static void erroDialog(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem,
            "Forno di Pietra — Erro", JOptionPane.ERROR_MESSAGE);
    }

    /** Exibe mensagem de sucesso padronizada. */
    public static void sucessoDialog(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem,
            "Forno di Pietra — Sucesso", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Diálogo de confirmação — retorna true se o usuário confirmou.
     * Apêndice II — seção 5 (cancelamento exige confirmação).
     */
    public static boolean confirmarDialog(Component pai, String mensagem) {
        int resposta = JOptionPane.showConfirmDialog(pai, mensagem,
            "Forno di Pietra — Confirmação",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        return resposta == JOptionPane.YES_OPTION;
    }

    /** Exibe aviso informativo (sem erro). */
    public static void avisoDialog(Component pai, String mensagem) {
        JOptionPane.showMessageDialog(pai, mensagem,
            "Forno di Pietra — Aviso", JOptionPane.WARNING_MESSAGE);
    }
}
