package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.UsuarioService;
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
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
    private TableColumn<Usuario, String> telefoneColumn;
    
    @FXML
    private TableColumn<Usuario, String> dataCriacaoColumn;
    
    @FXML
    private TableColumn<Usuario, String> ultimoAcessoColumn;
    
    @FXML
    private TableColumn<Usuario, Void> acoesColumn;
    
    @FXML
    private TextField pesquisaField;
    
    @FXML
    private ComboBox<Usuario.TipoUsuario> filtroTipoCombo;
    
    public UsuariosController() {
        this.usuarioService = new UsuarioService();
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
        telefoneColumn.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        
        dataCriacaoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getDataCriacao() != null ? 
                DATE_FORMATTER.format(cellData.getValue().getDataCriacao()) : ""));
                
        ultimoAcessoColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(
                cellData.getValue().getUltimoAcesso() != null ? 
                DATE_FORMATTER.format(cellData.getValue().getUltimoAcesso()) : ""));
        
        configurarColunaAcoes();
    }
    
    private void configurarColunaAcoes() {
        acoesColumn.setCellFactory(col -> new TableCell<Usuario, Void>() {
            private final Button editarButton = new Button("Editar");
            private final Button excluirButton = new Button("Excluir");
            private final HBox container = new HBox(5);
            
            {
                editarButton.getStyleClass().addAll("action-button", "edit");
                excluirButton.getStyleClass().addAll("action-button", "delete");
                
                container.getChildren().addAll(editarButton, excluirButton);
                container.setAlignment(javafx.geometry.Pos.CENTER);
                
                editarButton.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarUsuario(usuario);
                });
                
                excluirButton.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    excluirUsuario(usuario);
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
    
    private void configurarFiltros() {
        filtroTipoCombo.setItems(FXCollections.observableArrayList(Usuario.TipoUsuario.values()));
        
        pesquisaField.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        filtroTipoCombo.valueProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
    }
    
    private void aplicarFiltros() {
        try {
            String termoPesquisa = pesquisaField.getText().toLowerCase();
            Usuario.TipoUsuario tipoFiltro = filtroTipoCombo.getValue();
            
            List<Usuario> usuarios = usuarioService.listarTodos();
            
            List<Usuario> usuariosFiltrados = usuarios.stream()
                .filter(u -> tipoFiltro == null || u.getTipo() == tipoFiltro)
                .filter(u -> termoPesquisa.isEmpty() || 
                           u.getNome().toLowerCase().contains(termoPesquisa) ||
                           u.getEmail().toLowerCase().contains(termoPesquisa))
                .toList();
            
            usuariosTable.setItems(FXCollections.observableArrayList(usuariosFiltrados));
            
        } catch (Exception e) {
            System.err.println("Erro ao aplicar filtros: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao filtrar usuários: " + e.getMessage());
        }
    }
    
    private void atualizarTabela() {
        try {
            List<Usuario> usuarios = usuarioService.listarTodos();
            usuariosTable.setItems(FXCollections.observableArrayList(usuarios));
        } catch (Exception e) {
            System.err.println("Erro ao atualizar tabela: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao carregar usuários: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleNovoUsuario() {
        try {
            carregarTela("fxml/UsuarioForm.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao abrir formulário: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir formulário de usuário: " + e.getMessage());
        }
    }
    
    private void editarUsuario(Usuario usuario) {
        try {
            carregarTela("fxml/UsuarioForm.fxml", usuario);
        } catch (Exception e) {
            System.err.println("Erro ao editar usuário: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao abrir formulário de edição: " + e.getMessage());
        }
    }
    
    private void excluirUsuario(Usuario usuario) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Exclusão");
            alert.setHeaderText("Excluir Usuário");
            alert.setContentText("Tem certeza que deseja excluir o usuário " + usuario.getNome() + "?");
            
            if (alert.showAndWait().orElse(null) == ButtonType.OK) {
                usuarioService.excluirUsuario(usuario.getId());
                atualizarTabela();
            }
        } catch (Exception e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao excluir usuário: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVoltar() {
        try {
            carregarTela("/fxml/AdminDashboardView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
            mostrarErro("Erro ao voltar para dashboard: " + e.getMessage());
        }
    }
} 