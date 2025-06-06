package com.techelp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class PoliticaPrivacidadeController extends BaseController {
    
    @FXML
    private TextArea politicaArea;
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando PoliticaPrivacidadeController");
        carregarPolitica();
    }
    
    private void carregarPolitica() {
        try {
            // Aqui você pode carregar o texto da política de privacidade de um arquivo ou banco de dados
            String politica = """
                Política de Privacidade do TecHelp
                
                1. Coleta de Dados
                O TecHelp coleta apenas as informações necessárias para o funcionamento do sistema:
                - Nome completo
                - E-mail
                - Telefone
                
                2. Uso dos Dados
                As informações coletadas são utilizadas exclusivamente para:
                - Identificação do usuário
                - Comunicação sobre chamados
                - Estatísticas internas
                
                3. Proteção dos Dados
                Todos os dados são armazenados de forma segura e criptografada.
                
                4. Compartilhamento
                Não compartilhamos suas informações com terceiros.
                
                5. Seus Direitos
                Você tem direito a:
                - Acessar seus dados
                - Corrigir seus dados
                - Excluir sua conta
                
                6. Contato
                Para questões sobre privacidade: privacidade@techelp.com
                """;
            
            politicaArea.setText(politica);
            politicaArea.setEditable(false);
            politicaArea.setWrapText(true);
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar política de privacidade: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar política de privacidade: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFechar() {
        try {
            // Como esta tela é aberta em uma nova janela, vamos fechá-la
            if (primaryStage != null) {
                primaryStage.close();
            }
        } catch (Exception e) {
            System.err.println("Erro ao fechar janela: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao fechar janela: " + e.getMessage());
        }
    }
} 