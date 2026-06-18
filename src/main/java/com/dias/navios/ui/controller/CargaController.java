package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Carga;
import com.dias.navios.model.Porto;
import com.dias.navios.model.TipoCarga;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class CargaController {

    @FXML private TableView<Carga> tabelaCargas;
    @FXML private TableColumn<Carga, String> colDesignacao;
    @FXML private TableColumn<Carga, String> colTipo;
    @FXML private TableColumn<Carga, String> colVolume;
    @FXML private TableColumn<Carga, String> colPeso;

    @FXML private TextField campoDesignacao;
    @FXML private ComboBox<TipoCarga> comboTipo;
    @FXML private TextField campoVolume;
    @FXML private TextField campoPeso;
    @FXML private CheckBox checkInflamavel;
    @FXML private CheckBox checkCorrosiva;
    @FXML private CheckBox checkToxica;
    @FXML private ComboBox<Porto> comboPortoCarregamento;
    @FXML private ComboBox<Porto> comboPortoDescarga;
    @FXML private TextField campoPesquisa;
    @FXML private Label labelMensagem;

    private CargaService cargaService = new CargaService();
    private PortoService portoService = new PortoService();
    private Carga cargaSelecionada = null;

    @FXML
    public void initialize() {
        colDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colVolume.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVolume() + " m³"));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPeso() + " ton"));

        comboTipo.setItems(FXCollections.observableArrayList(TipoCarga.values()));

        tabelaCargas.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) carregarNoFormulario(novo);
        });

        carregarPortos();
        carregarTabela();
    }

    private void carregarPortos() {
        try {
            List<Porto> portos = portoService.listarPortos();
            ObservableList<Porto> obs = FXCollections.observableArrayList(portos);
            comboPortoCarregamento.setItems(obs);
            comboPortoDescarga.setItems(FXCollections.observableArrayList(portos));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar portos: " + e.getMessage());
        }
    }

    private void carregarTabela() {
        try {
            List<Carga> cargas = cargaService.listarCargas();
            String filtro = campoPesquisa != null ? campoPesquisa.getText().toLowerCase() : "";
            if (!filtro.isBlank()) {
                cargas.removeIf(c -> !c.getDesignacao().toLowerCase().contains(filtro));
            }
            tabelaCargas.setItems(FXCollections.observableArrayList(cargas));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar cargas: " + e.getMessage());
        }
    }

    private void carregarNoFormulario(Carga carga) {
        cargaSelecionada = carga;
        campoDesignacao.setText(carga.getDesignacao());
        comboTipo.setValue(carga.getTipo());
        campoVolume.setText(String.valueOf(carga.getVolume()));
        campoPeso.setText(String.valueOf(carga.getPeso()));
        checkInflamavel.setSelected(carga.isInflamavel());
        checkCorrosiva.setSelected(carga.isCorrosiva());
        checkToxica.setSelected(carga.isToxica());

        comboPortoCarregamento.getItems().stream()
            .filter(p -> p.getId() == carga.getPortoCarregamentoId())
            .findFirst().ifPresent(comboPortoCarregamento::setValue);
        comboPortoDescarga.getItems().stream()
            .filter(p -> p.getId() == carga.getPortoDescargaId())
            .findFirst().ifPresent(comboPortoDescarga::setValue);

        labelMensagem.setText("Carga carregada para edição.");
    }

    @FXML
    public void guardarCarga() {
        try {
            Carga carga = cargaSelecionada != null ? cargaSelecionada : new Carga();
            carga.setDesignacao(campoDesignacao.getText());
            carga.setTipo(comboTipo.getValue());
            carga.setVolume(Double.parseDouble(campoVolume.getText()));
            carga.setPeso(Double.parseDouble(campoPeso.getText()));
            carga.setInflamavel(checkInflamavel.isSelected());
            carga.setCorrosiva(checkCorrosiva.isSelected());
            carga.setToxica(checkToxica.isSelected());
            carga.setPortoCarregamentoId(comboPortoCarregamento.getValue() != null ? comboPortoCarregamento.getValue().getId() : 0);
            carga.setPortoDescargaId(comboPortoDescarga.getValue() != null ? comboPortoDescarga.getValue().getId() : 0);

            if (cargaSelecionada != null) {
                cargaService.editarCarga(carga);
                labelMensagem.setText("Carga actualizada com sucesso.");
            } else {
                cargaService.registarCarga(carga);
                labelMensagem.setText("Carga registada com sucesso.");
            }
            cargaSelecionada = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            labelMensagem.setText("Validação/Formato: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarCarga() {
        Carga selecionada = tabelaCargas.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Seleccione uma carga para apagar.");
            return;
        }
        try {
            cargaService.apagarCarga(selecionada.getId());
            labelMensagem.setText("Carga apagada.");
            cargaSelecionada = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalStateException e) {
            labelMensagem.setText("Regra de negócio: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void pesquisar() {
        carregarTabela();
    }

    @FXML
    public void novaCarga() {
        cargaSelecionada = null;
        limparFormulario();
        labelMensagem.setText("Formulário pronto para nova carga.");
    }

    private void limparFormulario() {
        campoDesignacao.clear();
        campoVolume.clear();
        campoPeso.clear();
        comboTipo.setValue(null);
        checkInflamavel.setSelected(false);
        checkCorrosiva.setSelected(false);
        checkToxica.setSelected(false);
        comboPortoCarregamento.setValue(null);
        comboPortoDescarga.setValue(null);
        tabelaCargas.getSelectionModel().clearSelection();
    }
}
