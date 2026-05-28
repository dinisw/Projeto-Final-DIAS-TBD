package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class CargaController {

    @FXML private TableView<Carga> tabelaCargas;
    @FXML private TextField campoDesignacao;
    @FXML private ComboBox<TipoCarga> comboTipo;
    @FXML private TextField campoVolume;
    @FXML private TextField campoPeso;
    @FXML private CheckBox checkInflamavel;
    @FXML private CheckBox checkCorrosiva;
    @FXML private CheckBox checkToxica;
    @FXML private Label labelMensagem;

    private CargaService cargaService = new CargaService();

    @FXML
    public void initialize() {
        comboTipo.setItems(FXCollections.observableArrayList(TipoCarga.values()));
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Carga> cargas = cargaService.listarCargas();
            tabelaCargas.setItems(FXCollections.observableArrayList(cargas));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar cargas: " + e.getMessage());
        }
    }

    @FXML
    public void guardarCarga() {
        // TODO: ler os campos, criar Carga e chamar cargaService.registarCarga()
        try {
            Carga carga = new Carga();
            carga.setDesignacao(campoDesignacao.getText());
            carga.setTipo(comboTipo.getValue());
            carga.setVolume(Double.parseDouble(campoVolume.getText()));
            carga.setPeso(Double.parseDouble(campoPeso.getText()));
            carga.setInflamavel(checkInflamavel.isSelected());
            carga.setCorrosiva(checkCorrosiva.isSelected());
            carga.setToxica(checkToxica.isSelected());

            cargaService.registarCarga(carga);
            labelMensagem.setText("Carga guardada com sucesso.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarCarga() {
        // TODO: obter carga selecionada e chamar cargaService.apagarCarga()
        Carga selecionada = tabelaCargas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Selecione uma carga para apagar.");
            return;
        }
        try {
            cargaService.apagarCarga(selecionada.getId());
            labelMensagem.setText("Carga apagada.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }
}
