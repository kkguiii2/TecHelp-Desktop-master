package com.techelp.config;

public class GroqConfig {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = "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";
    private static final String MODEL = "llama3-70b-8192";
    
    public static String getApiKey() {
        return API_KEY;
    }
    
    public static String getApiUrl() {
        return API_URL;
    }
    
    public static String getModel() {
        return MODEL;
    }
} 