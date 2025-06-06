package com.techelp.service;

import com.techelp.config.GeminiConfig;
import com.techelp.model.entity.Chamado;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.techelp.config.AppConfig;
import java.util.HashMap;
import java.util.regex.Pattern;

public class GeminiService {
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    private final HttpClient client;
    private static final int TIMEOUT_SECONDS = 30;
    private List<JSONObject> conversationHistory;
    private static final Map<String, List<String>> PALAVRAS_CHAVE = new HashMap<>();
    private static final Map<String, List<String>> SOLUCOES_AUTOMATICAS = new HashMap<>();
    
    static {
        // Inicializa palavras-chave para cada categoria
        PALAVRAS_CHAVE.put("HARDWARE", List.of(
            "computador", "monitor", "teclado", "mouse", "impressora", "scanner",
            "placa", "memória", "hd", "ssd", "fonte", "bateria", "carregador"
        ));
        
        PALAVRAS_CHAVE.put("SOFTWARE", List.of(
            "programa", "sistema", "windows", "office", "excel", "word",
            "instalação", "atualização", "licença", "antivírus", "software"
        ));
        
        PALAVRAS_CHAVE.put("REDE", List.of(
            "internet", "wifi", "rede", "conexão", "cabo", "roteador",
            "vpn", "acesso remoto", "servidor", "proxy", "firewall"
        ));
        
        PALAVRAS_CHAVE.put("ACESSO", List.of(
            "senha", "login", "usuário", "permissão", "bloqueio", "desbloqueio",
            "conta", "perfil", "autenticação", "token", "certificado"
        ));
        
        PALAVRAS_CHAVE.put("EMAIL", List.of(
            "email", "e-mail", "outlook", "gmail", "correio", "caixa postal",
            "spam", "mensagem", "assinatura", "backup", "restauração"
        ));
        
        PALAVRAS_CHAVE.put("IMPRESSORA", List.of(
            "impressora", "toner", "cartucho", "papel", "digitalização",
            "scanner", "cópia", "impressão", "fila", "spooler"
        ));
        
        // Inicializa soluções automáticas para cada categoria
        SOLUCOES_AUTOMATICAS.put("HARDWARE", List.of(
            "Por favor, verifique se todos os cabos estão conectados corretamente.",
            "Tente reiniciar o equipamento.",
            "Verifique se há alguma luz ou sinal de funcionamento no dispositivo."
        ));
        
        SOLUCOES_AUTOMATICAS.put("SOFTWARE", List.of(
            "Tente reiniciar o programa.",
            "Verifique se há atualizações pendentes.",
            "Limpe o cache e os arquivos temporários."
        ));
        
        SOLUCOES_AUTOMATICAS.put("REDE", List.of(
            "Verifique se outros dispositivos também estão sem conexão.",
            "Tente reiniciar o roteador.",
            "Verifique se o cabo de rede está bem conectado."
        ));
        
        SOLUCOES_AUTOMATICAS.put("ACESSO", List.of(
            "Tente redefinir sua senha através do portal de autoatendimento.",
            "Verifique se não há bloqueio por tentativas incorretas.",
            "Limpe o cache do navegador e tente novamente."
        ));
        
        SOLUCOES_AUTOMATICAS.put("EMAIL", List.of(
            "Verifique se há espaço disponível na caixa postal.",
            "Tente acessar através do webmail.",
            "Verifique se o email não está na pasta de spam."
        ));
        
        SOLUCOES_AUTOMATICAS.put("IMPRESSORA", List.of(
            "Verifique se há papel na bandeja.",
            "Verifique se há toner/cartucho suficiente.",
            "Tente remover e adicionar a impressora novamente."
        ));
    }
    
    public GeminiService() {
        logger.info("Inicializando GeminiService...");
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
            .build();
        this.conversationHistory = new ArrayList<>();
        
        // Adiciona a mensagem inicial do sistema que define o comportamento do assistente
        addSystemMessage(
            "Você é um assistente de suporte técnico da TecHelp. " +
            "Seu objetivo é ajudar os usuários com problemas técnicos de forma profissional e eficiente. " +
            "Mantenha suas respostas concisas e focadas na solução do problema. " +
            "Se após algumas tentativas o problema não for resolvido, sugira encaminhar para um técnico humano."
        );
        logger.info("GeminiService inicializado com sucesso.");
    }
    
    public String processarMensagem(String mensagem, Chamado chamado) {
        // Se for uma saudação, responde adequadamente
        if (isSaudacao(mensagem)) {
            return "Olá! Como posso ajudar?";
        }
        
        // Se for um agradecimento, responde adequadamente
        if (isAgradecimento(mensagem)) {
            return "Por nada! Estou aqui para ajudar. Há mais alguma coisa que posso fazer por você?";
        }
        
        // Verifica se há palavras-chave de problemas comuns
        String categoria = chamado.getCategoriaIa();
        List<String> solucoes = SOLUCOES_AUTOMATICAS.get(categoria);
        
        if (solucoes != null && !solucoes.isEmpty()) {
            return solucoes.get(0); // Retorna a primeira solução da categoria
        }
        
        // Resposta padrão
        return "Entendi sua solicitação. Vou encaminhar para um técnico avaliar.";
    }
    
    public boolean precisaEncaminharParaTecnico(String resposta) {
        return resposta.contains("encaminhar para um técnico");
    }
    
    private boolean isSaudacao(String mensagem) {
        return Pattern.compile("\\b(oi|olá|ola|bom dia|boa tarde|boa noite|hi|hello)\\b")
                .matcher(mensagem.toLowerCase())
                .find();
    }
    
    private boolean isAgradecimento(String mensagem) {
        return Pattern.compile("\\b(obrigado|obrigada|valeu|thanks|thank you|grato|grata)\\b")
                .matcher(mensagem.toLowerCase())
                .find();
    }
    
    private void addSystemMessage(String content) {
        conversationHistory.add(new JSONObject()
            .put("role", "system")
            .put("content", content));
    }
    
    private void addUserMessage(String content) {
        conversationHistory.add(new JSONObject()
            .put("role", "user")
            .put("content", content));
        
        // Limita o histórico para evitar tokens excessivos
        if (conversationHistory.size() > 10) {
            // Mantém a mensagem do sistema e remove as mensagens mais antigas
            List<JSONObject> newHistory = new ArrayList<>();
            newHistory.add(conversationHistory.get(0)); // Mensagem do sistema
            newHistory.addAll(conversationHistory.subList(
                conversationHistory.size() - 4, 
                conversationHistory.size()
            ));
            conversationHistory = newHistory;
        }
    }
    
    private void addAssistantMessage(String content) {
        conversationHistory.add(new JSONObject()
            .put("role", "assistant")
            .put("content", content));
    }
    
    private String buildConversationText() {
        StringBuilder text = new StringBuilder();
        for (JSONObject message : conversationHistory) {
            text.append(message.getString("role"))
                .append(": ")
                .append(message.getString("content"))
                .append("\n\n");
        }
        return text.toString();
    }
    
    public String classificarChamado(Chamado chamado) {
        String textoCompleto = (chamado.getTitulo() + " " + chamado.getDescricao()).toLowerCase();
        Map<String, Integer> pontuacao = new HashMap<>();
        
        for (Map.Entry<String, List<String>> categoria : PALAVRAS_CHAVE.entrySet()) {
            int pontos = categoria.getValue().stream()
                .mapToInt(palavra -> textoCompleto.contains(palavra) ? 1 : 0)
                .sum();
            
            pontuacao.put(categoria.getKey(), pontos);
        }
        
        return pontuacao.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("OUTROS");
    }
} 