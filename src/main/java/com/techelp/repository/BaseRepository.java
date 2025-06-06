package com.techelp.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.io.InputStream;
import java.util.Properties;

public abstract class BaseRepository {
    private static final Properties props = new Properties();
    
    static {
        try (InputStream input = BaseRepository.class.getClassLoader().getResourceAsStream("application.properties")) {
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar configurações: " + e.getMessage(), e);
        }
    }
    
    protected Connection getConnection() {
        try {
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            // Registra o driver do H2
            Class.forName(props.getProperty("db.driver"));
            
            return DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
} 