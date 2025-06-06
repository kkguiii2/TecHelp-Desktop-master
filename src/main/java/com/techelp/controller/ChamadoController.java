package com.techelp.controller;

import com.techelp.model.dto.ChamadoDTO;
import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.model.entity.Interacao;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class ChamadoController extends BaseController implements DadosAware {
    
    private final ChamadoService chamadoService;
    private final AuthService authService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private Timeline atualizacaoTimer;
    private boolean esperandoResposta = false;
    
    private ChamadoDTO chamado;
    private Usuario usuarioLogado;
    
    @FXML
    private Text idText;
    
    @FXML
    private Text statusText;
    
    @FXML
    private Text prioridadeText;
    
    @FXML
    private Text categoriaText;
    
    @FXML
    private Text solicitanteText;
    
    @FXML
    private Text tecnicoText;
    
    @FXML
    private Text dataAberturaText;
    
    @FXML
    private Text tempoDecorridoText;
    
    @FXML
    private TextArea descricaoArea;
    
    @FXML
    private VBox interacoesBox;
    
    @FXML
    private TextField mensagemField;
    
    @FXML
    private VBox avaliacaoBox;
    
    @FXML
    private Text avaliacaoText;
    
    @FXML
    private TextField tituloField;
    
    @FXML
    private VBox acoesBox;
    
    public ChamadoController() {
        this.chamadoService = new ChamadoService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando ChamadoController");
            
            usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            // Se não houver chamado, é um novo chamado
            if (chamado == null) {
                tituloField.setVisible(true);
                descricaoArea.setEditable(true);
                descricaoArea.setPromptText("Digite a descrição do chamado");
                tituloField.setPromptText("Digite o título do chamado");
            }
            
            // Configurar timer de atualização automática
            atualizacaoTimer = new Timeline(
                new KeyFrame(Duration.seconds(2), event -> {
                    if (chamado != null && !esperandoResposta) {
                        carregarInteracoes();
                    }
                })
            );
            atualizacaoTimer.setCycleCount(Timeline.INDEFINITE);
            atualizacaoTimer.play();
            
            // Mostrar/esconder menu de ações baseado no tipo de usuário
            if (usuarioLogado.getTipo() == Usuario.TipoUsuario.TECNICO) {
                acoesBox.setVisible(true);
            } else {
                acoesBox.setVisible(false);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar controller: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar tela: " + e.getMessage());
        }
    }
    
    @Override
    public void setDados(Object dados) {
        if (dados instanceof ChamadoDTO) {
            this.chamado = (ChamadoDTO) dados;
            carregarDados();
            
            // Iniciar timer de atualização quando o chamado for carregado
            if (atualizacaoTimer != null) {
                atualizacaoTimer.play();
            }
        }
    }
    
    private void carregarDados() {
        try {
            if (chamado == null) {
                throw new RuntimeException("Chamado não foi carregado corretamente");
            }
            
            idText.setText(String.valueOf(chamado.getId()));
            statusText.setText(chamado.getStatus().toString());
            prioridadeText.setText(chamado.getPrioridade().toString());
            categoriaText.setText(chamado.getCategoriaIa());
            solicitanteText.setText(chamado.getSolicitante().getNome());
            tecnicoText.setText(chamado.getTecnico() != null ? chamado.getTecnico().getNome() : "Não atribuído");
            dataAberturaText.setText(DATE_FORMATTER.format(chamado.getDataAbertura()));
            descricaoArea.setText(chamado.getDescricao());
            
            // Calcula o tempo decorrido
            long tempoDecorrido = chamado.getTempoDecorrido();
            tempoDecorridoText.setText(formatarTempoDecorrido(tempoDecorrido));
            
            // Mostra avaliação se o chamado estiver fechado
            if (chamado.getAvaliacao() != null) {
                avaliacaoBox.setVisible(true);
                avaliacaoText.setText(String.valueOf(chamado.getAvaliacao()));
            }
            
            carregarInteracoes();
            
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados do chamado: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar dados do chamado: " + e.getMessage());
        }
    }
    
    private void carregarInteracoes() {
        try {
            interacoesBox.getChildren().clear();
            chamadoService.listarInteracoes(chamado.getId()).forEach(interacao -> {
                VBox mensagemBox = new VBox();
                mensagemBox.getStyleClass().add("mensagem-box");
                
                // Verifica se é uma mensagem própria (do usuário logado) e não é uma resposta automática
                boolean isPropriaMsg = interacao.getUsuario().getId().equals(usuarioLogado.getId()) 
                    && interacao.getTipo() != Interacao.TipoInteracao.RESPOSTA_CHATBOT;
                
                if (isPropriaMsg) {
                    mensagemBox.getStyleClass().add("mensagem-propria");
                } else if (interacao.getTipo() == Interacao.TipoInteracao.RESPOSTA_CHATBOT) {
                    mensagemBox.getStyleClass().add("mensagem-bot");
                }
                
                // Criar TextFlow para informações do usuário
                TextFlow infoFlow = new TextFlow();
                infoFlow.getStyleClass().add("mensagem-info-container");
                Text infoText = new Text(String.format("%s - %s",
                    interacao.getTipo() == Interacao.TipoInteracao.RESPOSTA_CHATBOT ? "Assistente Virtual" : interacao.getUsuario().getNome(),
                    DATE_FORMATTER.format(interacao.getDataHora())));
                infoText.getStyleClass().add("mensagem-info");
                infoFlow.getChildren().add(infoText);
                
                // Criar TextFlow para a mensagem
                TextFlow mensagemFlow = new TextFlow();
                mensagemFlow.getStyleClass().add("mensagem-texto-container");
                mensagemFlow.setMaxWidth(500); // Limita a largura máxima
                Text mensagemText = new Text(interacao.getMensagem());
                mensagemText.getStyleClass().add("mensagem-texto");
                mensagemFlow.getChildren().add(mensagemText);
                
                mensagemBox.getChildren().addAll(infoFlow, mensagemFlow);
                
                HBox container = new HBox();
                container.setMaxWidth(Double.MAX_VALUE);
                if (isPropriaMsg) {
                    container.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                } else {
                    container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                }
                container.getChildren().add(mensagemBox);
                
                interacoesBox.getChildren().add(container);
            });
            
            // Rola para a última mensagem após um pequeno atraso
            if (!interacoesBox.getChildren().isEmpty()) {
                CompletableFuture.delayedExecutor(100, TimeUnit.MILLISECONDS).execute(() -> {
                    Platform.runLater(() -> {
                        javafx.scene.Parent parent = interacoesBox.getParent();
                        while (parent != null && !(parent instanceof ScrollPane)) {
                            parent = parent.getParent();
                        }
                        if (parent instanceof ScrollPane) {
                            ((ScrollPane) parent).setVvalue(1.0);
                        }
                    });
                });
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar interações: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar interações: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleEnviarMensagem() {
        try {
            String mensagem = mensagemField.getText().trim();
            if (mensagem.isEmpty()) {
                mostrarErro("Por favor, digite uma mensagem");
                return;
            }
            
            if (chamado == null) {
                // Criar novo chamado
                String titulo = tituloField.getText().trim();
                String descricao = descricaoArea.getText().trim();
                
                if (titulo.isEmpty()) {
                    mostrarErro("Por favor, digite um título para o chamado");
                    return;
                }
                
                if (descricao.isEmpty()) {
                    mostrarErro("Por favor, digite uma descrição para o chamado");
                    return;
                }
                
                chamado = chamadoService.abrirChamado(titulo, descricao, usuarioLogado);
                
                // Esconder campos de novo chamado
                tituloField.setVisible(false);
                descricaoArea.setEditable(false);
                
                // Carregar dados do novo chamado
                carregarDados();
            }

            // Salvar a mensagem do usuário e limpar o campo
            final String mensagemEnviada = mensagem;
            mensagemField.clear();
            mensagemField.setDisable(true);
            esperandoResposta = true;

            // Adicionar interação do usuário e atualizar imediatamente
            chamadoService.adicionarInteracao(chamado.getId(), mensagemEnviada, usuarioLogado);
            carregarInteracoes();

            // Processar a resposta do assistente em uma thread separada
            CompletableFuture.delayedExecutor(3, TimeUnit.SECONDS).execute(() -> {
                try {
                    Platform.runLater(() -> {
                        try {
                            // Reativar o campo de mensagem
                            mensagemField.setDisable(false);
                            mensagemField.requestFocus();
                            esperandoResposta = false;
                        } catch (Exception e) {
                            System.err.println("Erro ao reativar campo de mensagem: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    System.err.println("Erro ao processar resposta do assistente: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao enviar mensagem: " + e.getMessage());
            mensagemField.setDisable(false);
            esperandoResposta = false;
        }
    }
    
    @FXML
    private void handleVoltar() {
        try {
            // Parar o timer de atualização
            if (atualizacaoTimer != null) {
                atualizacaoTimer.stop();
            }

            String telaRetorno = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };
            
            carregarTela(telaRetorno);
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao voltar: " + e.getMessage());
        }
    }
    
    private String formatarTempoDecorrido(long horas) {
        if (horas < 24) {
            return horas + " hora" + (horas == 1 ? "" : "s");
        } else {
            long dias = horas / 24;
            return dias + " dia" + (dias == 1 ? "" : "s");
        }
    }
    
    @FXML
    private void handleIniciarAtendimento() {
        try {
            if (usuarioLogado.getTipo() != Usuario.TipoUsuario.TECNICO) {
                mostrarErro("Apenas técnicos podem alterar o status do chamado");
                return;
            }
            
            chamado = chamadoService.atualizarStatus(chamado.getId(), Chamado.StatusChamado.EM_ANDAMENTO, usuarioLogado);
            carregarDados();
            
        } catch (Exception e) {
            System.err.println("Erro ao iniciar atendimento: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao iniciar atendimento: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleFecharChamado() {
        try {
            if (usuarioLogado.getTipo() != Usuario.TipoUsuario.TECNICO) {
                mostrarErro("Apenas técnicos podem fechar o chamado");
                return;
            }
            
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Fechar Chamado");
            dialog.setHeaderText("Informe a resolução do chamado");
            dialog.setContentText("Resolução:");
            
            dialog.showAndWait().ifPresent(resolucao -> {
                try {
                    chamado = chamadoService.fecharChamado(chamado.getId(), resolucao, usuarioLogado);
                    carregarDados();
                } catch (Exception e) {
                    mostrarErro("Erro ao fechar chamado: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao fechar chamado: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao fechar chamado: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelarChamado() {
        try {
            if (usuarioLogado.getTipo() != Usuario.TipoUsuario.TECNICO) {
                mostrarErro("Apenas técnicos podem cancelar o chamado");
                return;
            }
            
            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Cancelar Chamado");
            confirmacao.setHeaderText("Tem certeza que deseja cancelar este chamado?");
            confirmacao.setContentText("Esta ação não pode ser desfeita.");
            
            confirmacao.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        chamado = chamadoService.atualizarStatus(chamado.getId(), Chamado.StatusChamado.CANCELADO, usuarioLogado);
                        carregarDados();
                    } catch (Exception e) {
                        mostrarErro("Erro ao cancelar chamado: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao cancelar chamado: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao cancelar chamado: " + e.getMessage());
        }
    }
} 