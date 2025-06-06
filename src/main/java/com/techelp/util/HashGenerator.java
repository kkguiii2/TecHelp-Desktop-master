package com.techelp.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class HashGenerator {
    public static void main(String[] args) {
        String senha = "admin123";
        String hash = BCrypt.withDefaults().hashToString(12, senha.toCharArray());
        System.out.println("Hash para a senha '" + senha + "': " + hash);
    }
} 