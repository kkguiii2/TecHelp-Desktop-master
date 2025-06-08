package com.techelp.view;

import com.techelp.service.AISupportService;
import javax.swing.*;
import java.awt.*;

public class AISupportView extends JFrame {
    private final AISupportService aiService;
    private final JTextArea problemInput;
    private final JTextArea solutionOutput;
    
    public AISupportView() {
        aiService = new AISupportService();
        
        setTitle("Assistente Virtual TecHelp");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Painel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Área de entrada do problema
        JLabel problemLabel = new JLabel("Descreva seu problema:");
        problemInput = new JTextArea(5, 40);
        problemInput.setLineWrap(true);
        problemInput.setWrapStyleWord(true);
        JScrollPane problemScroll = new JScrollPane(problemInput);
        
        // Botão de análise
        JButton analyzeButton = new JButton("Analisar Problema");
        
        // Área de saída da solução
        JLabel solutionLabel = new JLabel("Solução:");
        solutionOutput = new JTextArea(5, 40);
        solutionOutput.setLineWrap(true);
        solutionOutput.setWrapStyleWord(true);
        solutionOutput.setEditable(false);
        JScrollPane solutionScroll = new JScrollPane(solutionOutput);
        
        // Adiciona componentes ao painel
        mainPanel.add(problemLabel, BorderLayout.NORTH);
        mainPanel.add(problemScroll, BorderLayout.CENTER);
        mainPanel.add(analyzeButton, BorderLayout.SOUTH);
        
        // Painel de solução
        JPanel solutionPanel = new JPanel(new BorderLayout());
        solutionPanel.add(solutionLabel, BorderLayout.NORTH);
        solutionPanel.add(solutionScroll, BorderLayout.CENTER);
        
        // Adiciona painéis à janela
        setLayout(new BorderLayout(10, 10));
        add(mainPanel, BorderLayout.NORTH);
        add(solutionPanel, BorderLayout.CENTER);
        
        // Ação do botão
        analyzeButton.addActionListener(e -> {
            String problem = problemInput.getText();
            if (!problem.trim().isEmpty()) {
                String solution = aiService.analyzeProblem(problem);
                solutionOutput.setText(solution);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Por favor, descreva seu problema antes de analisar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AISupportView view = new AISupportView();
            view.setVisible(true);
        });
    }
} 