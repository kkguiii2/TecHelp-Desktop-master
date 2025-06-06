package com.techelp.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import com.techelp.repository.BaseRepository;

public class TestConnection extends BaseRepository {
    public static void main(String[] args) {
        TestConnection test = new TestConnection();
        test.testConnection();
    }
    
    public void testConnection() {
        try {
            System.out.println("Testando conexão com o banco de dados...");
            
            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                
                // Tenta criar uma tabela de teste
                stmt.execute("CREATE TABLE IF NOT EXISTS test (id INT)");
                System.out.println("Tabela de teste criada com sucesso!");
                
                // Tenta inserir um registro
                stmt.execute("INSERT INTO test VALUES (1)");
                System.out.println("Registro inserido com sucesso!");
                
                // Tenta consultar
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM test")) {
                    if (rs.next()) {
                        System.out.println("Registro encontrado: " + rs.getInt("id"));
                    }
                }
                
                // Limpa a tabela de teste
                stmt.execute("DROP TABLE test");
                System.out.println("Tabela de teste removida com sucesso!");
                
                System.out.println("Teste de conexão concluído com sucesso!");
            }
        } catch (Exception e) {
            System.err.println("Erro ao testar conexão: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 