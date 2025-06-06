package com.techelp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=true;trustServerCertificate=true";
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=TecHelp;encrypt=true;trustServerCertificate=true";
    private static final String USER = "guilherme";
    private static final String PASSWORD = "teste123";
    
    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Erro ao carregar driver SQL Server", e);
        }
    }
    
    private static void initializeDatabase() {
        try {
            // Primeiro conecta ao master para criar o banco se necessário
            try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
                System.out.println("Verificando se o banco de dados existe...");
                
                try (Statement stmt = conn.createStatement()) {
                    // Tenta criar o banco de dados se não existir
                    stmt.execute(
                        "IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'TecHelp') " +
                        "BEGIN " +
                        "    CREATE DATABASE TecHelp; " +
                        "END"
                    );
                }
            }
            
            // Agora conecta ao banco TecHelp para criar as tabelas
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                System.out.println("Criando tabelas se necessário...");
                
                // Carrega e executa o script SQL
                String schemaScript = loadResourceFile("/db/techelp_schema.sql");
                try (Statement stmt = conn.createStatement()) {
                    for (String command : schemaScript.split("GO")) {
                        if (!command.trim().isEmpty()) {
                            stmt.execute(command);
                        }
                    }
                }
                
                System.out.println("Banco de dados inicializado com sucesso!");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar banco de dados: " + e.getMessage(), e);
        }
    }
    
    private static String loadResourceFile(String resourcePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(DatabaseConfig.class.getResourceAsStream(resourcePath)))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASSWORD);
    }
} 