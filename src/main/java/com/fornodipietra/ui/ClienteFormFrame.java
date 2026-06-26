package com.fornodipietra.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Tela: ClienteFormFrame
 * Stub — implementação completa nos próximos commits.
 */
public class ClienteFormFrame extends JFrame {

    public ClienteFormFrame(JFrame pai) {
        setTitle("Forno di Pietra — ClienteFormFrame");
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
