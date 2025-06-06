package com.techelp.controller;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.model.dto.ChamadoDTO;
import com.techelp.service.ChamadoService;
import com.techelp.service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
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
            
            solicitanteLogado = authService.getUsuarioLogado();
            if (solicitanteLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            // Primeiro configura a tabela e os filtros
            configurarTabela();
            configurarFiltros();
            
            // Depois inicializa as notificações
            inicializarNotificacoes(solicitanteLogado);
            
            // Por último carrega os dados
            carregarChamados();
            
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
        
        acoesColumn.setCellFactory(column -> new TableCell<ChamadoDTO, Void>() {
            private final Button verButton = new Button("Ver");
            private final Button avaliarButton = new Button("Avaliar");
            private final HBox box = new HBox(5, verButton, avaliarButton);
            
            {
                verButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    verChamado(chamado);
                });
                
                avaliarButton.setOnAction(event -> {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    avaliarChamado(chamado);
                });
                
                verButton.getStyleClass().add("secondary-button");
                avaliarButton.getStyleClass().add("primary-button");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    ChamadoDTO chamado = getTableView().getItems().get(getIndex());
                    avaliarButton.setVisible(chamado.getStatus() == Chamado.StatusChamado.FECHADO && 
                        chamado.getAvaliacao() == null);
                    setGraphic(box);
                }
            }
        });
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
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Avaliar Chamado");
        dialog.setHeaderText("Avalie o atendimento de 1 a 5");
        
        ComboBox<Integer> notaCombo = new ComboBox<>(
            FXCollections.observableArrayList(1, 2, 3, 4, 5)
        );
        
        dialog.getDialogPane().setContent(notaCombo);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return notaCombo.getValue();
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(avaliacao -> {
            try {
                chamadoService.avaliarChamado(chamado.getId(), avaliacao);
                carregarChamados();
                mostrarSucesso("Avaliação registrada com sucesso!");
            } catch (Exception e) {
                mostrarErro("Erro ao registrar avaliação: " + e.getMessage());
            }
        });
    }
    
    private void carregarChamados() {
        List<ChamadoDTO> chamados = chamadoService.listarChamadosPorSolicitante(solicitanteLogado);
        chamadosTable.setItems(FXCollections.observableArrayList(chamados));
        atualizarEstatisticas();
        atualizarGraficos();
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