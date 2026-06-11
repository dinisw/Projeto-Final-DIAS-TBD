package com.dias.navios.ui.controller;

import com.dias.navios.bll.TripulanteService;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;
import com.dias.navios.ui.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class TripulanteController {

    @FXML private TableView<Tripulante> tabela;
    @FXML private TableColumn<Tripulante, String> colNome;
    @FXML private TableColumn<Tripulante, String> colCertificado;
    @FXML private TableColumn<Tripulante, String> colFuncao;
    @FXML private TableColumn<Tripulante, String> colDisponivel;
    @FXML private TableColumn<Tripulante, String> colNacionalidade;

    @FXML private TextField campoNome;
    @FXML private TextField campoCertificado;
    @FXML private ComboBox<FuncaoTripulante> comboFuncao;
    @FXML private CheckBox checkDisponivel;
    @FXML private TextField campoNacionalidade;
    @FXML private Label labelMensagem;

    private final TripulanteService tripulanteService = new TripulanteService();
    private Tripulante selecionado;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colFuncao.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getFuncao())));
        colDisponivel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponivel() ? "Sim" : "Nao"));
        colNacionalidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNacionalidade()));

        comboFuncao.setItems(FXCollections.observableArrayList(FuncaoTripulante.values()));
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        try {
            tabela.setItems(FXCollections.observableArrayList(tripulanteService.listarTripulantes()));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar dados: " + e.getMessage());
        }
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
            if (comboFuncao.getValue() == null) throw new IllegalArgumentException("Selecione a funcao do tripulante.");

            Tripulante t = (selecionado == null) ? new Tripulante() : selecionado;
            t.setNome(campoNome.getText());
            t.setNumeroCertificado(campoCertificado.getText());
            t.setFuncao(comboFuncao.getValue());
            t.setDisponivel(checkDisponivel.isSelected());
            t.setNacionalidade(campoNacionalidade.getText());

            if (selecionado == null) {
                tripulanteService.registarTripulante(t);
                Dialogs.info("Tripulante criado com sucesso.");
            } else {
                tripulanteService.editarTripulante(t);
                Dialogs.info("Tripulante atualizado com sucesso.");
            }
            novo();
            recarregar();
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        } catch (Exception e) {
            Dialogs.erro("Erro ao guardar: " + e.getMessage());
        }
    }

    @FXML
    public void apagar() {
        Tripulante sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione um tripulante para apagar."); return; }
        if (!Dialogs.confirmar("Apagar o tripulante \"" + sel.getNome() + "\"?")) return;
        try {
            tripulanteService.apagarTripulante(sel.getId());
            Dialogs.info("Tripulante apagado.");
            novo();
            recarregar();
        } catch (Exception e) {
            Dialogs.erro("Erro ao apagar: " + e.getMessage());
        }
    }

    private void preencher(Tripulante t) {
        selecionado = t;
        if (t == null) return;
        campoNome.setText(t.getNome());
        campoCertificado.setText(t.getNumeroCertificado());
        comboFuncao.setValue(t.getFuncao());
        checkDisponivel.setSelected(t.isDisponivel());
        campoNacionalidade.setText(t.getNacionalidade());
        labelMensagem.setText("A editar: " + t.getNome());
    }

    private void limpar() {
        campoNome.clear();
        campoCertificado.clear();
        comboFuncao.setValue(null);
        checkDisponivel.setSelected(false);
        campoNacionalidade.clear();
    }

    private String texto(Object o) { return o == null ? "" : o.toString(); }
}
