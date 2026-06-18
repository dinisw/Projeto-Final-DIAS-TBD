package com.dias.navios.ui.controller;

import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Porto;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PortoController {

    @FXML private TableView<Porto> tabelaPortos;
    @FXML private TableColumn<Porto, String> colNome;
    @FXML private TableColumn<Porto, String> colPais;
    @FXML private TableColumn<Porto, String> colCodigo;

    @FXML private TextField campoNome;
    @FXML private TextField campoPais;
    @FXML private TextField campoCodigo;
    @FXML private TextField campoPesquisa;
    @FXML private Label labelMensagem;

    private PortoService portoService = new PortoService();
    private Porto portoSelecionado = null;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colPais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPais()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

        tabelaPortos.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) carregarNoFormulario(novo);
        });

        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Porto> portos = portoService.listarPortos();
            String filtro = campoPesquisa != null ? campoPesquisa.getText().toLowerCase() : "";
            if (!filtro.isBlank()) {
                portos.removeIf(p -> !p.getNome().toLowerCase().contains(filtro)
                        && !p.getCodigo().toLowerCase().contains(filtro)
                        && !p.getPais().toLowerCase().contains(filtro));
            }
            tabelaPortos.setItems(FXCollections.observableArrayList(portos));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar portos: " + e.getMessage());
        }
    }

    private void carregarNoFormulario(Porto porto) {
        portoSelecionado = porto;
        campoNome.setText(porto.getNome());
        campoPais.setText(porto.getPais());
        campoCodigo.setText(porto.getCodigo());
        labelMensagem.setText("Porto carregado para edição.");
    }

    @FXML
    public void guardarPorto() {
        try {
            Porto porto = portoSelecionado != null ? portoSelecionado : new Porto();
            porto.setNome(campoNome.getText());
            porto.setPais(campoPais.getText());
            porto.setCodigo(campoCodigo.getText());

            if (portoSelecionado != null) {
                portoService.editarPorto(porto);
                labelMensagem.setText("Porto actualizado com sucesso.");
            } else {
                portoService.registarPorto(porto);
                labelMensagem.setText("Porto registado com sucesso.");
            }
            portoSelecionado = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            labelMensagem.setText("Validação: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarPorto() {
        Porto selecionado = tabelaPortos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            labelMensagem.setText("Seleccione um porto para apagar.");
            return;
        }
        try {
            portoService.apagarPorto(selecionado.getId());
            labelMensagem.setText("Porto apagado.");
            portoSelecionado = null;
            limparFormulario();
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void pesquisar() {
        carregarTabela();
    }

    @FXML
    public void novoPorto() {
        portoSelecionado = null;
        limparFormulario();
        labelMensagem.setText("Formulário pronto para novo porto.");
    }

    private void limparFormulario() {
        campoNome.clear();
        campoPais.clear();
        campoCodigo.clear();
        tabelaPortos.getSelectionModel().clearSelection();
    }
}
