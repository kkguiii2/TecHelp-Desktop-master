package com.techelp.controller;

import com.techelp.model.entity.Chamado;
import com.techelp.model.entity.Usuario;
import com.techelp.model.dto.ChamadoDTO;
import com.techelp.service.ChamadoService;
import com.techelp.service.RelatorioService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.beans.property.SimpleStringProperty;
import java.io.File;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.techelp.service.AuthService;
import com.techelp.service.UsuarioService;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

public class AdminDashboardController extends BaseController {
    
    private final ChamadoService chamadoService;
    private final RelatorioService relatorioService;
    private final AuthService authService;
    private final UsuarioService usuarioService;
    private Usuario adminLogado;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    @FXML
    private Text totalChamadosText;
    
    @FXML
    private Text chamadosAbertosText;
    
    @FXML
    private Text tempoMedioText;
    
    @FXML
    private PieChart chamadosPorCategoriaChart;
    
    @FXML
    private BarChart<String, Number> chamadosPorTecnicoChart;
    
    @FXML
    private ComboBox<Chamado.StatusChamado> filtroStatusCombo;
    
    @FXML
    private ComboBox<Chamado.PrioridadeChamado> filtroPrioridadeCombo;
    
    @FXML
    private ComboBox<Usuario> filtroTecnicoCombo;
    
    @FXML
    private TableView<ChamadoDTO> chamadosTable;
    
    @FXML
    private TableColumn<ChamadoDTO, Long> idColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> tituloColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> solicitanteColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> tecnicoColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> statusColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, String> prioridadeColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, LocalDateTime> dataAberturaColumn;
    
    @FXML
    private TableColumn<ChamadoDTO, Void> acoesColumn;
    
    @FXML
    private TableView<?> usuariosTable;
    
    @FXML
    private TextField pesquisaField;
    
    @FXML
    private Text chamadosFechadosText;
    
    public AdminDashboardController() {
        this.chamadoService = new ChamadoService();
        this.relatorioService = new RelatorioService();
        this.authService = AuthService.getInstance();
        this.usuarioService = new UsuarioService();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando AdminDashboardController");
            
            adminLogado = authService.getUsuarioLogado();
            if (adminLogado == null) {
                System.err.println("Usuário não autenticado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            inicializarNotificacoes(adminLogado);
            carregarDados();
            
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
    
    private void carregarDados() {
        configurarTabela();
        configurarFiltros();
        
        // Carregar lista de técnicos para o filtro
        List<Usuario> tecnicos = usuarioService.listarTecnicos();
        filtroTecnicoCombo.setItems(FXCollections.observableArrayList(tecnicos));
        
        // Carregar todos os chamados inicialmente
        List<ChamadoDTO> todosChamados = chamadoService.listarChamadosPorTecnico(adminLogado);
        chamadosTable.setItems(FXCollections.observableArrayList(todosChamados));
        
        // Atualizar estatísticas e gráficos
        atualizarEstatisticas();
        atualizarGraficos();
    }
    
    private void configurarTabela() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        tituloColumn.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        solicitanteColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getSolicitante().getNome()));
        tecnicoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getTecnico() != null ? 
                cellData.getValue().getTecnico().getNome() : "Não atribuído"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        prioridadeColumn.setCellValueFactory(new PropertyValueFactory<>("prioridade"));
        dataAberturaColumn.setCellValueFactory(new PropertyValueFactory<>("dataAbertura"));
        
        // Formatação de data
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
        
        configurarColunaAcoes();
    }
    
    private void configurarFiltros() {
        filtroStatusCombo.setItems(FXCollections.observableArrayList(Chamado.StatusChamado.values()));
        filtroPrioridadeCombo.setItems(FXCollections.observableArrayList(Chamado.PrioridadeChamado.values()));
        
        filtroStatusCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroPrioridadeCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroTecnicoCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }
    
    private void atualizarEstatisticas() {
        List<ChamadoDTO> chamados = chamadoService.listarChamados();
        long totalChamados = chamados.size();
        long chamadosAbertos = chamados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.ABERTO)
                .count();
        double tempoMedio = chamados.stream()
                .filter(c -> c.getTempoResolucao() != null)
                .mapToLong(ChamadoDTO::getTempoResolucao)
                .average()
                .orElse(0.0);
        
