package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Carga;
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

public class CargaController {

    @FXML private TableView<Carga>            tabela;
    @FXML private TableColumn<Carga, String>  colDesignacao;
    @FXML private TableColumn<Carga, String>  colTipo;
    @FXML private TableColumn<Carga, String>  colVolume;
    @FXML private TableColumn<Carga, String>  colPeso;
    @FXML private TableColumn<Carga, String>  colInflamavel;
    @FXML private TableColumn<Carga, String>  colCarga;
    @FXML private TableColumn<Carga, String>  colDescarga;
    @FXML private Button                      btnEditar;
    @FXML private Button                      btnApagar;
    @FXML private Label                       labelMensagem;

    private final CargaService  cargaService  = new CargaService();
    private final PortoService  portoService  = new PortoService();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();
    private List<Porto> portosCache = new ArrayList<>();

    @FXML
    public void initialize() {
        colDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getTipo())));
        colVolume.setCellValueFactory(c -> new SimpleStringProperty(fmtDouble(c.getValue().getVolume()) + " m³"));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(fmtDouble(c.getValue().getPeso()) + " t"));
        colInflamavel.setCellValueFactory(c -> {
            Carga cg = c.getValue();
            String props = (cg.isInflamavel() ? "🔥 " : "") +
                           (cg.isCorrosiva()  ? "⚗ " : "") +
                           (cg.isToxica()     ? "☠ " : "");
            return new SimpleStringProperty(props.isBlank() ? "—" : props.trim());
        });
        colCarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoCarregamentoId())));
        colDescarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDescargaId())));

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            boolean sel = (novo != null);
            btnEditar.setDisable(!sel);
            btnApagar.setDisable(!sel);
        });

        tabela.setRowFactory(tv -> {
            TableRow<Carga> row = new TableRow<>();
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
                List<Carga> cargas = cargaService.listarCargas();
                Platform.runLater(() -> {
                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    portosCache = portos;
                    tabela.setItems(FXCollections.observableArrayList(cargas));
                    labelMensagem.setText(cargas.size() + " carga(s)");
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
        FormDialogs.mostrarCarga(null, portosCache).ifPresent(carga -> guardar(carga, true));
    }

    @FXML
    public void editar() {
        Carga sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FormDialogs.mostrarCarga(sel, portosCache).ifPresent(carga -> {
            carga.setId(sel.getId());
            guardar(carga, false);
        });
    }

    @FXML
    public void apagar() {
        Carga sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        if (!Dialogs.confirmar("Apagar a carga \"" + sel.getDesignacao() + "\"?")) return;
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                cargaService.apagarCarga(id);
                Platform.runLater(() -> { labelMensagem.setText("Carga apagada."); recarregar(); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao apagar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void guardar(Carga carga, boolean isNovo) {
        Thread t = new Thread(() -> {
            try {
                if (isNovo) cargaService.registarCarga(carga);
                else        cargaService.editarCarga(carga);
                Platform.runLater(() -> {
                    labelMensagem.setText(isNovo ? "Carga criada." : "Carga atualizada.");
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
