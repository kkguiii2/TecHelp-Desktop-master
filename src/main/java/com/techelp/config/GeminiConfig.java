package com.techelp.config;

public class GeminiConfig {
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent";
    private static final String API_KEY = "AIzaSyAovRzE2K1MRNQImmIrGJvLWf-_4Zj5XVE";
    
    public static String getApiKey() {
        return API_KEY;
    }
    
    public static String getApiUrl() {
        return API_URL;
    }
} 