package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Porto;
import com.dias.navios.model.Viagem;
import com.dias.navios.ui.Dialogs;
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
    private final NavioService navioService = new NavioService();
    private final PortoService portoService = new PortoService();

    // Para mostrar nomes (em vez de IDs) na tabela
    private final Map<Integer, Navio> naviosPorId = new HashMap<>();
    private final Map<Integer, Porto> portosPorId = new HashMap<>();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colNavio.setCellValueFactory(c -> new SimpleStringProperty(nomeNavio(c.getValue().getNavioId())));
        colOrigem.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoOrigemId())));
        colDestino.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDestinoId())));
        colPartida.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataPartida())));
        colChegada.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getDataChegadaPrevista())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getEstado())));

        recarregar();
    }

    private void recarregar() {
        try {
            List<Navio> navios = navioService.listarNavios();
            naviosPorId.clear();
            for (Navio n : navios) naviosPorId.put(n.getId(), n);
            comboNavio.setItems(FXCollections.observableArrayList(navios));

            List<Porto> portos = portoService.listar();
            portosPorId.clear();
            for (Porto p : portos) portosPorId.put(p.getId(), p);
            comboOrigem.setItems(FXCollections.observableArrayList(portos));
            comboDestino.setItems(FXCollections.observableArrayList(portos));

            tabela.setItems(FXCollections.observableArrayList(viagemService.listarViagens()));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar dados: " + e.getMessage());
        }
    }

    @FXML
    public void novo() {
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
            if (comboNavio.getValue() == null) throw new IllegalArgumentException("Selecione o navio.");
            if (comboOrigem.getValue() == null) throw new IllegalArgumentException("Selecione o porto de origem.");
            if (comboDestino.getValue() == null) throw new IllegalArgumentException("Selecione o porto de destino.");
            if (dataPartida.getValue() == null) throw new IllegalArgumentException("Indique a data de partida.");
            if (comboOrigem.getValue().getId() == comboDestino.getValue().getId())
                throw new IllegalArgumentException("A origem e o destino nao podem ser o mesmo porto.");
            if (dataChegada.getValue() != null && dataChegada.getValue().isBefore(dataPartida.getValue()))
                throw new IllegalArgumentException("A data de chegada nao pode ser anterior a de partida.");

            Viagem v = new Viagem();
            v.setNavioId(comboNavio.getValue().getId());
            v.setPortoOrigemId(comboOrigem.getValue().getId());
            v.setPortoDestinoId(comboDestino.getValue().getId());
            v.setDataPartida(dataPartida.getValue());
            v.setDataChegadaPrevista(dataChegada.getValue());

            viagemService.criarViagem(v);   // o service valida regras de negocio e poe estado PLANEADA
            Dialogs.info("Viagem criada com sucesso (estado: PLANEADA).");
            novo();
            recarregar();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Dialogs.erro(e.getMessage());
        } catch (Exception e) {
            Dialogs.erro("Erro ao criar viagem: " + e.getMessage());
        }
    }

    @FXML
    public void avancarEstado() {
        Viagem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma viagem na tabela."); return; }
        try {
            viagemService.avancarEstado(sel.getId());
            Dialogs.info("Estado da viagem avancado.");
            recarregar();
        } catch (IllegalStateException e) {
            Dialogs.erro(e.getMessage());
        } catch (Exception e) {
            Dialogs.erro("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarViagem() {
        Viagem sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma viagem na tabela."); return; }
        if (!Dialogs.confirmar("Cancelar a viagem #" + sel.getId() + "?")) return;
        try {
            viagemService.cancelarViagem(sel.getId());
            Dialogs.info("Viagem cancelada.");
            recarregar();
        } catch (IllegalStateException e) {
            Dialogs.erro(e.getMessage());
        } catch (Exception e) {
            Dialogs.erro("Erro: " + e.getMessage());
        }
    }

    // ───── auxiliares ─────
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
