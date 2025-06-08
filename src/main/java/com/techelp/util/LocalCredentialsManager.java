package com.techelp.util;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;

public class LocalCredentialsManager {
    private static final String CREDENTIALS_FILE = "local_credentials.dat";
    private static final String ENCRYPTION_KEY = "TecHelpLocalKey2024";
    private static LocalCredentialsManager instance;
    private final Properties credentials;
    private final Path credentialsPath;
    
    private LocalCredentialsManager() {
        credentials = new Properties();
        credentialsPath = Paths.get(System.getProperty("user.home"), ".techelp", CREDENTIALS_FILE);
        loadCredentials();
    }
    
    public static synchronized LocalCredentialsManager getInstance() {
        if (instance == null) {
            instance = new LocalCredentialsManager();
        }
        return instance;
    }
    
    private void loadCredentials() {
        try {
            if (Files.exists(credentialsPath)) {
                byte[] encryptedData = Files.readAllBytes(credentialsPath);
                String decryptedData = decrypt(encryptedData);
                credentials.load(new StringReader(decryptedData));
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar credenciais: " + e.getMessage());
        }
    }
    
    private void saveCredentials() {
        try {
            // Cria o diretório se não existir
            Files.createDirectories(credentialsPath.getParent());
            
            // Converte as credenciais para string
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            credentials.store(out, null);
            String credentialsString = out.toString();
            
            // Criptografa e salva
            byte[] encryptedData = encrypt(credentialsString);
            Files.write(credentialsPath, encryptedData);
        } catch (Exception e) {
            System.err.println("Erro ao salvar credenciais: " + e.getMessage());
        }
    }
    
    public void saveCredentials(String email, String senha, boolean lembrar) {
        if (lembrar) {
            credentials.setProperty("email", email);
            credentials.setProperty("senha", senha);
            credentials.setProperty("lembrar", "true");
        } else {
            credentials.remove("email");
            credentials.remove("senha");
            credentials.setProperty("lembrar", "false");
        }
        saveCredentials();
    }
    
    public String getEmail() {
        return credentials.getProperty("email", "");
    }
    
    public String getSenha() {
        return credentials.getProperty("senha", "");
    }
    
    public boolean isLembrar() {
        return Boolean.parseBoolean(credentials.getProperty("lembrar", "false"));
    }
    
    private byte[] encrypt(String data) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data.getBytes());
    }
    
    private String decrypt(byte[] encryptedData) throws Exception {
        SecretKeySpec key = generateKey();
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }
    
    private SecretKeySpec generateKey() throws Exception {
        byte[] key = ENCRYPTION_KEY.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }
} 