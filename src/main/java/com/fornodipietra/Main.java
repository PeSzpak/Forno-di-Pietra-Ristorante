package com.fornodipietra;

import com.fornodipietra.config.DatabaseConnection;
import com.fornodipietra.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Ponto de entrada da aplicação Forno di Pietra Ristorante.
 *
 * Responsabilidades:
 *  1. Aplicar o Look and Feel do sistema operacional.
 *  2. Testar a conexão com o banco antes de abrir qualquer tela.
 *  3. Iniciar a interface gráfica na Event Dispatch Thread (EDT).
 *  4. Fechar a conexão ao encerrar a aplicação.
 */
public class Main {

    public static void main(String[] args) {

        // 1. Look and Feel nativo do SO (Windows/Linux/macOS)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fallback silencioso — usa o padrão Swing
        }

        // 2. Testar conexão com o banco antes de abrir a UI
        DatabaseConnection db = DatabaseConnection.getInstance();

        if (!db.testarConexao()) {
            JOptionPane.showMessageDialog(
                null,
                "Não foi possível conectar ao banco de dados.\n\n"
                + "Verifique se o Docker está rodando:\n"
                + "  docker-compose up -d\n\n"
                + "Aguarde ~15 segundos e tente novamente.",
                "Forno di Pietra — Erro de Conexão",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }

        // 3. Fechar conexão ao encerrar a JVM
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            DatabaseConnection.getInstance().fechar();
        }));

        // 4. Abrir interface na Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainFrame janela = new MainFrame();
            janela.setVisible(true);
        });
    }
}
