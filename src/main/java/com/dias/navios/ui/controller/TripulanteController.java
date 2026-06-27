package com.dias.navios.ui.controller;

import atlantafx.base.theme.Styles;
import com.dias.navios.bll.TripulanteService;
import com.dias.navios.model.Tripulante;
import com.dias.navios.ui.Dialogs;
import com.dias.navios.ui.FormDialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

public class TripulanteController {

    @FXML private TableView<Tripulante>            tabela;
    @FXML private TableColumn<Tripulante, String>  colNome;
    @FXML private TableColumn<Tripulante, String>  colCertificado;
    @FXML private TableColumn<Tripulante, String>  colFuncao;
    @FXML private TableColumn<Tripulante, String>  colEstado;
    @FXML private TableColumn<Tripulante, String>  colEmail;
    @FXML private TableColumn<Tripulante, String>  colNascimento;
    @FXML private Button                           btnEditar;
    @FXML private Button                           btnApagar;
    @FXML private Label                            labelMensagem;

    private final TripulanteService tripulanteService = new TripulanteService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colFuncao.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getFuncao())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoDisponibilidade()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colNascimento.setCellValueFactory(c -> {
            LocalDate d = c.getValue().getDataNascimento();
            return new SimpleStringProperty(d != null ? d.toString() : "");
        });

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            boolean sel = (novo != null);
            btnEditar.setDisable(!sel);
            btnApagar.setDisable(!sel);
        });

        tabela.setRowFactory(tv -> {
            TableRow<Tripulante> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) editar(); });
            return row;
        });

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar…");
        Thread t = new Thread(() -> {
            try {
                List<Tripulante> lista = tripulanteService.listarTripulantes();
                Platform.runLater(() -> {
                    tabela.setItems(FXCollections.observableArrayList(lista));
                    labelMensagem.setText(lista.size() + " tripulante(s)");
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void novo() {
        FormDialogs.mostrarTripulante(null).ifPresent(t -> guardar(t, true));
    }

    @FXML
    public void editar() {
        Tripulante sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FormDialogs.mostrarTripulante(sel).ifPresent(t -> {
            t.setId(sel.getId());
            guardar(t, false);
        });
    }

    @FXML
    public void apagar() {
        Tripulante sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!Dialogs.confirmar("Apagar o tripulante \"" + sel.getNome() + "\"?")) return;
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                tripulanteService.apagarTripulante(id);
                Platform.runLater(() -> { msg("Tripulante apagado.", true); recarregar(); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao apagar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void guardar(Tripulante tripulante, boolean isNovo) {
        Thread t = new Thread(() -> {
            try {
                if (isNovo) tripulanteService.registarTripulante(tripulante);
                else        tripulanteService.editarTripulante(tripulante);
                Platform.runLater(() -> {
                    msg(isNovo ? "Tripulante criado." : "Tripulante atualizado.", true);
                    recarregar();
                });
            } catch (IllegalArgumentException | IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao guardar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }

    private void msg(String texto, boolean sucesso) {
        labelMensagem.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER, Styles.WARNING);
        labelMensagem.setText(texto);
        if (sucesso) labelMensagem.getStyleClass().add(Styles.SUCCESS);
        else         labelMensagem.getStyleClass().add(Styles.DANGER);
    }
}
