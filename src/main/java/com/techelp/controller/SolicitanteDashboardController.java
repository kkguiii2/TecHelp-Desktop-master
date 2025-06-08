package com.techelp.controller;

import com.techelp.model.dto.ChamadoDTO;
import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.application.Platform;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SolicitanteDashboardController extends BaseController {
    
    private final ChamadoService chamadoService;
    private final AuthService authService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public SolicitanteDashboardController() {
        this.chamadoService = new ChamadoService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    private Text chamadosAbertosText;
    
    @FXML
    private Text chamadosAndamentoText;
    
    @FXML
    private Text chamadosResolvidosText;
    
    @FXML
    private ComboBox<Chamado.StatusChamado> filtroStatusCombo;
    
    @FXML
    private ComboBox<Chamado.PrioridadeChamado> filtroPrioridadeCombo;
    
    @FXML
    private TextField pesquisaField;
    
    @FXML
    private TableView<ChamadoDTO> chamadosTable;
    
    @FXML
    private TableColumn<ChamadoDTO, Long> idColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> tituloColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> tecnicoColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> statusColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> prioridadeColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, LocalDateTime> dataAberturaColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> tempoDecorridoColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, Void> acoesColumn;
    
    @FXML
    private PieChart chamadosPorStatusChart;
    
    @FXML
    private PieChart chamadosPorCategoriaChart;
    
    private Usuario solicitanteLogado;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando SolicitanteDashboardController");
            
            // Verifica se o usuário está autenticado
            solicitanteLogado = authService.getUsuarioLogado();
            if (solicitanteLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            // Verifica se todas as colunas foram injetadas corretamente
            if (idColumn == null || tituloColumn == null || tecnicoColumn == null ||
                statusColumn == null || prioridadeColumn == null || dataAberturaColumn == null ||
                tempoDecorridoColumn == null || acoesColumn == null) {
                throw new RuntimeException("Uma ou mais colunas da tabela não foram inicializadas corretamente");
            }
            
            // Configura a tabela e os filtros
            Platform.runLater(() -> {
                try {
                    configurarTabela();
                    configurarFiltros();
                    
                    // Inicializa as notificações
                    inicializarNotificacoes(solicitanteLogado);
                    
                    // Carrega os dados
                    carregarChamados();
                } catch (Exception e) {
                    System.err.println("Erro ao configurar componentes: " + e.getMessage());
                    e.printStackTrace();
                    mostrarErro("Erro ao configurar componentes: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar dashboard: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar dashboard: " + e.getMessage());
            try {
                carregarTela("/fxml/LoginView.fxml");
            } catch (Exception ex) {
                System.err.println("Erro ao redirecionar para login: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    private void configurarTabela() {
        try {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tituloColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
            tecnicoColumn.setCellValueFactory(cellData -> 
                new SimpleStringProperty(
                    cellData.getValue().getTecnico() != null ? 
                    cellData.getValue().getTecnico().getNome() : "Não atribuído"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            prioridadeColumn.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
            
            dataAberturaColumn.setCellValueFactory(new PropertyValueFactory<>("dataAbertura"));
            dataAberturaColumn.setCellFactory(column -> new TableCell<ChamadoDTO, LocalDateTime>() {
                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(DATE_FORMATTER.format(item));
                    }
                }
            });
            
            tempoDecorridoColumn.setCellValueFactory(cellData -> {
                LocalDateTime abertura = cellData.getValue().getDataAbertura();
                LocalDateTime agora = LocalDateTime.now();
                long horas = ChronoUnit.HOURS.between(abertura, agora);
                return new SimpleStringProperty(horas + "h");
            });
            
            configurarColunaAcoes();
        } catch (Exception e) {
            System.err.println("Erro ao configurar tabela: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao configurar tabela: " + e.getMessage());
        }
    }
    
    private void configurarFiltros() {
        filtroStatusCombo.setItems(FXCollections.observableArrayList(Chamado.StatusChamado.values()));
        filtroPrioridadeCombo.setItems(FXCollections.observableArrayList(Chamado.PrioridadeChamado.values()));
        
        filtroStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroPrioridadeCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }
    
    private void atualizarEstatisticas() {
        List<ChamadoDTO> chamados = chamadoService.listarChamadosPorSolicitante(solicitanteLogado);
        
        long chamadosAbertos = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.ABERTO)
                .count();
                
        long chamadosAndamento = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.EM_ANDAMENTO)
                .count();
                
        long chamadosResolvidos = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.FECHADO)
                .count();
        
        chamadosAbertosText.setText(String.valueOf(chamadosAbertos));
        chamadosAndamentoText.setText(String.valueOf(chamadosAndamento));
        chamadosResolvidosText.setText(String.valueOf(chamadosResolvidos));
    }
    
    private void atualizarGraficos() {
        List<ChamadoDTO> chamados = chamadoService.listarChamadosPorSolicitante(solicitanteLogado);
        
        // Gráfico de pizza - Chamados por status
        Map<Chamado.StatusChamado, Long> chamadosPorStatus = chamados.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    ChamadoDTO::getStatus, 
                    java.util.stream.Collectors.counting()
                ));
                
        ObservableList<PieChart.Data> statusChartData = FXCollections.observableArrayList();
        chamadosPorStatus.forEach((status, quantidade) -> 
            statusChartData.add(new PieChart.Data(status.toString(), quantidade)));
        chamadosPorStatusChart.setData(statusChartData);
        
        // Gráfico de pizza - Chamados por categoria
        Map<String, Long> chamadosPorCategoria = chamados.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                    chamado -> chamado.getCategoriaIa() != null ? chamado.getCategoriaIa() : "Sem Categoria",
                    java.util.stream.Collectors.counting()
                ));
                
        ObservableList<PieChart.Data> categoriaChartData = FXCollections.observableArrayList();
        chamadosPorCategoria.forEach((categoria, quantidade) -> 
            categoriaChartData.add(new PieChart.Data(categoria, quantidade)));
        chamadosPorCategoriaChart.setData(categoriaChartData);
    }
    
    private void aplicarFiltros() {
        String pesquisa = pesquisaField.getText().toLowerCase();
        Chamado.StatusChamado status = filtroStatusCombo.getValue();
        Chamado.PrioridadeChamado prioridade = filtroPrioridadeCombo.getValue();
        
        List<ChamadoDTO> chamadosFiltrados = chamadoService.listarChamadosPorSolicitante(solicitanteLogado)
            .stream()
            .filter(c -> (status == null || c.getStatus() == status) &&
                        (prioridade == null || c.getPrioridade() == prioridade) &&
                        (pesquisa.isEmpty() || 
                         c.getTitulo().toLowerCase().contains(pesquisa) ||
                         c.getDescricao().toLowerCase().contains(pesquisa)))
            .toList();
            
        chamadosTable.setItems(FXCollections.observableArrayList(chamadosFiltrados));
    }
    
    private void verChamado(ChamadoDTO chamado) {
        try {
            carregarTela("/fxml/ChamadoView.fxml", chamado);
        } catch (Exception e) {
            System.err.println("Erro ao abrir chamado: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir chamado: " + e.getMessage());
        }
    }
    
    private void avaliarChamado(ChamadoDTO chamado) {
        // Verificação adicional de segurança
        if (chamado.getStatus() != Chamado.StatusChamado.FECHADO) {
            mostrarErro("Apenas chamados fechados podem ser avaliados.");
            return;
        }
        if (chamado.getAvaliacao() != null) {
            mostrarErro("Este chamado já foi avaliado.");
            return;
        }

        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Avaliar Chamado");
        dialog.setHeaderText("Como foi seu atendimento?");
        
        // Container para as opções de avaliação
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        
        // Criar botões de avaliação
        HBox ratingBox = new HBox(10);
        ratingBox.setStyle("-fx-alignment: center;");
        
        SimpleIntegerProperty selectedRating = new SimpleIntegerProperty(0);
        List<Button> ratingButtons = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Button ratingButton = new Button(String.valueOf(i));
            final int rating = i;
            
            ratingButton.getStyleClass().add("rating-button");
            ratingButton.setOnAction(e -> {
                selectedRating.set(rating);
                // Atualizar estilo dos botões
                for (int j = 0; j < ratingButtons.size(); j++) {
                    Button btn = ratingButtons.get(j);
                    if (j < rating) {
                        btn.getStyleClass().add("selected");
                    } else {
                        btn.getStyleClass().remove("selected");
                    }
                }
            });
            
            ratingButtons.add(ratingButton);
            ratingBox.getChildren().add(ratingButton);
        }
        
        content.getChildren().add(ratingBox);
        dialog.getDialogPane().setContent(content);
        
        // Botões de confirmação
        ButtonType confirmButtonType = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, cancelButtonType);
        
        // Desabilitar o botão de confirmar até uma avaliação ser selecionada
        Button confirmButton = (Button) dialog.getDialogPane().lookupButton(confirmButtonType);
        confirmButton.setDisable(true);
        
        // Habilitar o botão quando uma avaliação for selecionada
        selectedRating.addListener((obs, oldVal, newVal) -> {
            confirmButton.setDisable(newVal.intValue() == 0);
        });
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == confirmButtonType) {
                return selectedRating.get();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(avaliacao -> {
            try {
                // Salva a avaliação no banco
                chamadoService.avaliarChamado(chamado.getId(), avaliacao);
                
                // Força uma atualização completa dos dados
                chamadoService.limparCache();
                
                // Atualiza a interface em um único runLater para evitar problemas de concorrência
                Platform.runLater(() -> {
                    // Recarrega o chamado do banco para garantir dados atualizados
                    ChamadoDTO chamadoAtualizado = new ChamadoDTO(chamadoService.buscarChamado(chamado.getId()));
                    
                    // Atualiza o objeto local com os dados do banco
                    chamado.setAvaliacao(chamadoAtualizado.getAvaliacao());
                    
                    // Força uma atualização completa da tabela
                    carregarChamados();
                    
                    // Mostra mensagem de sucesso após a atualização
                    mostrarSucesso("Avaliação registrada com sucesso!");
                });
            } catch (Exception e) {
                mostrarErro("Erro ao registrar avaliação: " + e.getMessage());
            }
        });
    }
    
    private void configurarColunaAcoes() {
        acoesColumn.setCellFactory(column -> new TableCell<ChamadoDTO, Void>() {
            private final Button verButton = new Button("Ver");
            private final Button avaliarButton = new Button("Avaliar");
            private HBox box;
            
            {
                verButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    verChamado(chamado);
                });
                
                avaliarButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    avaliarChamado(chamado);
                });
                
                verButton.getStyleClass().addAll("action-button", "view");
                avaliarButton.getStyleClass().addAll("action-button", "rate");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    // Mostra o botão de avaliação apenas se o chamado estiver fechado E não tiver sido avaliado ainda
                    boolean podeAvaliar = chamado.getStatus() == Chamado.StatusChamado.FECHADO && 
                                        chamado.getAvaliacao() == null;
                    
                    // Recria o HBox com os botões necessários
                    box = new HBox(5);
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    box.getChildren().add(verButton);
                    
                    if (podeAvaliar) {
                        box.getChildren().add(avaliarButton);
                    }
                    
                    setGraphic(box);
                }
            }
        });
    }
    
    private void carregarChamados() {
        // Limpa o cache antes de recarregar os dados
        chamadoService.limparCache();
        
        Platform.runLater(() -> {
            // Busca os dados atualizados do banco
            List<ChamadoDTO> chamados = chamadoService.listarChamadosPorSolicitante(solicitanteLogado);
            
            // Atualiza a tabela
            chamadosTable.getItems().clear();
            chamadosTable.setItems(FXCollections.observableArrayList(chamados));
            
            // Força uma atualização visual completa
            chamadosTable.refresh();
            
            // Atualiza as estatísticas e gráficos
            atualizarEstatisticas();
            atualizarGraficos();
        });
    }
    
    @FXML
    private void handleNovoChamado() {
        try {
            carregarTela("/fxml/ChamadoView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir formulário: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir formulário de chamado: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePerfil() {
        try {
            carregarTela("/fxml/PerfilView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir perfil: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir perfil: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        try {
            System.out.println("Realizando logout");
            authService.logout();
            carregarTela("/fxml/LoginView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao realizar logout: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao realizar logout: " + e.getMessage());
        }
    }
} 