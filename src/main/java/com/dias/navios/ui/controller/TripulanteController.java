package com.dias.navios.ui.controller;

import com.dias.navios.bll.TripulanteService;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;
import com.dias.navios.ui.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class TripulanteController {

    @FXML private TableView<Tripulante> tabela;
    @FXML private TableColumn<Tripulante, String> colNome;
    @FXML private TableColumn<Tripulante, String> colCertificado;
    @FXML private TableColumn<Tripulante, String> colFuncao;
    @FXML private TableColumn<Tripulante, String> colEstado;
    @FXML private TableColumn<Tripulante, String> colEmail;

    @FXML private TextField campoNome;
    @FXML private TextField campoCertificado;
    @FXML private ComboBox<FuncaoTripulante> comboFuncao;
    @FXML private TextField campoEmail;
    @FXML private DatePicker campoDataNascimento;
    @FXML private Label labelMensagem;

    private final TripulanteService tripulanteService = new TripulanteService();
    private Tripulante selecionado;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colFuncao.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getFuncao())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoDisponibilidade()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        comboFuncao.setItems(FXCollections.observableArrayList(FuncaoTripulante.values()));
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar...");
        Thread t = new Thread(() -> {
            try {
                List<Tripulante> tripulantes = tripulanteService.listarTripulantes();
                Platform.runLater(() -> {
                    tabela.setItems(FXCollections.observableArrayList(tripulantes));
                    labelMensagem.setText("");
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro ao carregar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void novo() {
        tabela.getSelectionModel().clearSelection();
        selecionado = null;
        limpar();
        labelMensagem.setText("A registar um novo tripulante.");
    }

    @FXML
    public void guardar() {
        try {
            if (comboFuncao.getValue() == null)
                throw new IllegalArgumentException("Selecione a função do tripulante.");
            if (campoEmail.getText().isBlank())
                throw new IllegalArgumentException("O email é obrigatório.");
            if (campoDataNascimento.getValue() == null)
                throw new IllegalArgumentException("A data de nascimento é obrigatória.");

            final Tripulante t = (selecionado == null) ? new Tripulante() : selecionado;
            t.setNome(campoNome.getText());
            t.setNumeroCertificado(campoCertificado.getText());
            t.setFuncao(comboFuncao.getValue());
            t.setEmail(campoEmail.getText());
            t.setDataNascimento(campoDataNascimento.getValue());
            if (t.getEstadoDisponibilidade() == null) {
                t.setEstadoDisponibilidade("DISPONIVEL");
            }

            final boolean isNovo = (selecionado == null);
            Thread th = new Thread(() -> {
                try {
                    if (isNovo) tripulanteService.registarTripulante(t);
                    else        tripulanteService.editarTripulante(t);
                    Platform.runLater(() -> {
                        Dialogs.info(isNovo ? "Tripulante criado com sucesso." : "Tripulante atualizado com sucesso.");
                        novo();
                        recarregar();
                    });
                } catch (IllegalArgumentException | IllegalStateException e) {
                    Platform.runLater(() -> Dialogs.erro(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Dialogs.erro("Erro ao guardar: " + e.getMessage()));
                }
            });
            th.setDaemon(true);
            th.start();
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        }
    }

    @FXML
    public void apagar() {
        Tripulante sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione um tripulante para apagar."); return; }
        if (!Dialogs.confirmar("Apagar o tripulante \"" + sel.getNome() + "\"?")) return;

        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                tripulanteService.apagarTripulante(id);
                Platform.runLater(() -> {
                    Dialogs.info("Tripulante apagado.");
                    novo();
                    recarregar();
                });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao apagar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void preencher(Tripulante t) {
        selecionado = t;
        if (t == null) return;
        campoNome.setText(t.getNome());
        campoCertificado.setText(t.getNumeroCertificado());
        comboFuncao.setValue(t.getFuncao());
        campoEmail.setText(t.getEmail());
        campoDataNascimento.setValue(t.getDataNascimento());
        labelMensagem.setText("A editar: " + t.getNome() + " [" + t.getEstadoDisponibilidade() + "]");
    }

    private void limpar() {
        campoNome.clear();
        campoCertificado.clear();
        comboFuncao.setValue(null);
        campoEmail.clear();
        campoDataNascimento.setValue(null);
    }

    private String texto(Object o) { return o == null ? "" : o.toString(); }
}
