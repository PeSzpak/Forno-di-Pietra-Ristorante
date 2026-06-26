package com.fornodipietra.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Tela: PedidoFrame
 * Stub — implementação completa nos próximos commits.
 */
public class PedidoFrame extends JFrame {

    public PedidoFrame(JFrame pai) {
        setTitle("Forno di Pietra — PedidoFrame");
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
