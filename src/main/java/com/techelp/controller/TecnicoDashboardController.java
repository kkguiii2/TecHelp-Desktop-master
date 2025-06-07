package com.techelp.controller;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.model.dto.ChamadoDTO;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.application.Platform;

public class TecnicoDashboardController extends BaseController {
    
    private final ChamadoService chamadoService;
    private final AuthService authService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public TecnicoDashboardController() {
        this.chamadoService = new ChamadoService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    private Text chamadosAndamentoText;
    
    @FXML
    private Text chamadosResolvidosText;
    
    @FXML
    private Text tempoMedioText;
    
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
    private TableColumn<ChamadoDTO, String> solicitanteColumn;
    
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
    private PieChart chamadosPorCategoriaChart;
    
    @FXML
    private LineChart<String, Number> desempenhoChart;
    
    private Usuario tecnicoLogado;
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando TecnicoDashboardController");
            
            // Verifica se os componentes foram injetados corretamente
            if (chamadosTable == null) {
                throw new RuntimeException("TableView não foi injetada corretamente");
            }
            
            tecnicoLogado = authService.getUsuarioLogado();
            if (tecnicoLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            System.out.println("Técnico logado: " + tecnicoLogado.getNome());
            
            configurarTabela();
            configurarFiltros();
            aplicarFiltros();
            atualizarEstatisticas();
            atualizarGraficos();
            
            pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    aplicarFiltros();
                } catch (Exception e) {
                    System.err.println("Erro ao aplicar filtros: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            inicializarNotificacoes(tecnicoLogado);
            
            // Configura atualização automática a cada 30 segundos
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
                Platform.runLater(() -> {
                    System.out.println("Atualizando lista de chamados automaticamente");
                    aplicarFiltros();
                    atualizarEstatisticas();
                    atualizarGraficos();
                });
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
            
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tituloColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        solicitanteColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSolicitante().getNome()));
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
    }
    
    private void configurarFiltros() {
        filtroStatusCombo.setItems(FXCollections.observableArrayList(Chamado.StatusChamado.values()));
        filtroPrioridadeCombo.setItems(FXCollections.observableArrayList(Chamado.PrioridadeChamado.values()));
        
        filtroStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroPrioridadeCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }
    
