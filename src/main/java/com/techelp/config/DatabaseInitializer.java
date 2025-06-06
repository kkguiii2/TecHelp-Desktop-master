package com.techelp.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;
import java.util.Properties;
import java.sql.DriverManager;
import java.nio.charset.StandardCharsets;

import com.techelp.repository.BaseRepository;

public class DatabaseInitializer extends BaseRepository {
    
    public void initialize() {
        System.out.println("Inicializando banco de dados...");
        try {
            // Carrega o driver JDBC do SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            String setupSql = loadResourceFile("/db/setup.sql");
            System.out.println("Script de setup carregado");
            
            System.out.println("Executando script de setup...");
            executeSetup(setupSql);
            System.out.println("Banco de dados inicializado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar banco de dados: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Falha ao inicializar banco de dados", e);
        }
    }
    
    private String loadResourceFile(String resourcePath) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    getClass().getResourceAsStream(resourcePath),
                    StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    private void executeSetup(String sql) throws Exception {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            props.load(input);
        }
        
        String url = props.getProperty("db.url");
        String user = props.getProperty("db.username");
        String password = props.getProperty("db.password");
        
        // Modifica a URL para conectar ao banco master primeiro
        url = url.replace("databaseName=techelp", "databaseName=master");
        System.out.println("Conectando ao banco master com URL: " + url);
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Conexão estabelecida!");
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                String[] commands = sql.split("GO");
                for (String command : commands) {
                    String trimmedCommand = command.trim();
                    if (!trimmedCommand.isEmpty()) {
                        try {
                            System.out.println("Executando comando: " + trimmedCommand);
                            stmt.execute(trimmedCommand);
                        } catch (Exception e) {
                            System.err.println("Erro ao executar comando: " + trimmedCommand);
                            System.err.println("Erro: " + e.getMessage());
                            conn.rollback();
                            throw e;
                        }
                    }
                }
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        }
    }
    
    public static void main(String[] args) {
        new DatabaseInitializer().initialize();
    }
} 