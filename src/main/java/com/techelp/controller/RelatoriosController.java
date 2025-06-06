package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.model.entity.Chamado;
import com.techelp.service.AuthService;
import com.techelp.service.RelatorioService;
import com.techelp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.text.Text;
import java.time.LocalDate;
import java.time.LocalDateTime;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.collections.ObservableList;
import java.util.Map;
import java.util.List;

public class RelatoriosController extends BaseController {
    
    private final AuthService authService;
    private final RelatorioService relatorioService;
    private final UsuarioService usuarioService;
    private Usuario usuarioLogado;
    
    @FXML
    private DatePicker dataInicioPicker;
    
    @FXML
    private DatePicker dataFimPicker;
    
    @FXML
    private ComboBox<Usuario> tecnicoCombo;
    
    @FXML
    private ComboBox<String> categoriaCombo;
    
    @FXML
    private DatePicker dataInicioDesempenhoPicker;
    
    @FXML
    private DatePicker dataFimDesempenhoPicker;
    
    @FXML
    private DatePicker dataInicioCategoriasPicker;
    
    @FXML
    private DatePicker dataFimCategoriasPicker;
    
    @FXML
    private Text totalChamadosText;
    
    @FXML
    private Text tempoMedioText;
    
    @FXML
    private Text satisfacaoMediaText;
    
    @FXML
    private PieChart statusChart;
    
    @FXML
    private PieChart categoriaChart;
    
    @FXML
    private LineChart<String, Number> chamadosPorDiaChart;
    
    public RelatoriosController() {
        this.authService = AuthService.getInstance();
        this.relatorioService = new RelatorioService();
        this.usuarioService = new UsuarioService();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando RelatoriosController");
            
            usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null || usuarioLogado.getTipo() != Usuario.TipoUsuario.ADMIN) {
                System.err.println("Usuário não autorizado, redirecionando para login");
                carregarTela("/fxml/LoginView.fxml");
                return;
            }
            
            // Carrega os técnicos para o combo
            List<Usuario> tecnicos = usuarioService.listarTodos().stream()
                .filter(u -> u.getTipo() == Usuario.TipoUsuario.TECNICO)
                .toList();
            tecnicoCombo.setItems(FXCollections.observableArrayList(tecnicos));
            
            // Carrega as categorias para o combo
            List<String> categorias = relatorioService.getTodasCategorias();
            categoriaCombo.setItems(FXCollections.observableArrayList(categorias));
            
            // Configura as datas iniciais (último mês)
            LocalDate hoje = LocalDate.now();
            LocalDate mesPassado = hoje.minusMonths(1);
            
            dataInicioPicker.setValue(mesPassado);
            dataFimPicker.setValue(hoje);
            dataInicioDesempenhoPicker.setValue(mesPassado);
            dataFimDesempenhoPicker.setValue(hoje);
            dataInicioCategoriasPicker.setValue(mesPassado);
            dataFimCategoriasPicker.setValue(hoje);
            
            // Adiciona listeners para atualizar os gráficos quando as datas mudarem
            dataInicioPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            dataFimPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            dataInicioDesempenhoPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            dataFimDesempenhoPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            dataInicioCategoriasPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            dataFimCategoriasPicker.valueProperty().addListener((obs, oldVal, newVal) -> atualizarRelatorios());
            
