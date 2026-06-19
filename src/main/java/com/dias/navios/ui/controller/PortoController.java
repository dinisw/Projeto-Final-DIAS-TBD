package com.dias.navios.ui.controller;

import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Porto;
import com.dias.navios.ui.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PortoController {

    @FXML private TableView<Porto> tabela;
    @FXML private TableColumn<Porto, String> colNome;
    @FXML private TableColumn<Porto, String> colPais;
    @FXML private TableColumn<Porto, String> colCodigo;

    @FXML private TextField campoNome;
    @FXML private TextField campoPais;
    @FXML private TextField campoCodigo;
    @FXML private Label labelMensagem;

    private final PortoService portoService = new PortoService();
    private Porto selecionado;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colPais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPais()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar...");
        Thread t = new Thread(() -> {
            try {
                List<Porto> portos = portoService.listarPortos();
                Platform.runLater(() -> {
                    tabela.setItems(FXCollections.observableArrayList(portos));
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
        labelMensagem.setText("A criar um novo porto.");
    }

    @FXML
    public void guardar() {
        try {
            final Porto porto = (selecionado == null) ? new Porto() : selecionado;
            porto.setNome(campoNome.getText());
            porto.setPais(campoPais.getText());
            porto.setCodigo(campoCodigo.getText());

            final boolean isNovo = (selecionado == null);
            Thread t = new Thread(() -> {
                try {
                    if (isNovo) portoService.registarPorto(porto);
                    else        portoService.editarPorto(porto);
                    Platform.runLater(() -> {
                        Dialogs.info(isNovo ? "Porto criado com sucesso." : "Porto atualizado com sucesso.");
                        novo();
                        recarregar();
                    });
                } catch (IllegalArgumentException e) {
                    Platform.runLater(() -> Dialogs.erro(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Dialogs.erro("Erro ao guardar: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        }
    }

    @FXML
    public void apagar() {
        Porto sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione um porto para apagar."); return; }
        if (!Dialogs.confirmar("Apagar o porto \"" + sel.getNome() + "\"?")) return;

        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                portoService.apagarPorto(id);
                Platform.runLater(() -> {
                    Dialogs.info("Porto apagado.");
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

    private void preencher(Porto p) {
        selecionado = p;
        if (p == null) return;
        campoNome.setText(p.getNome());
        campoPais.setText(p.getPais());
        campoCodigo.setText(p.getCodigo());
        labelMensagem.setText("A editar: " + p.getNome());
    }

    private void limpar() {
        campoNome.clear();
        campoPais.clear();
        campoCodigo.clear();
    }
}
