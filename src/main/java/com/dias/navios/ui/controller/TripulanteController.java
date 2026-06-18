package com.dias.navios.ui.controller;

import com.dias.navios.bll.TripulanteService;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;
import com.dias.navios.model.Viagem;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class TripulanteController {

    @FXML private TableView<Tripulante> tabelaTripulantes;
    @FXML private TableColumn<Tripulante, String> colNome;
    @FXML private TableColumn<Tripulante, String> colFuncao;
    @FXML private TableColumn<Tripulante, String> colCertificado;
    @FXML private TableColumn<Tripulante, String> colDisponivel;
    @FXML private TableColumn<Tripulante, String> colNacionalidade;

    @FXML private TableView<Viagem> tabelaHistorico;
    @FXML private TableColumn<Viagem, String> colHistViagem;
    @FXML private TableColumn<Viagem, String> colHistEstado;
    @FXML private TableColumn<Viagem, String> colHistPartida;

    @FXML private TextField campoNome;
    @FXML private TextField campoCertificado;
    @FXML private ComboBox<FuncaoTripulante> comboFuncao;
    @FXML private CheckBox checkDisponivel;
    @FXML private TextField campoNacionalidade;
    @FXML private TextField campoPesquisa;
    @FXML private Label labelMensagem;

    private TripulanteService tripulanteService = new TripulanteService();
    private Tripulante tripulanteSelecionado = null;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colFuncao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFuncao().name()));
        colCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colDisponivel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponivel() ? "Sim" : "Não"));
        colNacionalidade.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNacionalidade() != null ? c.getValue().getNacionalidade() : ""));

        colHistViagem.setCellValueFactory(c -> new SimpleStringProperty("Viagem #" + c.getValue().getId()));
        colHistEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colHistPartida.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataPartida() != null ? c.getValue().getDataPartida().toString() : ""));

        comboFuncao.setItems(FXCollections.observableArrayList(FuncaoTripulante.values()));

        tabelaTripulantes.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) carregarNoFormulario(novo);
        });

        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Tripulante> tripulantes = tripulanteService.listarTripulantes();
            String filtro = campoPesquisa != null ? campoPesquisa.getText().toLowerCase() : "";
            if (!filtro.isBlank()) {
                tripulantes.removeIf(t -> !t.getNome().toLowerCase().contains(filtro)
                        && !t.getFuncao().name().toLowerCase().contains(filtro));
            }
            tabelaTripulantes.setItems(FXCollections.observableArrayList(tripulantes));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar tripulantes: " + e.getMessage());
        }
    }

    private void carregarNoFormulario(Tripulante t) {
        tripulanteSelecionado = t;
        campoNome.setText(t.getNome());
        campoCertificado.setText(t.getNumeroCertificado());
        comboFuncao.setValue(t.getFuncao());
        checkDisponivel.setSelected(t.isDisponivel());
        campoNacionalidade.setText(t.getNacionalidade() != null ? t.getNacionalidade() : "");
        carregarHistorico(t.getId());
        labelMensagem.setText("Tripulante carregado para edição.");
    }

    private void carregarHistorico(int tripulanteId) {
        try {
            List<Viagem> historico = tripulanteService.listarHistoricoTripulante(tripulanteId);
            tabelaHistorico.setItems(FXCollections.observableArrayList(historico));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar histórico: " + e.getMessage());
        }
    }

    @FXML
    public void guardarTripulante() {
        try {
            Tripulante t = tripulanteSelecionado != null ? tripulanteSelecionado : new Tripulante();
            t.setNome(campoNome.getText());
            t.setNumeroCertificado(campoCertificado.getText());
            t.setFuncao(comboFuncao.getValue());
            t.setDisponivel(checkDisponivel.isSelected());
            t.setNacionalidade(campoNacionalidade.getText());

            if (tripulanteSelecionado != null) {
                tripulanteService.editarTripulante(t);
                labelMensagem.setText("Tripulante actualizado com sucesso.");
            } else {
                tripulanteService.registarTripulante(t);
                labelMensagem.setText("Tripulante registado com sucesso.");
            }
            tripulanteSelecionado = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            labelMensagem.setText("Validação: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarTripulante() {
        Tripulante selecionado = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            labelMensagem.setText("Seleccione um tripulante para apagar.");
            return;
        }
        try {
            tripulanteService.apagarTripulante(selecionado.getId());
            labelMensagem.setText("Tripulante apagado.");
            tripulanteSelecionado = null;
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
    public void novoTripulante() {
        tripulanteSelecionado = null;
        limparFormulario();
        tabelaHistorico.setItems(FXCollections.emptyObservableList());
        labelMensagem.setText("Formulário pronto para novo tripulante.");
    }

    private void limparFormulario() {
        campoNome.clear();
        campoCertificado.clear();
        campoNacionalidade.clear();
        comboFuncao.setValue(null);
        checkDisponivel.setSelected(true);
        tabelaTripulantes.getSelectionModel().clearSelection();
    }
}