            // Carrega os dados iniciais
            atualizarRelatorios();
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar relatórios: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar relatórios: " + e.getMessage());
        }
    }
    
    private void atualizarRelatorios() {
        try {
            LocalDate dataInicio = dataInicioPicker.getValue();
            LocalDate dataFim = dataFimPicker.getValue();
            
            if (dataInicio == null || dataFim == null) {
                mostrarErro("Por favor, selecione o período do relatório");
                return;
            }
            
            if (dataFim.isBefore(dataInicio)) {
                mostrarErro("A data final deve ser posterior à data inicial");
                return;
            }
            
            // Converte LocalDate para LocalDateTime
            LocalDateTime inicio = dataInicio.atStartOfDay();
            LocalDateTime fim = dataFim.atTime(23, 59, 59);
            
            // Atualiza estatísticas
            int totalChamados = relatorioService.getTotalChamados(inicio, fim);
            double tempoMedio = relatorioService.getTempoMedioResolucao(inicio, fim);
            double satisfacaoMedia = relatorioService.getSatisfacaoMedia(inicio, fim);
            
            totalChamadosText.setText(String.valueOf(totalChamados));
            tempoMedioText.setText(String.format("%.1f horas", tempoMedio));
            satisfacaoMediaText.setText(String.format("%.1f", satisfacaoMedia));
            
            // Atualiza gráficos
            atualizarGraficoStatus(inicio, fim);
            atualizarGraficoCategoria(inicio, fim);
            atualizarGraficoChamadosPorDia(inicio, fim);
            
        } catch (Exception e) {
            System.err.println("Erro ao atualizar relatórios: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao atualizar relatórios: " + e.getMessage());
        }
    }
    
    private void atualizarGraficoStatus(LocalDateTime inicio, LocalDateTime fim) {
        Map<Chamado.StatusChamado, Long> dadosStatus = relatorioService.getChamadosPorStatus(inicio, fim);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        dadosStatus.forEach((status, quantidade) ->
            pieChartData.add(new PieChart.Data(status.toString(), quantidade)));
            
        statusChart.setData(pieChartData);
    }
    
    private void atualizarGraficoCategoria(LocalDateTime inicio, LocalDateTime fim) {
        Map<String, Long> dadosCategoria = relatorioService.getChamadosPorCategoria(inicio, fim);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        dadosCategoria.forEach((categoria, quantidade) ->
            pieChartData.add(new PieChart.Data(categoria, quantidade)));
            
        categoriaChart.setData(pieChartData);
    }
    
    private void atualizarGraficoChamadosPorDia(LocalDateTime inicio, LocalDateTime fim) {
        Map<LocalDate, Long> dadosDiarios = relatorioService.getChamadosPorDia(inicio, fim);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Chamados por Dia");
        
        dadosDiarios.forEach((data, quantidade) ->
            series.getData().add(new XYChart.Data<>(data.toString(), quantidade)));
            
        chamadosPorDiaChart.getData().clear();
        chamadosPorDiaChart.getData().add(series);
    }
    
    @FXML
    private void handleGerarRelatorioPorTecnico() {
        try {
            Usuario tecnico = tecnicoCombo.getValue();
            LocalDate dataInicio = dataInicioPicker.getValue();
            LocalDate dataFim = dataFimPicker.getValue();
            
            if (tecnico == null || dataInicio == null || dataFim == null) {
                mostrarErro("Por favor, preencha todos os campos");
                return;
            }
            
            if (dataFim.isBefore(dataInicio)) {
                mostrarErro("A data final deve ser posterior à data inicial");
                return;
            }
            
            relatorioService.gerarRelatorioPorTecnico(tecnico, dataInicio, dataFim);
            mostrarSucesso("Relatório gerado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório por técnico: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao gerar relatório: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGerarRelatorioDesempenho() {
        try {
            LocalDate dataInicio = dataInicioDesempenhoPicker.getValue();
            LocalDate dataFim = dataFimDesempenhoPicker.getValue();
            
            if (dataInicio == null || dataFim == null) {
                mostrarErro("Por favor, preencha as datas");
                return;
            }
            
            if (dataFim.isBefore(dataInicio)) {
                mostrarErro("A data final deve ser posterior à data inicial");
                return;
            }
            
            relatorioService.gerarRelatorioDesempenho(dataInicio, dataFim);
            mostrarSucesso("Relatório gerado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório de desempenho: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao gerar relatório: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGerarRelatorioPorCategoria() {
        try {
            String categoria = categoriaCombo.getValue();
            LocalDate dataInicio = dataInicioCategoriasPicker.getValue();
            LocalDate dataFim = dataFimCategoriasPicker.getValue();
            
            if (categoria == null || dataInicio == null || dataFim == null) {
                mostrarErro("Por favor, preencha todos os campos");
                return;
            }
            
            if (dataFim.isBefore(dataInicio)) {
                mostrarErro("A data final deve ser posterior à data inicial");
                return;
            }
            
            relatorioService.gerarRelatorioPorCategoria(categoria, dataInicio, dataFim);
            mostrarSucesso("Relatório gerado com sucesso!");
            
        } catch (Exception e) {
            System.err.println("Erro ao gerar relatório por categoria: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao gerar relatório: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoltar() {
        try {
            carregarTela("/fxml/AdminDashboardView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao voltar: " + e.getMessage());
        }
    }
} 