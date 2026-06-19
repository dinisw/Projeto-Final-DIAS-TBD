package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Porto;
import com.dias.navios.model.Viagem;
import com.dias.navios.ui.Dialogs;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViagemController {

    @FXML private TableView<Viagem> tabela;
    @FXML private TableColumn<Viagem, String> colId;
    @FXML private TableColumn<Viagem, String> colNavio;
    @FXML private TableColumn<Viagem, String> colOrigem;
    @FXML private TableColumn<Viagem, String> colDestino;
    @FXML private TableColumn<Viagem, String> colPartida;
    @FXML private TableColumn<Viagem, String> colChegada;
    @FXML private TableColumn<Viagem, String> colEstado;

    @FXML private ComboBox<Navio> comboNavio;
    @FXML private ComboBox<Porto> comboOrigem;
    @FXML private ComboBox<Porto> comboDestino;
    @FXML private DatePicker dataPartida;
    @FXML private DatePicker dataChegada;
    @FXML private Label labelMensagem;

    private final ViagemService viagemService = new ViagemService();
    private final NavioService navioService   = new NavioService();
    private final PortoService portoService   = new PortoService();

    private final Map<Integer, Navio> naviosPorId = new HashMap<>();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();

    private Viagem selecionado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNavio.setCellValueFactory(c -> new SimpleStringProperty(nomeNavio(c.getValue().getNavioId())));
        colOrigem.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoOrigemId())));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDestinoId())));
        colPartida.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataPartida())));
        colChegada.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataChegadaPrevista())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getEstado())));

        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar...");
        Thread t = new Thread(() -> {
            try {
                List<Navio> navios   = navioService.listarNavios();
                List<Porto> portos   = portoService.listarPortos();
                List<Viagem> viagens = viagemService.listarViagens();
                Platform.runLater(() -> {
                    naviosPorId.clear();
                    navios.forEach(n -> naviosPorId.put(n.getId(), n));
                    comboNavio.setItems(FXCollections.observableArrayList(navios));

                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    comboOrigem.setItems(FXCollections.observableArrayList(portos));
                    comboDestino.setItems(FXCollections.observableArrayList(portos));

                    tabela.setItems(FXCollections.observableArrayList(viagens));
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
        selecionado = null;
        comboNavio.setValue(null);
        comboOrigem.setValue(null);
        comboDestino.setValue(null);
        dataPartida.setValue(null);
        dataChegada.setValue(null);
        tabela.getSelectionModel().clearSelection();
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

    private void preencher(Viagem v) {
        selecionado = v;
        if (v == null) return;
        comboNavio.setValue(naviosPorId.get(v.getNavioId()));
        comboOrigem.setValue(portosPorId.get(v.getPortoOrigemId()));
        comboDestino.setValue(portosPorId.get(v.getPortoDestinoId()));
        dataPartida.setValue(v.getDataPartida());
        dataChegada.setValue(v.getDataChegadaPrevista());
        labelMensagem.setText("A editar viagem #" + v.getId() + " [" + v.getEstado() + "]");
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

    private String texto(Object o) { return o == null ? "" : o.toString(); }

    private String nomeNavio(int id) {
        Navio n = naviosPorId.get(id);
        return n == null ? ("#" + id) : n.getNome();
    }

    private String nomePorto(int id) {
        Porto p = portosPorId.get(id);
        return p == null ? "-" : p.getNome();
    }
}
