package com.techelp.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import com.techelp.controller.StatusBarController;

public class DatabaseSyncManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseSyncManager.class.getName());
    private static final String SQL_SERVER_URL = "jdbc:sqlserver://localhost:1433;databaseName=TecHelp;encrypt=true;trustServerCertificate=true";
    private static final String H2_URL = "jdbc:h2:mem:techelp;DB_CLOSE_DELAY=-1";
    private static final String USER = "guilherme";
    private static final String PASSWORD = "teste123";
    private static final int SYNC_INTERVAL = 30000; // 30 segundos
    
    private Timer syncTimer;
    private boolean isSyncing = false;
    private StatusBarController statusBar;
    
    public DatabaseSyncManager() {
        try {
            // Inicializa o banco H2
            Class.forName("org.h2.Driver");
            try (Connection h2Conn = DriverManager.getConnection(H2_URL, USER, PASSWORD)) {
                // Cria as tabelas no H2 se não existirem
                createTablesIfNotExist(h2Conn);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erro ao inicializar banco H2: " + e.getMessage(), e);
        }
        startSyncTimer();
    }
    
    public void setStatusBar(StatusBarController statusBar) {
        this.statusBar = statusBar;
    }
    
    private void updateStatus(String message, Color color) {
        if (statusBar != null) {
            Platform.runLater(() -> statusBar.updateStatus(message, color));
        }
    }
    
    private void startSyncTimer() {
        syncTimer = new Timer(true);
        syncTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isSyncing) {
                    checkAndSync();
                }
            }
        }, SYNC_INTERVAL, SYNC_INTERVAL);
    }
    
    private void checkAndSync() {
        try {
            // Tenta conectar ao SQL Server
            try (Connection sqlConn = DriverManager.getConnection(SQL_SERVER_URL, USER, PASSWORD)) {
                LOGGER.info("SQL Server está online. Iniciando sincronização...");
                updateStatus("SQL Server online. Sincronizando dados...", Color.ORANGE);
                isSyncing = true;
                
                // Sincroniza cada tabela
                syncTable("usuarios", sqlConn);
                syncTable("chamados", sqlConn);
                syncTable("interacoes", sqlConn);
                syncTable("notificacoes", sqlConn);
                
                LOGGER.info("Sincronização concluída com sucesso!");
                updateStatus("Sincronização concluída com sucesso!", Color.GREEN);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, "SQL Server ainda offline: {0}", e.getMessage());
            updateStatus("SQL Server offline. Usando banco local.", Color.RED);
        } finally {
            isSyncing = false;
        }
    }
    
    private void syncTable(String tableName, Connection sqlConn) throws SQLException {
        LOGGER.info("Sincronizando tabela: " + tableName);
        updateStatus("Sincronizando tabela: " + tableName, Color.ORANGE);
        
        try (Connection h2Conn = DriverManager.getConnection(H2_URL, USER, PASSWORD)) {
            // Obtém os dados do H2
            List<String> h2Data = getTableData(h2Conn, tableName);
            
            // Obtém os dados do SQL Server
            List<String> sqlData = getTableData(sqlConn, tableName);
            
            // Encontra registros que existem no H2 mas não no SQL Server
            List<String> newRecords = new ArrayList<>(h2Data);
            newRecords.removeAll(sqlData);
            
            // Insere os novos registros no SQL Server
            for (String record : newRecords) {
                insertRecord(sqlConn, tableName, record);
            }
            
            String message = String.format("Tabela %s sincronizada. %d novos registros inseridos.", 
                tableName, newRecords.size());
            LOGGER.info(message);
            updateStatus(message, Color.GREEN);
        }
    }
    
    private List<String> getTableData(Connection conn, String tableName) throws SQLException {
        List<String> data = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName;
        
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                StringBuilder record = new StringBuilder();
                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                    if (i > 1) record.append(",");
                    record.append(rs.getString(i));
                }
                data.add(record.toString());
            }
        }
        
        return data;
    }
    
    private void insertRecord(Connection sqlConn, String tableName, String record) throws SQLException {
        String[] values = record.split(",");
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
        
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        
        try (PreparedStatement stmt = sqlConn.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.length; i++) {
                stmt.setString(i + 1, values[i]);
            }
            stmt.executeUpdate();
        }
    }
    
    private void createTablesIfNotExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            // Cria tabela de usuários
            stmt.execute("CREATE TABLE IF NOT EXISTS usuarios (" +
                "id_user INT AUTO_INCREMENT PRIMARY KEY," +
                "name_user VARCHAR(100) NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "password VARCHAR(100) NOT NULL," +
                "type_user VARCHAR(20) NOT NULL," +
                "dept VARCHAR(50) NOT NULL," +
                "data_criacao TIMESTAMP NOT NULL" +
                ")");

            // Cria tabela de chamados
            stmt.execute("CREATE TABLE IF NOT EXISTS chamados (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "titulo VARCHAR(100) NOT NULL," +
                "descricao TEXT NOT NULL," +
                "status VARCHAR(20) NOT NULL," +
                "prioridade VARCHAR(20) NOT NULL," +
                "data_abertura TIMESTAMP NOT NULL," +
                "data_fechamento TIMESTAMP," +
                "solicitante_id INT NOT NULL," +
                "tecnico_id INT," +
                "FOREIGN KEY (solicitante_id) REFERENCES usuarios(id_user)," +
                "FOREIGN KEY (tecnico_id) REFERENCES usuarios(id_user)" +
                ")");

            // Cria tabela de interações
            stmt.execute("CREATE TABLE IF NOT EXISTS interacoes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "chamado_id INT NOT NULL," +
                "usuario_id INT NOT NULL," +
                "mensagem TEXT NOT NULL," +
                "data_hora TIMESTAMP NOT NULL," +
                "tipo VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (chamado_id) REFERENCES chamados(id)," +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id_user)" +
                ")");

            // Cria tabela de notificações
            stmt.execute("CREATE TABLE IF NOT EXISTS notificacoes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "usuario_id INT NOT NULL," +
                "chamado_id INT NOT NULL," +
                "mensagem TEXT NOT NULL," +
                "data_criacao TIMESTAMP NOT NULL," +
                "lida BOOLEAN DEFAULT FALSE," +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id_user)," +
                "FOREIGN KEY (chamado_id) REFERENCES chamados(id)" +
                ")");
        }
    }
    
    public void stopSync() {
        if (syncTimer != null) {
            syncTimer.cancel();
        }
    }
} 