        totalChamadosText.setText(String.valueOf(totalChamados));
        chamadosAbertosText.setText(String.valueOf(chamadosAbertos));
        tempoMedioText.setText(String.format("%.1f horas", tempoMedio));
    }
    
    private void atualizarGraficos() {
        // Gráfico de pizza - Chamados por categoria
        Map<String, Long> chamadosPorCategoria = chamadoService.gerarEstatisticasPorCategoria();
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        chamadosPorCategoria.forEach((categoria, quantidade) -> 
            pieChartData.add(new PieChart.Data(categoria, quantidade)));
            
        chamadosPorCategoriaChart.setData(pieChartData);
        
        // Gráfico de barras - Chamados por técnico
        // Implementar lógica de chamados por técnico
    }
    
    private void aplicarFiltros() {
        List<ChamadoDTO> chamadosFiltrados;
        
        // Se um status específico foi selecionado
        if (filtroStatusCombo.getValue() != null) {
            chamadosFiltrados = chamadoService.listarChamadosPorTecnicoEStatus(adminLogado, filtroStatusCombo.getValue());
        } else {
            chamadosFiltrados = chamadoService.listarChamadosPorTecnico(adminLogado);
        }
        
        // Filtrar por prioridade se selecionada
        if (filtroPrioridadeCombo.getValue() != null) {
            chamadosFiltrados = chamadosFiltrados.stream()
                .filter(c -> c.getPrioridade() == filtroPrioridadeCombo.getValue())
                .toList();
        }
        
        // Filtrar por técnico se selecionado
        if (filtroTecnicoCombo.getValue() != null) {
            chamadosFiltrados = chamadosFiltrados.stream()
                .filter(c -> {
                    if (c.getTecnico() == null) return false;
                    return c.getTecnico().getId().equals(filtroTecnicoCombo.getValue().getId());
                })
                .toList();
        }
        
        // Atualizar a tabela
        chamadosTable.setItems(FXCollections.observableArrayList(chamadosFiltrados));
        
        // Atualizar estatísticas
        long totalChamados = chamadosFiltrados.size();
        long chamadosAbertos = chamadosFiltrados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.ABERTO)
                .count();
        long chamadosFechados = chamadosFiltrados.stream()
                .filter(c -> c.getStatus() == Chamado.StatusChamado.FECHADO)
                .count();
                
        totalChamadosText.setText(String.valueOf(totalChamados));
        chamadosAbertosText.setText(String.valueOf(chamadosAbertos));
        chamadosFechadosText.setText(String.valueOf(chamadosFechados));
    }
    
    @FXML
    private void handleUsuarios() {
        try {
            System.out.println("Abrindo gerenciamento de usuários");
            carregarTela("/fxml/UsuariosView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir usuários: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir gerenciamento de usuários: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRelatorios() {
        try {
            System.out.println("Abrindo relatórios");
            carregarTela("/fxml/RelatoriosView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir relatórios: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir relatórios: " + e.getMessage());
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
    
    @FXML
    private void handleExportarPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salvar Relatório");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            
        File file = fileChooser.showSaveDialog(chamadosTable.getScene().getWindow());
        
        if (file != null) {
            try {
                byte[] pdf = relatorioService.gerarRelatorioDesempenho(
                    LocalDateTime.now().minusMonths(1), 
                    LocalDateTime.now()
                );
                Files.write(file.toPath(), pdf);
            } catch (Exception e) {
                mostrarErro("Erro ao gerar relatório: " + e.getMessage());
            }
        }
    }
    
    private void verChamado(ChamadoDTO chamado) {
        try {
            carregarTela("/fxml/ChamadoView.fxml", Map.of("chamado", chamado));
        } catch (Exception e) {
            mostrarErro("Erro ao abrir chamado: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void resolverChamado(ChamadoDTO chamado) {
        try {
            carregarTela("/fxml/ChamadoView.fxml", Map.of(
                "chamado", chamado,
                "modo", "RESOLVER",
                "callback", (Runnable) this::atualizarDados
            ));
        } catch (Exception e) {
            mostrarErro("Erro ao abrir tela de resolução: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void editarChamado(ChamadoDTO chamado) {
        Dialog<ChamadoDTO> dialog = new Dialog<>();
        dialog.setTitle("Editar Chamado");
        dialog.setHeaderText("Editar informações do chamado");
        
        ButtonType salvarButtonType = new ButtonType("Salvar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(salvarButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        ComboBox<Chamado.StatusChamado> statusCombo = new ComboBox<>();
        statusCombo.setItems(FXCollections.observableArrayList(Chamado.StatusChamado.values()));
        statusCombo.setValue(chamado.getStatus());
        
        ComboBox<Chamado.PrioridadeChamado> prioridadeCombo = new ComboBox<>();
        prioridadeCombo.setItems(FXCollections.observableArrayList(Chamado.PrioridadeChamado.values()));
        prioridadeCombo.setValue(chamado.getPrioridade());
        
        content.getChildren().addAll(
            new Label("Status:"),
            statusCombo,
            new Label("Prioridade:"),
            prioridadeCombo
        );
        
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == salvarButtonType) {
                try {
                    chamado.setStatus(statusCombo.getValue());
                    chamado.setPrioridade(prioridadeCombo.getValue());
                    chamadoService.atualizarChamado(chamado);
                    carregarDados();
                    atualizarEstatisticas();
                    return chamado;
                } catch (Exception e) {
                    mostrarErro("Erro ao salvar chamado: " + e.getMessage());
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    private void confirmarExclusao(ChamadoDTO chamado) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Exclusão");
        alert.setHeaderText("Excluir Chamado");
        alert.setContentText("Tem certeza que deseja excluir o chamado #" + chamado.getId() + "?");
        
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    chamadoService.excluirChamado(chamado.getId());
                    carregarDados();
                    atualizarEstatisticas();
                } catch (Exception e) {
                    mostrarErro("Erro ao excluir chamado: " + e.getMessage());
                }
            }
        });
    }
    
    private void configurarColunaAcoes() {
        acoesColumn.setCellFactory(col -> new TableCell<ChamadoDTO, Void>() {
            private final Button verButton = new Button("Ver");
            private final Button resolverButton = new Button("Resolver");
            private final HBox container = new HBox(5);

            {
                verButton.getStyleClass().addAll("action-button", "view");
                resolverButton.getStyleClass().addAll("action-button", "resolve");
                
                container.getChildren().addAll(verButton, resolverButton);
                container.setAlignment(javafx.geometry.Pos.CENTER);

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
                    setGraphic(container);
                }
            }
        });
    }
    
    private void atualizarDados() {
        try {
            atualizarEstatisticas();
            atualizarGraficos();
            aplicarFiltros();
        } catch (Exception e) {
            mostrarErro("Erro ao atualizar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 