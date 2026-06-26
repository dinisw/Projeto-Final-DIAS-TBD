package com.dias.navios.ui.controller;

import atlantafx.base.theme.Styles;
import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.TripulanteService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.*;
import com.dias.navios.ui.Dialogs;
import com.dias.navios.ui.FormDialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViagemController {

    // ── tabela principal ──────────────────────────────────────────────────────
    @FXML private TableView<Viagem>               tabela;
    @FXML private TableColumn<Viagem, String>     colId;
    @FXML private TableColumn<Viagem, String>     colNavio;
    @FXML private TableColumn<Viagem, String>     colOrigem;
    @FXML private TableColumn<Viagem, String>     colDestino;
    @FXML private TableColumn<Viagem, String>     colPartida;
    @FXML private TableColumn<Viagem, String>     colChegada;
    @FXML private TableColumn<Viagem, String>     colChegadaReal;
    @FXML private TableColumn<Viagem, String>     colEstado;

    // ── botões da toolbar ─────────────────────────────────────────────────────
    @FXML private Button btnEditar;
    @FXML private Button btnAvancar;
    @FXML private Button btnCancelar;

    // ── label de estado ───────────────────────────────────────────────────────
    @FXML private Label labelMensagem;

    // ── tab cargas ────────────────────────────────────────────────────────────
    @FXML private TableView<Carga>                tabelaCargas;
    @FXML private TableColumn<Carga, String>      colCargaDesignacao;
    @FXML private TableColumn<Carga, String>      colCargaTipo;
    @FXML private TableColumn<Carga, String>      colCargaPeso;
    @FXML private TableColumn<Carga, String>      colCargaVolume;
    @FXML private ComboBox<Carga>                 comboCarga;

    // ── tab tripulantes ───────────────────────────────────────────────────────
    @FXML private TableView<Tripulante>           tabelaTripulantes;
    @FXML private TableColumn<Tripulante, String> colTripNome;
    @FXML private TableColumn<Tripulante, String> colTripFuncao;
    @FXML private TableColumn<Tripulante, String> colTripCertificado;
    @FXML private TableColumn<Tripulante, String> colTripDisponivel;
    @FXML private ComboBox<Tripulante>            comboTripulante;

    // ── serviços ──────────────────────────────────────────────────────────────
    private final ViagemService     viagemService     = new ViagemService();
    private final NavioService      navioService      = new NavioService();
    private final PortoService      portoService      = new PortoService();
    private final CargaService      cargaService      = new CargaService();
    private final TripulanteService tripulanteService = new TripulanteService();

    private final Map<Integer, Navio> naviosPorId = new HashMap<>();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();
    private List<Navio> naviosCache = new ArrayList<>();
    private List<Porto> portosCache = new ArrayList<>();

    private Viagem selecionado;

    // ── inicialização ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNavio.setCellValueFactory(c -> new SimpleStringProperty(nomeNavio(c.getValue().getNavioId())));
        colOrigem.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoOrigemId())));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDestinoId())));
        colPartida.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getDataPartida())));
        colChegada.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getDataChegadaPrevista())));
        colChegadaReal.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getDataChegadaReal())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getEstado())));

        colCargaDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colCargaTipo.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getTipo())));
        colCargaPeso.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPeso())));
        colCargaVolume.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getVolume())));

        colTripNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colTripFuncao.setCellValueFactory(c -> new SimpleStringProperty(str(c.getValue().getFuncao())));
        colTripCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colTripDisponivel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstadoDisponibilidade()));

        comboCarga.setConverter(new StringConverter<>() {
            @Override public String toString(Carga c)     { return c == null ? "" : c.getDesignacao() + " (" + c.getTipo() + ")"; }
            @Override public Carga fromString(String s)   { return null; }
        });
        comboTripulante.setConverter(new StringConverter<>() {
            @Override public String toString(Tripulante t){ return t == null ? "" : t.getNome() + " — " + t.getFuncao(); }
            @Override public Tripulante fromString(String s){ return null; }
        });

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            selecionado = novo;
            boolean sel = (novo != null);
            btnEditar.setDisable(!sel);
            btnAvancar.setDisable(!sel);
            btnCancelar.setDisable(!sel);
            if (novo != null) carregarDetalhesViagem(novo.getId());
            else {
                tabelaCargas.getItems().clear();
                tabelaTripulantes.getItems().clear();
                labelMensagem.setText("");
            }
        });

        tabela.setRowFactory(tv -> {
            TableRow<Viagem> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !row.isEmpty()) editar(); });
            return row;
        });

        recarregar();
    }

    // ── carregamento principal ────────────────────────────────────────────────

    private void recarregar() {
        labelMensagem.setText("A carregar…");
        Thread t = new Thread(() -> {
            try {
                List<Navio>      navios  = navioService.listarNavios();
                List<Porto>      portos  = portoService.listarPortos();
                List<Viagem>     viagens = viagemService.listarViagens();
                List<Carga>      cargas  = cargaService.listarCargas();
                List<Tripulante> trips   = tripulanteService.listarTripulantes();

                Platform.runLater(() -> {
                    naviosPorId.clear();
                    navios.forEach(n -> naviosPorId.put(n.getId(), n));
                    naviosCache = navios;

                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    portosCache = portos;

                    tabela.setItems(FXCollections.observableArrayList(viagens));
                    comboCarga.setItems(FXCollections.observableArrayList(cargas));
                    comboTripulante.setItems(FXCollections.observableArrayList(trips));
                    labelMensagem.setText(viagens.size() + " viagem(ns)");
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void carregarDetalhesViagem(int viagemId) {
        Thread t = new Thread(() -> {
            try {
                List<Carga>      cargas = viagemService.listarCargasDaViagem(viagemId);
                List<Tripulante> trips  = viagemService.listarTripulantesDaViagem(viagemId);
                Platform.runLater(() -> {
                    tabelaCargas.setItems(FXCollections.observableArrayList(cargas));
                    tabelaTripulantes.setItems(FXCollections.observableArrayList(trips));
                    if (selecionado != null)
                        labelMensagem.setText("Viagem #" + selecionado.getId() + "  [" + selecionado.getEstado() + "]");
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro ao carregar detalhes: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── acções da toolbar de viagens ──────────────────────────────────────────

    @FXML
    public void novo() {
        FormDialogs.mostrarViagem(null, naviosCache, portosCache).ifPresent(v -> {
            Thread t = new Thread(() -> {
                try {
                    viagemService.criarViagem(v);
                    Platform.runLater(() -> {
                        msg("Viagem criada (estado: PLANEADA).", true);
                        recarregar();
                    });
                } catch (IllegalArgumentException | IllegalStateException e) {
                    Platform.runLater(() -> Dialogs.erro(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Dialogs.erro("Erro ao criar viagem: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        });
    }

    @FXML
    public void editar() {
        if (selecionado == null) return;
        FormDialogs.mostrarViagem(selecionado, naviosCache, portosCache).ifPresent(v -> {
            Thread t = new Thread(() -> {
                try {
                    viagemService.editarViagem(v);
                    Platform.runLater(() -> {
                        msg("Viagem #" + v.getId() + " atualizada.", true);
                        recarregar();
                    });
                } catch (IllegalArgumentException | IllegalStateException e) {
                    Platform.runLater(() -> Dialogs.erro(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Dialogs.erro("Erro ao editar: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        });
    }

    @FXML
    public void avancarEstado() {
        if (selecionado == null) return;
        final int id = selecionado.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.avancarEstado(id);
                Platform.runLater(() -> { msg("Estado avançado.", true); recarregar(); });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void cancelarViagem() {
        if (selecionado == null) return;
        if (!Dialogs.confirmar("Cancelar a viagem #" + selecionado.getId() + "?")) return;
        final int id = selecionado.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.cancelarViagem(id);
                Platform.runLater(() -> { msg("Viagem cancelada.", true); recarregar(); });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── acções de cargas ──────────────────────────────────────────────────────

    @FXML
    public void adicionarCarga() {
        if (selecionado == null) { Dialogs.erro("Selecione primeiro uma viagem na tabela."); return; }
        Carga carga = comboCarga.getValue();
        if (carga == null) { Dialogs.erro("Selecione uma carga para adicionar."); return; }
        final int viagemId = selecionado.getId();
        final int cargaId  = carga.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.associarCarga(viagemId, cargaId);
                Platform.runLater(() -> {
                    comboCarga.setValue(null);
                    carregarDetalhesViagem(viagemId);
                    msg("Carga adicionada.", true);
                });
            } catch (IllegalArgumentException | IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao adicionar carga: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void removerCarga() {
        if (selecionado == null) { Dialogs.erro("Selecione primeiro uma viagem na tabela."); return; }
        Carga carga = tabelaCargas.getSelectionModel().getSelectedItem();
        if (carga == null) { Dialogs.erro("Selecione uma carga da lista para remover."); return; }
        if (!Dialogs.confirmar("Remover \"" + carga.getDesignacao() + "\" desta viagem?")) return;
        final int viagemId = selecionado.getId();
        final int cargaId  = carga.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.removerCarga(viagemId, cargaId);
                Platform.runLater(() -> { carregarDetalhesViagem(viagemId); msg("Carga removida.", true); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── acções de tripulantes ─────────────────────────────────────────────────

    @FXML
    public void adicionarTripulante() {
        if (selecionado == null) { Dialogs.erro("Selecione primeiro uma viagem na tabela."); return; }
        Tripulante trip = comboTripulante.getValue();
        if (trip == null) { Dialogs.erro("Selecione um tripulante para adicionar."); return; }
        final int viagemId     = selecionado.getId();
        final int tripulanteId = trip.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.associarTripulante(viagemId, tripulanteId);
                Platform.runLater(() -> {
                    comboTripulante.setValue(null);
                    carregarDetalhesViagem(viagemId);
                    msg("Tripulante adicionado.", true);
                });
            } catch (IllegalArgumentException | IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void removerTripulante() {
        if (selecionado == null) { Dialogs.erro("Selecione primeiro uma viagem na tabela."); return; }
        Tripulante trip = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (trip == null) { Dialogs.erro("Selecione um tripulante da lista para remover."); return; }
        if (!Dialogs.confirmar("Remover " + trip.getNome() + " desta viagem?")) return;
        final int viagemId     = selecionado.getId();
        final int tripulanteId = trip.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.removerTripulante(viagemId, tripulanteId);
                Platform.runLater(() -> { carregarDetalhesViagem(viagemId); msg("Tripulante removido.", true); });
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── auxiliares ────────────────────────────────────────────────────────────

    private String str(Object o)       { return o == null ? "" : o.toString(); }
    private String nomeNavio(int id)   { Navio n = naviosPorId.get(id); return n == null ? "#" + id : n.getNome(); }
    private String nomePorto(int id)   { Porto p = portosPorId.get(id); return p == null ? "-" : p.getNome(); }

    private void msg(String texto, boolean sucesso) {
        labelMensagem.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER, Styles.WARNING);
        labelMensagem.setText(texto);
        if (sucesso) labelMensagem.getStyleClass().add(Styles.SUCCESS);
        else         labelMensagem.getStyleClass().add(Styles.DANGER);
    }
}
