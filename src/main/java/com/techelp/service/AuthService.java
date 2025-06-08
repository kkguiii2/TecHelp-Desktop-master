package com.techelp.service;

import com.techelp.model.entity.Usuario;
import com.techelp.repository.UsuarioRepository;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AuthService {
    private static AuthService instance;
    private final UsuarioRepository usuarioRepository;
    private Usuario usuarioLogado;
    
    private AuthService() {
        this.usuarioRepository = new UsuarioRepository();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    public Usuario autenticar(String email, String senha) {
        System.out.println("Tentando autenticar usuário: " + email);
        Usuario usuario = usuarioRepository.findByEmail(email);
        
        if (usuario == null) {
            System.out.println("Usuário não encontrado: " + email);
            return null;
        }
        
        System.out.println("Usuário encontrado: " + usuario.getNome());
        System.out.println("Tipo do usuário: " + usuario.getTipo());
        System.out.println("Hash armazenado: " + usuario.getSenha());
        
        if (validarSenha(senha, usuario.getSenha())) {
            System.out.println("Senha válida para o usuário: " + email);
            this.usuarioLogado = usuario;
            return usuario;
        } else {
            System.out.println("Senha inválida para o usuário: " + email);
            return null;
        }
    }
    
    public boolean emailJaExiste(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario != null) {
            return true;
        }
        return false;
    }
    
    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }
    
    public void logout() {
        this.usuarioLogado = null;
    }
    
    public String hashSenha(String senha) {
        return BCrypt.withDefaults().hashToString(12, senha.toCharArray());
    }
    
    public boolean validarSenha(String senhaDigitada, String senhaArmazenada) {
        try {
            System.out.println("Validando senha...");
            System.out.println("Hash armazenado: " + senhaArmazenada);
            System.out.println("Senha digitada: " + senhaDigitada);
            
            // Configura o verificador BCrypt com as mesmas configurações usadas para criar o hash
            BCrypt.Verifyer verifyer = BCrypt.verifyer(BCrypt.Version.VERSION_2A);
            
            // Verifica se a senha digitada corresponde ao hash armazenado
            BCrypt.Result result = verifyer.verify(senhaDigitada.toCharArray(), senhaArmazenada.toCharArray());
            System.out.println("Resultado da validação: " + result.verified);
            return result.verified;
        } catch (Exception e) {
            System.err.println("Erro ao validar senha: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 