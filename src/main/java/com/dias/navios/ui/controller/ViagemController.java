package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.TripulanteService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.*;
import com.dias.navios.ui.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViagemController {

    // ── tabela principal ──────────────────────────────────────────────────────
    @FXML private TableView<Viagem>  tabela;
    @FXML private TableColumn<Viagem, String> colId;
    @FXML private TableColumn<Viagem, String> colNavio;
    @FXML private TableColumn<Viagem, String> colOrigem;
    @FXML private TableColumn<Viagem, String> colDestino;
    @FXML private TableColumn<Viagem, String> colPartida;
    @FXML private TableColumn<Viagem, String> colChegada;
    @FXML private TableColumn<Viagem, String> colEstado;

    // ── formulário ────────────────────────────────────────────────────────────
    @FXML private ComboBox<Navio>  comboNavio;
    @FXML private ComboBox<Porto>  comboOrigem;
    @FXML private ComboBox<Porto>  comboDestino;
    @FXML private DatePicker       dataPartida;
    @FXML private DatePicker       dataChegada;
    @FXML private Label            labelMensagem;

    // ── tab cargas ────────────────────────────────────────────────────────────
    @FXML private TableView<Carga>  tabelaCargas;
    @FXML private TableColumn<Carga, String> colCargaDesignacao;
    @FXML private TableColumn<Carga, String> colCargaTipo;
    @FXML private TableColumn<Carga, String> colCargaPeso;
    @FXML private TableColumn<Carga, String> colCargaVolume;
    @FXML private ComboBox<Carga>   comboCarga;

    // ── tab tripulantes ───────────────────────────────────────────────────────
    @FXML private TableView<Tripulante>  tabelaTripulantes;
    @FXML private TableColumn<Tripulante, String> colTripNome;
    @FXML private TableColumn<Tripulante, String> colTripFuncao;
    @FXML private TableColumn<Tripulante, String> colTripCertificado;
    @FXML private TableColumn<Tripulante, String> colTripDisponivel;
    @FXML private ComboBox<Tripulante>   comboTripulante;

    // ── serviços ──────────────────────────────────────────────────────────────
    private final ViagemService     viagemService     = new ViagemService();
    private final NavioService      navioService      = new NavioService();
    private final PortoService      portoService      = new PortoService();
    private final CargaService      cargaService      = new CargaService();
    private final TripulanteService tripulanteService = new TripulanteService();

    private final Map<Integer, Navio> naviosPorId = new HashMap<>();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();

    private Viagem selecionado;

    // ── inicialização ─────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // colunas da tabela de viagens
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNavio.setCellValueFactory(c -> new SimpleStringProperty(nomeNavio(c.getValue().getNavioId())));
        colOrigem.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoOrigemId())));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDestinoId())));
        colPartida.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataPartida())));
        colChegada.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataChegadaPrevista())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getEstado())));

        // colunas da tabela de cargas
        colCargaDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colCargaTipo.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getTipo())));
        colCargaPeso.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPeso())));
        colCargaVolume.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getVolume())));

        // colunas da tabela de tripulantes
        colTripNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colTripFuncao.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getFuncao())));
        colTripCertificado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumeroCertificado()));
        colTripDisponivel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isDisponivel() ? "Sim" : "Não"));

        // conversor para mostrar nome nas ComboBox
        comboCarga.setConverter(new StringConverter<>() {
            @Override public String toString(Carga c)     { return c == null ? "" : c.getDesignacao() + " (" + c.getTipo() + ")"; }
            @Override public Carga fromString(String s)   { return null; }
        });
        comboTripulante.setConverter(new StringConverter<>() {
            @Override public String toString(Tripulante t){ return t == null ? "" : t.getNome() + " — " + t.getFuncao(); }
            @Override public Tripulante fromString(String s){ return null; }
        });
        comboNavio.setConverter(new StringConverter<>() {
            @Override public String toString(Navio n)    { return n == null ? "" : n.getNome(); }
            @Override public Navio fromString(String s)  { return null; }
        });
        comboOrigem.setConverter(new StringConverter<>() {
            @Override public String toString(Porto p)    { return p == null ? "" : p.getNome() + " (" + p.getCodigo() + ")"; }
            @Override public Porto fromString(String s)  { return null; }
        });
        comboDestino.setConverter(comboOrigem.getConverter());

        // quando seleciona uma viagem, carrega o formulário e os detalhes
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    // ── carregamento principal ────────────────────────────────────────────────

    private void recarregar() {
        labelMensagem.setText("A carregar...");
        Thread t = new Thread(() -> {
            try {
                List<Navio>  navios  = navioService.listarNavios();
                List<Porto>  portos  = portoService.listarPortos();
                List<Viagem> viagens = viagemService.listarViagens();
                List<Carga>  cargas  = cargaService.listarCargas();
                List<Tripulante> trips = tripulanteService.listarTripulantes();

                Platform.runLater(() -> {
                    naviosPorId.clear();
                    navios.forEach(n -> naviosPorId.put(n.getId(), n));
                    comboNavio.setItems(FXCollections.observableArrayList(navios));

                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    comboOrigem.setItems(FXCollections.observableArrayList(portos));
                    comboDestino.setItems(FXCollections.observableArrayList(portos));

                    tabela.setItems(FXCollections.observableArrayList(viagens));

                    comboCarga.setItems(FXCollections.observableArrayList(cargas));
                    comboTripulante.setItems(FXCollections.observableArrayList(trips));

                    labelMensagem.setText("");
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro ao carregar: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /** Carrega as cargas e tripulantes da viagem seleccionada nas subtabelas. */
    private void carregarDetalhesViagem(int viagemId) {
        Thread t = new Thread(() -> {
            try {
                List<Carga>      cargas = viagemService.listarCargasDaViagem(viagemId);
                List<Tripulante> trips  = viagemService.listarTripulantesDaViagem(viagemId);
                Platform.runLater(() -> {
                    tabelaCargas.setItems(FXCollections.observableArrayList(cargas));
                    tabelaTripulantes.setItems(FXCollections.observableArrayList(trips));
                });
            } catch (Exception e) {
                Platform.runLater(() -> labelMensagem.setText("Erro ao carregar detalhes: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── acções do formulário de viagem ────────────────────────────────────────

    @FXML
    public void novo() {
        selecionado = null;
        comboNavio.setValue(null);
        comboOrigem.setValue(null);
        comboDestino.setValue(null);
        dataPartida.setValue(null);
        dataChegada.setValue(null);
        tabela.getSelectionModel().clearSelection();
        tabelaCargas.getItems().clear();
        tabelaTripulantes.getItems().clear();
        labelMensagem.setText("A planear uma nova viagem.");
    }

    @FXML
    public void criarViagem() {
        try {
            validarFormulario();
            final Viagem v = new Viagem();
            v.setNavioId(comboNavio.getValue().getId());
            v.setPortoOrigemId(comboOrigem.getValue().getId());
            v.setPortoDestinoId(comboDestino.getValue().getId());
            v.setDataPartida(dataPartida.getValue());
            v.setDataChegadaPrevista(dataChegada.getValue());

            Thread t = new Thread(() -> {
                try {
                    viagemService.criarViagem(v);
                    Platform.runLater(() -> {
                        Dialogs.info("Viagem criada com sucesso (estado: PLANEADA).");
                        novo();
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
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        }
    }

    @FXML
    public void atualizarViagem() {
        if (selecionado == null) { Dialogs.erro("Selecione uma viagem na tabela para editar."); return; }
        try {
            validarFormulario();
            final Viagem v = new Viagem();
            v.setId(selecionado.getId());
            v.setNavioId(comboNavio.getValue().getId());
            v.setPortoOrigemId(comboOrigem.getValue().getId());
            v.setPortoDestinoId(comboDestino.getValue().getId());
            v.setDataPartida(dataPartida.getValue());
            v.setDataChegadaPrevista(dataChegada.getValue());

            Thread t = new Thread(() -> {
                try {
                    viagemService.editarViagem(v);
                    Platform.runLater(() -> {
                        Dialogs.info("Viagem #" + v.getId() + " atualizada com sucesso.");
                        novo();
                        recarregar();
                    });
                } catch (IllegalArgumentException | IllegalStateException e) {
                    Platform.runLater(() -> Dialogs.erro(e.getMessage()));
                } catch (Exception e) {
                    Platform.runLater(() -> Dialogs.erro("Erro ao atualizar viagem: " + e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        }
    }

    @FXML
    public void avancarEstado() {
        Viagem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma viagem na tabela."); return; }
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.avancarEstado(id);
                Platform.runLater(() -> {
                    Dialogs.info("Estado da viagem avançado.");
                    recarregar();
                });
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
        Viagem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma viagem na tabela."); return; }
        if (!Dialogs.confirmar("Cancelar a viagem #" + sel.getId() + "?")) return;
        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.cancelarViagem(id);
                Platform.runLater(() -> {
                    Dialogs.info("Viagem cancelada.");
                    recarregar();
                });
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
        if (carga == null) { Dialogs.erro("Selecione uma carga na lista para adicionar."); return; }

        final int viagemId = selecionado.getId();
        final int cargaId  = carga.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.associarCarga(viagemId, cargaId);
                Platform.runLater(() -> {
                    comboCarga.setValue(null);
                    carregarDetalhesViagem(viagemId);
                    labelMensagem.setText("Carga adicionada com sucesso.");
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
        if (carga == null) { Dialogs.erro("Selecione uma carga na lista para remover."); return; }
        if (!Dialogs.confirmar("Remover a carga \"" + carga.getDesignacao() + "\" desta viagem?")) return;

        final int viagemId = selecionado.getId();
        final int cargaId  = carga.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.removerCarga(viagemId, cargaId);
                Platform.runLater(() -> {
                    carregarDetalhesViagem(viagemId);
                    labelMensagem.setText("Carga removida.");
                });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao remover carga: " + e.getMessage()));
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
        if (trip == null) { Dialogs.erro("Selecione um tripulante na lista para adicionar."); return; }

        final int viagemId     = selecionado.getId();
        final int tripulanteId = trip.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.associarTripulante(viagemId, tripulanteId);
                Platform.runLater(() -> {
                    comboTripulante.setValue(null);
                    carregarDetalhesViagem(viagemId);
                    labelMensagem.setText("Tripulante adicionado com sucesso.");
                });
            } catch (IllegalArgumentException | IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao adicionar tripulante: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    public void removerTripulante() {
        if (selecionado == null) { Dialogs.erro("Selecione primeiro uma viagem na tabela."); return; }
        Tripulante trip = tabelaTripulantes.getSelectionModel().getSelectedItem();
        if (trip == null) { Dialogs.erro("Selecione um tripulante na lista para remover."); return; }
        if (!Dialogs.confirmar("Remover " + trip.getNome() + " desta viagem?")) return;

        final int viagemId     = selecionado.getId();
        final int tripulanteId = trip.getId();
        Thread t = new Thread(() -> {
            try {
                viagemService.removerTripulante(viagemId, tripulanteId);
                Platform.runLater(() -> {
                    carregarDetalhesViagem(viagemId);
                    labelMensagem.setText("Tripulante removido.");
                });
            } catch (IllegalStateException e) {
                Platform.runLater(() -> Dialogs.erro(e.getMessage()));
            } catch (Exception e) {
                Platform.runLater(() -> Dialogs.erro("Erro ao remover tripulante: " + e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ── auxiliares ────────────────────────────────────────────────────────────

    private void preencher(Viagem v) {
        selecionado = v;
        if (v == null) {
            tabelaCargas.getItems().clear();
            tabelaTripulantes.getItems().clear();
            return;
        }
        comboNavio.setValue(naviosPorId.get(v.getNavioId()));
        comboOrigem.setValue(portosPorId.get(v.getPortoOrigemId()));
        comboDestino.setValue(portosPorId.get(v.getPortoDestinoId()));
        dataPartida.setValue(v.getDataPartida());
        dataChegada.setValue(v.getDataChegadaPrevista());
        labelMensagem.setText("Viagem #" + v.getId() + "  [" + v.getEstado() + "]");
        carregarDetalhesViagem(v.getId());
    }

    private void validarFormulario() {
        if (comboNavio.getValue() == null)   throw new IllegalArgumentException("Selecione o navio.");
        if (comboOrigem.getValue() == null)  throw new IllegalArgumentException("Selecione o porto de origem.");
        if (comboDestino.getValue() == null) throw new IllegalArgumentException("Selecione o porto de destino.");
        if (dataPartida.getValue() == null)  throw new IllegalArgumentException("Indique a data de partida.");
        if (comboOrigem.getValue().getId() == comboDestino.getValue().getId())
            throw new IllegalArgumentException("A origem e o destino não podem ser o mesmo porto.");
        if (dataChegada.getValue() != null && dataChegada.getValue().isBefore(dataPartida.getValue()))
            throw new IllegalArgumentException("A data de chegada não pode ser anterior à de partida.");
    }

    private String texto(Object o)     { return o == null ? "" : o.toString(); }
    private String nomeNavio(int id)   { Navio n = naviosPorId.get(id); return n == null ? "#" + id : n.getNome(); }
    private String nomePorto(int id)   { Porto p = portosPorId.get(id); return p == null ? "-" : p.getNome(); }
}
