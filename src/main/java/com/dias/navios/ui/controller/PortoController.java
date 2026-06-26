package com.dias.navios.ui.controller;

import atlantafx.base.theme.Styles;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Porto;
import com.dias.navios.ui.Dialogs;
import com.dias.navios.ui.FormDialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class PortoController {

    @FXML private TableView<Porto>            tabela;
    @FXML private TableColumn<Porto, String>  colNome;
    @FXML private TableColumn<Porto, String>  colPais;
    @FXML private TableColumn<Porto, String>  colCodigo;
    @FXML private Button                      btnEditar;
    @FXML private Button                      btnApagar;
    @FXML private Label                       labelMensagem;

    private final PortoService portoService = new PortoService();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colPais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPais()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            boolean sel = (novo != null);
            btnEditar.setDisable(!sel);
            btnApagar.setDisable(!sel);
        });

        tabela.setRowFactory(tv -> {
            TableRow<Porto> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) editar(); });
            return row;
        });

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar…");
        Thread t = new Thread(() -> {
            try {
                List<Porto> lista = portoService.listarPortos();
                Platform.runLater(() -> {
                    tabela.setItems(FXCollections.observableArrayList(lista));
                    labelMensagem.setText(lista.size() + " porto(s)");
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
        FormDialogs.mostrarPorto(null).ifPresent(porto -> guardar(porto, true));
    }

    @FXML
    public void editar() {
        Porto sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FormDialogs.mostrarPorto(sel).ifPresent(porto -> {
            porto.setId(sel.getId());
            guardar(porto, false);
        });
    }

    @FXML
    public void apagar() {
        Porto sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!Dialogs.confirmar("Apagar o porto \"" + sel.getNome() + "\"?")) return;
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                portoService.apagarPorto(id);
                Platform.runLater(() -> { msg("Porto apagado.", true); recarregar(); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao apagar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void guardar(Porto porto, boolean isNovo) {
        Thread t = new Thread(() -> {
            try {
                if (isNovo) portoService.registarPorto(porto);
                else        portoService.editarPorto(porto);
                Platform.runLater(() -> {
                    msg(isNovo ? "Porto criado." : "Porto atualizado.", true);
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
    }

    private void msg(String texto, boolean sucesso) {
        labelMensagem.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER, Styles.WARNING);
        labelMensagem.setText(texto);
        if (sucesso) labelMensagem.getStyleClass().add(Styles.SUCCESS);
        else         labelMensagem.getStyleClass().add(Styles.DANGER);
    }
}
