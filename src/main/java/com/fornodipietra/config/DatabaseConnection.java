package com.fornodipietra.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton responsável pela conexão com o banco de dados MySQL.
 *
 * Lê as credenciais de src/main/resources/db.properties.
 * Garante uma única instância de conexão durante toda a execução.
 * Reconecta automaticamente se a conexão estiver fechada ou inválida.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String user;
    private String password;

    // ─── Construtor privado ───────────────────────────────────────────────────

    private DatabaseConnection() {
        carregarPropriedades();
    }

    // ─── Instância única (thread-safe com synchronized) ───────────────────────

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ─── Leitura do arquivo de configuração ──────────────────────────────────

    private void carregarPropriedades() {
        Properties props = new Properties();

        try (InputStream input = getClass()
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new RuntimeException(
                    "Arquivo db.properties não encontrado em resources/."
                );
            }

            props.load(input);
            this.url      = props.getProperty("db.url");
            this.user     = props.getProperty("db.user");
            this.password = props.getProperty("db.password");

        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler db.properties: " + e.getMessage(), e);
        }
    }

    // ─── Obter conexão (reconecta se necessário) ──────────────────────────────

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                connection = DriverManager.getConnection(url, user, password);
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Não foi possível conectar ao banco de dados.\n"
                + "Verifique se o Docker está rodando (docker-compose up -d).\n"
                + "Detalhe: " + e.getMessage(), e
            );
        }
        return connection;
    }

    // ─── Fechar conexão (chamar ao encerrar o sistema) ────────────────────────

    public void fechar() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Aviso: erro ao fechar conexão — " + e.getMessage());
            } finally {
                connection = null;
            }
        }
    }

    // ─── Teste de conectividade (usado na inicialização do sistema) ───────────

    public boolean testarConexao() {
        try {
            Connection conn = getConnection();
            return conn != null && !conn.isClosed() && conn.isValid(3);
        } catch (Exception e) {
            return false;
        }
    }
}
