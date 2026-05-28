package com.dias.navios.ui.controller;

import com.dias.navios.bll.TripulanteService;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class TripulanteController {

    @FXML private TableView<Tripulante> tabelaTripulantes;
    @FXML private TextField campoNome;
    @FXML private TextField campoCertificado;
    @FXML private ComboBox<FuncaoTripulante> comboFuncao;
    @FXML private CheckBox checkDisponivel;
    @FXML private TextField campoNacionalidade;
    @FXML private Label labelMensagem;

    private TripulanteService tripulanteService = new TripulanteService();

    @FXML
    public void initialize() {
        comboFuncao.setItems(FXCollections.observableArrayList(FuncaoTripulante.values()));
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Tripulante> tripulantes = tripulanteService.listarTripulantes();
            tabelaTripulantes.setItems(FXCollections.observableArrayList(tripulantes));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar tripulantes: " + e.getMessage());
        }
    }

    @FXML
    public void guardarTripulante() {
        // TODO: ler os campos e chamar tripulanteService.registarTripulante()
        try {
            Tripulante t = new Tripulante();
            t.setNome(campoNome.getText());
            t.setNumeroCertificado(campoCertificado.getText());
            t.setFuncao(comboFuncao.getValue());
            t.setDisponivel(checkDisponivel.isSelected());
            t.setNacionalidade(campoNacionalidade.getText());

            tripulanteService.registarTripulante(t);
            labelMensagem.setText("Tripulante guardado com sucesso.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarTripulante() {
        // TODO: obter tripulante selecionado e chamar tripulanteService.apagarTripulante()
        Tripulante selecionado = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            labelMensagem.setText("Selecione um tripulante para apagar.");
            return;
        }
        try {
            tripulanteService.apagarTripulante(selecionado.getId());
            labelMensagem.setText("Tripulante apagado.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }
}
