package com.techelp.config;

import com.techelp.service.AssistenteService;

public class AppConfig {
    private static AssistenteService assistenteService;
    
    public static AssistenteService getAssistenteService() {
        if (assistenteService == null) {
            assistenteService = new AssistenteService();
        }
        return assistenteService;
    }
} 