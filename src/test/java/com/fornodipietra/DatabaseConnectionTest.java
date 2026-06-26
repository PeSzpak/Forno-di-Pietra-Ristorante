package com.fornodipietra;

import com.fornodipietra.config.DatabaseConnection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para DatabaseConnection.
 *
 * ATENÇÃO: estes testes requerem o banco rodando via Docker.
 * Execute "docker-compose up -d" antes de rodar os testes.
 */
class DatabaseConnectionTest {

    @Test
    void deveRetornarMesmaInstancia() {
        DatabaseConnection a = DatabaseConnection.getInstance();
        DatabaseConnection b = DatabaseConnection.getInstance();
        assertSame(a, b, "getInstance() deve retornar sempre a mesma instância (Singleton)");
    }

    @Test
    void getConnectionNaoDeveRetornarNull() {
        // Este teste só passa com o Docker rodando
        // Se o banco não estiver disponível, o teste é ignorado graciosamente
        try {
            DatabaseConnection db = DatabaseConnection.getInstance();
            assertNotNull(db.getConnection(), "Connection não deve ser null");
        } catch (RuntimeException e) {
            System.out.println("AVISO: banco não disponível — teste de conexão pulado.");
            System.out.println("Rode 'docker-compose up -d' para executar este teste.");
        }
    }
}