    private void atualizarEstatisticas() {
        try {
            List<ChamadoDTO> chamados = chamadoService.listarChamadosPorTecnico(tecnicoLogado);
            if (chamados == null) {
                chamados = new ArrayList<>();
            }
            
            long chamadosAndamento = chamados.stream()
                    .filter(c -> c.getStatus() == Chamado.StatusChamado.EM_ANDAMENTO)
                    .count();
                    
            long chamadosResolvidos = chamados.stream()
                    .filter(c -> c.getStatus() == Chamado.StatusChamado.FECHADO)
                    .count();
                    
            double tempoMedio = chamados.stream()
                    .filter(c -> c.getTempoResolucao() != null)
                    .mapToLong(ChamadoDTO::getTempoResolucao)
                    .average()
                    .orElse(0.0);
            
            chamadosAndamentoText.setText(String.valueOf(chamadosAndamento));
            chamadosResolvidosText.setText(String.valueOf(chamadosResolvidos));
            tempoMedioText.setText(String.format("%.1f horas", tempoMedio));
        } catch (Exception e) {
            mostrarErro("Erro ao atualizar estatísticas: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void atualizarGraficos() {
        try {
            // Gráfico de pizza - Chamados por categoria
            Map<String, Long> chamadosPorCategoria = chamadoService.gerarEstatisticasPorCategoria();
            if (chamadosPorCategoria == null) {
                chamadosPorCategoria = new HashMap<>();
            }
            
            ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
            
            chamadosPorCategoria.forEach((categoria, quantidade) -> 
                pieChartData.add(new PieChart.Data(categoria, quantidade)));
                
            chamadosPorCategoriaChart.setData(pieChartData);
            
            // Gráfico de linha - Desempenho últimos 7 dias
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Tempo Médio");
            
            LocalDateTime hoje = LocalDateTime.now();
            for (int i = 6; i >= 0; i--) {
                LocalDateTime data = hoje.minusDays(i);
                String dataStr = data.format(DateTimeFormatter.ofPattern("dd/MM"));
                Double tempoMedio = chamadoService.calcularTempoMedioResolucaoPorData(tecnicoLogado, data);
                series.getData().add(new XYChart.Data<>(dataStr, tempoMedio != null ? tempoMedio : 0.0));
            }
            
            desempenhoChart.getData().clear();
            desempenhoChart.getData().add(series);
        } catch (Exception e) {
            mostrarErro("Erro ao atualizar gráficos: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void aplicarFiltros() {
        try {
            System.out.println("Aplicando filtros na dashboard do técnico");
            String pesquisa = pesquisaField.getText().toLowerCase();
            Chamado.StatusChamado status = filtroStatusCombo.getValue();
            Chamado.PrioridadeChamado prioridade = filtroPrioridadeCombo.getValue();
            
            System.out.println("Buscando chamados para o técnico: " + tecnicoLogado.getNome());
            List<ChamadoDTO> chamados = chamadoService.listarChamadosPorTecnico(tecnicoLogado);
            System.out.println("Total de chamados encontrados: " + chamados.size());
            
            List<ChamadoDTO> chamadosFiltrados = chamados.stream()
                .filter(c -> (status == null || c.getStatus() == status) &&
                            (prioridade == null || c.getPrioridade() == prioridade) &&
                            (pesquisa.isEmpty() || 
                             c.getTitulo().toLowerCase().contains(pesquisa) ||
                             c.getDescricao().toLowerCase().contains(pesquisa) ||
                             c.getSolicitante().getNome().toLowerCase().contains(pesquisa)))
                .toList();
            
            System.out.println("Total de chamados após filtros: " + chamadosFiltrados.size());
            chamadosTable.setItems(FXCollections.observableArrayList(chamadosFiltrados));
        } catch (Exception e) {
            System.err.println("Erro ao aplicar filtros: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao aplicar filtros: " + e.getMessage());
        }
    }
    
    private void verChamado(ChamadoDTO chamado) {
        try {
            carregarTela("/fxml/ChamadoView.fxml", chamado);
        } catch (Exception e) {
            mostrarErro("Erro ao abrir chamado: " + e.getMessage());
        }
    }
    
    private void resolverChamado(ChamadoDTO chamado) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Resolver Chamado");
        dialog.setHeaderText("Informe a resolução do chamado");
        dialog.setContentText("Resolução:");
        
        dialog.showAndWait().ifPresent(resolucao -> {
            try {
                chamadoService.fecharChamado(chamado.getId(), resolucao, tecnicoLogado);
                atualizarEstatisticas();
                atualizarGraficos();
                aplicarFiltros();
            } catch (Exception e) {
                mostrarErro("Erro ao resolver chamado: " + e.getMessage());
            }
        });
    }
    
    private void alterarStatusChamado(ChamadoDTO chamado, Chamado.StatusChamado novoStatus) {
        try {
            chamadoService.atualizarStatus(chamado.getId(), novoStatus, tecnicoLogado);
            atualizarEstatisticas();
            atualizarGraficos();
            aplicarFiltros();
        } catch (Exception e) {
            mostrarErro("Erro ao alterar status do chamado: " + e.getMessage());
        }
    }
    
    @FXML
    private void handlePerfil() {
        try {
            System.out.println("Abrindo perfil do usuário");
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
    
    private void configurarColunaAcoes() {
        acoesColumn.setCellFactory(col -> new TableCell<ChamadoDTO, Void>() {
            private final Button verButton = new Button("Ver");
            private final Button resolverButton = new Button("Resolver");
            private final HBox container = new HBox(5); // 5px de espaçamento

            {
                // Configurar botões
                verButton.getStyleClass().addAll("action-button", "view");
                resolverButton.getStyleClass().addAll("action-button", "resolve");
                
                // Configurar container
                container.getChildren().addAll(verButton, resolverButton);
                container.setAlignment(javafx.geometry.Pos.CENTER);

                // Configurar ações
                verButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    verChamado(chamado);
                });

                resolverButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    resolverChamado(chamado);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    
                    // Ajustar visibilidade dos botões baseado no status
                    resolverButton.setVisible(chamado.getStatus() != Chamado.StatusChamado.FECHADO && 
                                           chamado.getStatus() != Chamado.StatusChamado.CANCELADO);
                    
                    setGraphic(container);
                }
            }
        });
    }
} 