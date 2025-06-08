package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.UsuarioService;
import com.techelp.service.AuthService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleStringProperty;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsuariosController extends BaseController {
    
    private final UsuarioService usuarioService;
    private final AuthService authService;
    
    @FXML
    private TableView<Usuario> usuariosTable;
    
    @FXML
    private TableColumn<Usuario, Long> idColumn;
    
    @FXML
    private TableColumn<Usuario, String> nomeColumn;
    
    @FXML
    private TableColumn<Usuario, String> emailColumn;
    
    @FXML
    private TableColumn<Usuario, String> tipoColumn;
    
    @FXML
    private TableColumn<Usuario, String> dataCriacaoColumn;
    
    @FXML
    private TableColumn<Usuario, Void> acoesColumn;
    
    @FXML
    private TextField pesquisaField;
    
    @FXML
    private ComboBox<Usuario.TipoUsuario> filtroTipoCombo;
    
    public UsuariosController() {
        this.usuarioService = new UsuarioService();
        this.authService = AuthService.getInstance();
    }
    
    @FXML
    public void initialize() {
        try {
            System.out.println("Inicializando UsuariosController");
            
            configurarTabela();
            configurarFiltros();
            atualizarTabela();
            
        } catch (Exception e) {
            System.err.println("Erro ao inicializar tela de usuários: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao inicializar tela de usuários: " + e.getMessage());
        }
    }
    
    private void configurarTabela() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomeColumn.setCellValueFactory(new PropertyValueFactory<>("nome"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        tipoColumn.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        
        dataCriacaoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getDataCriacao() != null ? 
                cellData.getValue().getDataCriacao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
                
        configurarColunaAcoes();
    }
    
    private void configurarFiltros() {
        filtroTipoCombo.setItems(FXCollections.observableArrayList(Usuario.TipoUsuario.values()));
        filtroTipoCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }
    
    private void atualizarTabela() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            ObservableList<Usuario> observableUsuarios = FXCollections.observableArrayList(usuarios);
            usuariosTable.setItems(observableUsuarios);
        } catch (Exception e) {
            System.err.println("Erro ao carregar usuários: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar usuários: " + e.getMessage());
        }
    }
    
    private void aplicarFiltros() {
        try {
            String pesquisa = pesquisaField.getText().toLowerCase();
            Usuario.TipoUsuario tipo = filtroTipoCombo.getValue();
            
            List<Usuario> usuarios = usuarioService.findAll();
            
            List<Usuario> usuariosFiltrados = usuarios.stream()
                .filter(u -> (tipo == null || u.getTipo() == tipo) &&
                            (pesquisa.isEmpty() || 
                             u.getNome().toLowerCase().contains(pesquisa) ||
                             u.getEmail().toLowerCase().contains(pesquisa)))
                .toList();
            
            usuariosTable.setItems(FXCollections.observableArrayList(usuariosFiltrados));
        } catch (Exception e) {
            System.err.println("Erro ao aplicar filtros: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao aplicar filtros: " + e.getMessage());
        }
    }
    
    private void configurarColunaAcoes() {
        acoesColumn.setCellFactory(param -> new TableCell<Usuario, Void>() {
            private final Button editarButton = new Button("Editar");
            private final Button excluirButton = new Button("Excluir");
            
            {
                editarButton.getStyleClass().add("edit-button");
                excluirButton.getStyleClass().add("delete-button");
                
                editarButton.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    try {
                        carregarTela("/fxml/UsuarioForm.fxml", usuario);
                    } catch (Exception e) {
                        UsuariosController.this.mostrarErro("Erro ao editar usuário: " + e.getMessage());
                    }
                });
                
                excluirButton.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    if (UsuariosController.this.confirmarAcao("Excluir Usuário", "Tem certeza que deseja excluir o usuário " + usuario.getNome() + "?")) {
                        try {
                            usuarioService.excluir(usuario.getId());
                            atualizarTabela();
                            UsuariosController.this.mostrarSucesso("Usuário excluído com sucesso!");
                        } catch (Exception e) {
                            UsuariosController.this.mostrarErro("Erro ao excluir usuário: " + e.getMessage());
                        }
                    }
                });
            }

            @Override
            public void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new HBox(10, editarButton, excluirButton));
                }
            }
        });
    }

    @FXML
    private void handleNovoUsuario() {
        try {
            carregarTela("/fxml/UsuarioForm.fxml");
        } catch (Exception e) {
            mostrarErro("Erro ao abrir formulário de novo usuário: " + e.getMessage());
        }
    }

    @FXML
    private void handleVoltar() {
        try {
            Usuario usuarioLogado = authService.getUsuarioLogado();
            if (usuarioLogado == null) {
                carregarTela("/fxml/LoginView.fxml");
                return;
            }

            String fxmlPath = switch (usuarioLogado.getTipo()) {
                case TECNICO -> "/fxml/TecnicoDashboardView.fxml";
                case SOLICITANTE -> "/fxml/SolicitanteDashboardView.fxml";
                case ADMIN -> "/fxml/AdminDashboardView.fxml";
            };

            carregarTela(fxmlPath);
        } catch (Exception e) {
            mostrarErro("Erro ao voltar para a tela principal: " + e.getMessage());
        }
    }
} 