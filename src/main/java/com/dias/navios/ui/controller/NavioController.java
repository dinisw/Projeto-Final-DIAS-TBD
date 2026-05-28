package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class NavioController {

    @FXML private TableView<Navio> tabelaNavios;
    @FXML private TableColumn<Navio, String> colNome;
    @FXML private TableColumn<Navio, String> colIMO;
    @FXML private TableColumn<Navio, String> colTipo;
    @FXML private TableColumn<Navio, String> colEstado;

    @FXML private TextField campoNome;
    @FXML private TextField campoIMO;
    @FXML private ComboBox<TipoNavio> comboTipo;
    @FXML private TextField campoCapacidade;
    @FXML private TextField campoTanques;
    @FXML private TextField campoBandeira;
    @FXML private TextField campoAno;
    @FXML private ComboBox<EstadoNavio> comboEstado;
    @FXML private Label labelMensagem;

    private NavioService navioService = new NavioService();

    @FXML
    public void initialize() {
        // TODO: configurar as colunas da tabela com PropertyValueFactory
        comboTipo.setItems(FXCollections.observableArrayList(TipoNavio.values()));
        comboEstado.setItems(FXCollections.observableArrayList(EstadoNavio.values()));
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Navio> navios = navioService.listarNavios();
            tabelaNavios.setItems(FXCollections.observableArrayList(navios));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar navios: " + e.getMessage());
        }
    }

    @FXML
    public void guardarNavio() {
        // TODO: ler os campos do formulario, criar Navio e chamar navioService.registarNavio()
        try {
            Navio navio = new Navio();
            navio.setNome(campoNome.getText());
            navio.setCodigoIMO(campoIMO.getText());
            navio.setTipo(comboTipo.getValue());
            navio.setCapacidadeMaxima(Double.parseDouble(campoCapacidade.getText()));
            navio.setNumTanques(Integer.parseInt(campoTanques.getText()));
            navio.setBandeira(campoBandeira.getText());
            navio.setAnoFabrico(Integer.parseInt(campoAno.getText()));
            navio.setEstado(comboEstado.getValue());

            navioService.registarNavio(navio);
            labelMensagem.setText("Navio guardado com sucesso.");
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            labelMensagem.setText("Erro de validacao: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarNavio() {
        // TODO: obter o navio selecionado na tabela e chamar navioService.apagarNavio()
        Navio selecionado = tabelaNavios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            labelMensagem.setText("Selecione um navio para apagar.");
            return;
        }
        try {
            navioService.apagarNavio(selecionado.getId());
            labelMensagem.setText("Navio apagado.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    private void limparFormulario() {
        campoNome.clear();
        campoIMO.clear();
        campoCapacidade.clear();
        campoTanques.clear();
        campoBandeira.clear();
        campoAno.clear();
        comboTipo.setValue(null);
        comboEstado.setValue(null);
    }
}
