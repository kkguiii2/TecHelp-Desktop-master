package com.techelp.controller;

import com.techelp.model.entity.Usuario;
import com.techelp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.collections.FXCollections;
import javafx.scene.control.Label;

public class UsuarioFormController extends BaseController implements DadosAware {
    
    private final UsuarioService usuarioService;
    private Usuario usuarioEdicao;
    
    @FXML
    private TextField nomeField;
    
    @FXML
    private TextField emailField;
    
    @FXML
    private PasswordField senhaField;
    
    @FXML
    private PasswordField confirmaSenhaField;
    
    @FXML
    private TextField telefoneField;
    
    @FXML
    private ComboBox<Usuario.TipoUsuario> tipoUsuarioCombo;
    
    @FXML
    private Label mensagemLabel;
    
    public UsuarioFormController() {
        this.usuarioService = new UsuarioService();
    }
    
    @FXML
    public void initialize() {
        System.out.println("Inicializando UsuarioFormController");
        
        // Configura o ComboBox de tipo de usuário
        tipoUsuarioCombo.setItems(FXCollections.observableArrayList(Usuario.TipoUsuario.values()));
        tipoUsuarioCombo.setValue(Usuario.TipoUsuario.SOLICITANTE);
    }
    
    @Override
    public void setDados(Object dados) {
        if (dados instanceof Usuario) {
            this.usuarioEdicao = (Usuario) dados;
            preencherCampos();
        }
    }
    
    private void preencherCampos() {
        if (usuarioEdicao != null) {
            nomeField.setText(usuarioEdicao.getNome());
            emailField.setText(usuarioEdicao.getEmail());
            telefoneField.setText(usuarioEdicao.getTelefone());
            tipoUsuarioCombo.setValue(usuarioEdicao.getTipo());
            
            // Em modo de edição, os campos de senha são opcionais
            senhaField.setPromptText("Digite para alterar a senha");
            confirmaSenhaField.setPromptText("Confirme a nova senha");
        }
    }
    
    @FXML
    private void handleSalvar() {
        try {
            // Validação dos campos
            if (nomeField.getText().isEmpty() || emailField.getText().isEmpty() || telefoneField.getText().isEmpty() || tipoUsuarioCombo.getValue() == null) {
                mensagemLabel.setText("Preencha todos os campos obrigatórios");
                return;
            }
            
            if (usuarioEdicao == null && senhaField.getText().isEmpty()) {
                mensagemLabel.setText("Senha é obrigatória para novos usuários");
                return;
            }
            
            if (!senhaField.getText().isEmpty() && !senhaField.getText().equals(confirmaSenhaField.getText())) {
                mensagemLabel.setText("As senhas não coincidem");
                return;
            }
            
            // Cria ou atualiza o usuário
            Usuario usuario = usuarioEdicao != null ? usuarioEdicao : new Usuario();
            usuario.setNome(nomeField.getText());
            usuario.setEmail(emailField.getText());
            usuario.setTelefone(telefoneField.getText());
            usuario.setTipo(tipoUsuarioCombo.getValue());
            
            if (!senhaField.getText().isEmpty()) {
                usuario.setSenha(senhaField.getText());
            }
            
            if (usuarioEdicao == null) {
                usuarioService.salvar(usuario);
                mensagemLabel.setText("Usuário cadastrado com sucesso!");
            } else {
                usuarioService.atualizar(usuario);
                mensagemLabel.setText("Usuário atualizado com sucesso!");
            }
            
            carregarTela("/fxml/UsuariosView.fxml");
            
        } catch (Exception e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
            e.printStackTrace();
            mensagemLabel.setText("Erro ao salvar usuário: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        try {
            carregarTela("/fxml/UsuariosView.fxml");
        } catch (Exception e) {
            System.err.println("Erro ao voltar: " + e.getMessage());
            e.printStackTrace();
            mensagemLabel.setText("Erro ao voltar: " + e.getMessage());
        }
    }
} 