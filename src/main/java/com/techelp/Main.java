package com.techelp;

import com.techelp.service.AuthService;

public class Main {
    public static void main(String[] args) {
        String senha = "admin123";
        String hash = AuthService.getInstance().hashSenha(senha);
        System.out.println("Hash gerado para senha '" + senha + "': " + hash);
    }
} 