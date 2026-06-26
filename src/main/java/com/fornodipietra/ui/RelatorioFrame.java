package com.fornodipietra.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Tela: RelatorioFrame
 * Stub — implementação completa no commit 13.
 */
public class RelatorioFrame extends JFrame {

    public RelatorioFrame(JFrame pai) {
        setTitle("Forno di Pietra — Relatórios");
        setSize(900, 600);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        JLabel aviso = new JLabel("Relatórios — Em construção 📊", SwingConstants.CENTER);
        aviso.setFont(AppColors.FONTE_SUBTITULO);
        aviso.setForeground(AppColors.CINZA_TEXTO);
        add(aviso, BorderLayout.CENTER);
    }

    /** Pré-seleciona um relatório pelo índice (usado pelo botão Histórico do MainFrame). */
    public void selecionarRelatorio(int indice) {
        // Implementado no commit 13
    }
}
