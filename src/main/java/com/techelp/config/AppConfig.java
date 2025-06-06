package com.techelp.config;

import com.techelp.service.GeminiService;

public class AppConfig {
    private static GeminiService geminiService;
    
    public static GeminiService getGeminiService() {
        if (geminiService == null) {
            geminiService = new GeminiService();
        }
        return geminiService;
    }
} 