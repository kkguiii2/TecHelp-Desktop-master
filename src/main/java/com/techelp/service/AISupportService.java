package com.techelp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AISupportService {
    private static final Map<String, String> knowledgeBase = new HashMap<>();
    
    static {
        // Base de conhecimento inicial com problemas comuns e suas soluções
        knowledgeBase.put("computador não liga", "Verifique se o cabo de energia está conectado corretamente e se a tomada está funcionando.");
        knowledgeBase.put("tela azul", "Tente reiniciar o computador em modo seguro e verifique se há atualizações pendentes do Windows.");
        knowledgeBase.put("internet lenta", "Verifique se há muitos dispositivos conectados à rede e tente reiniciar o roteador.");
        // Adicione mais problemas e soluções conforme necessário
    }

    public String analyzeProblem(String problemDescription) {
        // Normaliza o texto para melhor comparação
        String normalizedProblem = problemDescription.toLowerCase().trim();
        
        // Procura por correspondências na base de conhecimento
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            if (normalizedProblem.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        // Se não encontrar uma solução específica, tenta uma análise mais genérica
        if (containsKeywords(normalizedProblem, "lento", "demorado", "travando")) {
            return "Seu problema parece estar relacionado à performance. Tente fechar programas não utilizados e verificar o uso de memória.";
        }
        
        if (containsKeywords(normalizedProblem, "erro", "falha", "não funciona")) {
            return "Seu problema parece ser mais complexo. Recomendo reiniciar o sistema e, se persistir, será necessário encaminhar para um técnico.";
        }
        
        // Se não conseguir identificar uma solução, encaminha para um técnico
        return "Não foi possível identificar uma solução automática para seu problema. Um técnico será notificado para ajudá-lo.";
    }
    
    private boolean containsKeywords(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    public void addToKnowledgeBase(String problem, String solution) {
        knowledgeBase.put(problem.toLowerCase(), solution);
    }
} 