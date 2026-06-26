package com.fornodipietra.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Tela: FidelidadeFrame
 * Stub — implementação completa nos próximos commits.
 */
public class FidelidadeFrame extends JFrame {

    public FidelidadeFrame(JFrame pai) {
        setTitle("Forno di Pietra — FidelidadeFrame");
        setSize(800, 560);
        setLocationRelativeTo(pai);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(AppColors.CREME);

        JLabel aviso = new JLabel("Em construção — próximo commit 🍕", SwingConstants.CENTER);
        aviso.setFont(AppColors.FONTE_SUBTITULO);
        aviso.setForeground(AppColors.CINZA_TEXTO);
        add(aviso, BorderLayout.CENTER);
    }
}
