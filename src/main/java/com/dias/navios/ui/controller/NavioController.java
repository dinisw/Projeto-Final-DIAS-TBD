package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Porto;
import com.dias.navios.ui.Dialogs;
import com.dias.navios.ui.FormDialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavioController {

    @FXML private TableView<Navio>            tabela;
    @FXML private TableColumn<Navio, String>  colNome;
    @FXML private TableColumn<Navio, String>  colIMO;
    @FXML private TableColumn<Navio, String>  colTipo;
    @FXML private TableColumn<Navio, String>  colCapacidade;
    @FXML private TableColumn<Navio, String>  colEstado;
    @FXML private TableColumn<Navio, String>  colPorto;
    @FXML private Button                      btnEditar;
    @FXML private Button                      btnApagar;
    @FXML private Label                       labelMensagem;

    private final NavioService  navioService  = new NavioService();
    private final PortoService  portoService  = new PortoService();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();
    private List<Porto> portosCache = new ArrayList<>();

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colIMO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoIMO()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getTipo())));
        colCapacidade.setCellValueFactory(c -> new SimpleStringProperty(fmtDouble(c.getValue().getCapacidadeMaxima()) + " t"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getEstado())));
        colPorto.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoAtualId())));

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            boolean sel = (novo != null);
            btnEditar.setDisable(!sel);
            btnApagar.setDisable(!sel);
        });

        tabela.setRowFactory(tv -> {
            TableRow<Navio> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) editar(); });
            return row;
        });

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar…");
        Thread t = new Thread(() -> {
            try {
                List<Porto> portos = portoService.listarPortos();
                List<Navio> navios = navioService.listarNavios();
                Platform.runLater(() -> {
                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    portosCache = portos;
                    tabela.setItems(FXCollections.observableArrayList(navios));
                    labelMensagem.setText(navios.size() + " navio(s)");
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
        FormDialogs.mostrarNavio(null, portosCache).ifPresent(navio -> guardar(navio, true));
    }

    @FXML
    public void editar() {
        Navio sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FormDialogs.mostrarNavio(sel, portosCache).ifPresent(navio -> {
            navio.setId(sel.getId());
            guardar(navio, false);
        });
    }

    @FXML
    public void apagar() {
        Navio sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!Dialogs.confirmar("Apagar o navio \"" + sel.getNome() + "\"?")) return;
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                navioService.apagarNavio(id);
                Platform.runLater(() -> { labelMensagem.setText("Navio apagado."); recarregar(); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao apagar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void guardar(Navio navio, boolean isNovo) {
        Thread t = new Thread(() -> {
            try {
                if (isNovo) navioService.registarNavio(navio);
                else        navioService.editarNavio(navio);
                Platform.runLater(() -> {
                    labelMensagem.setText(isNovo ? "Navio criado." : "Navio atualizado.");
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

    private String str(Object o)         { return o == null ? "" : o.toString(); }
    private String nomePorto(int id)     { Porto p = portosPorId.get(id); return p == null ? "-" : p.getNome(); }
    private String fmtDouble(double v)   { return v == (long) v ? String.valueOf((long) v) : String.valueOf(v); }
}
