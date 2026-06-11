package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Carga;
import com.dias.navios.model.Porto;
import com.dias.navios.model.TipoCarga;
import com.dias.navios.ui.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CargaController {

    @FXML private TableView<Carga> tabela;
    @FXML private TableColumn<Carga, String> colDesignacao;
    @FXML private TableColumn<Carga, String> colTipo;
    @FXML private TableColumn<Carga, String> colVolume;
    @FXML private TableColumn<Carga, String> colPeso;
    @FXML private TableColumn<Carga, String> colInflamavel;
    @FXML private TableColumn<Carga, String> colCarga;
    @FXML private TableColumn<Carga, String> colDescarga;

    @FXML private TextField campoDesignacao;
    @FXML private ComboBox<TipoCarga> comboTipo;
    @FXML private TextField campoVolume;
    @FXML private TextField campoPeso;
    @FXML private CheckBox checkInflamavel;
    @FXML private CheckBox checkCorrosiva;
    @FXML private CheckBox checkToxica;
    @FXML private ComboBox<Porto> comboPortoCarga;
    @FXML private ComboBox<Porto> comboPortoDescarga;
    @FXML private Label labelMensagem;

    private final CargaService cargaService = new CargaService();
    private final PortoService portoService = new PortoService();

    private Carga selecionado;
    private final Map<Integer, Porto> portosPorId = new HashMap<>();

    @FXML
    public void initialize() {
        colDesignacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignacao()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getTipo())));
        colVolume.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getVolume())));
        colPeso.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPeso())));
        colInflamavel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isInflamavel() ? "Sim" : "Nao"));
        colCarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoCarregamentoId())));
        colDescarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDescargaId())));

        comboTipo.setItems(FXCollections.observableArrayList(TipoCarga.values()));
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        try {
            List<Porto> portos = portoService.listar();
            portosPorId.clear();
            for (Porto p : portos) portosPorId.put(p.getId(), p);
            comboPortoCarga.setItems(FXCollections.observableArrayList(portos));
            comboPortoDescarga.setItems(FXCollections.observableArrayList(portos));

            tabela.setItems(FXCollections.observableArrayList(cargaService.listarCargas()));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar dados: " + e.getMessage());
        }
    }

    @FXML
    public void novo() {
        tabela.getSelectionModel().clearSelection();
        selecionado = null;
        limpar();
        labelMensagem.setText("A criar uma nova carga.");
    }

    @FXML
    public void guardar() {
        try {
            if (comboTipo.getValue() == null) throw new IllegalArgumentException("Selecione o tipo de carga.");

            Carga carga = (selecionado == null) ? new Carga() : selecionado;
            carga.setDesignacao(campoDesignacao.getText());
            carga.setTipo(comboTipo.getValue());
            carga.setVolume(parseDouble(campoVolume.getText(), "Volume"));
            carga.setPeso(parseDouble(campoPeso.getText(), "Peso"));
            carga.setInflamavel(checkInflamavel.isSelected());
            carga.setCorrosiva(checkCorrosiva.isSelected());
            carga.setToxica(checkToxica.isSelected());
            carga.setPortoCarregamentoId(comboPortoCarga.getValue() == null ? 0 : comboPortoCarga.getValue().getId());
            carga.setPortoDescargaId(comboPortoDescarga.getValue() == null ? 0 : comboPortoDescarga.getValue().getId());

            if (selecionado == null) {
                cargaService.registarCarga(carga);
                Dialogs.info("Carga criada com sucesso.");
            } else {
                cargaService.editarCarga(carga);
                Dialogs.info("Carga atualizada com sucesso.");
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
        Carga sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma carga para apagar."); return; }
        if (!Dialogs.confirmar("Apagar a carga \"" + sel.getDesignacao() + "\"?")) return;
        try {
            cargaService.apagarCarga(sel.getId());
            Dialogs.info("Carga apagada.");
            novo();
            recarregar();
        } catch (Exception e) {
            Dialogs.erro("Erro ao apagar: " + e.getMessage());
        }
    }

    private void preencher(Carga c) {
        selecionado = c;
        if (c == null) return;
        campoDesignacao.setText(c.getDesignacao());
        comboTipo.setValue(c.getTipo());
        campoVolume.setText(String.valueOf(c.getVolume()));
        campoPeso.setText(String.valueOf(c.getPeso()));
        checkInflamavel.setSelected(c.isInflamavel());
        checkCorrosiva.setSelected(c.isCorrosiva());
        checkToxica.setSelected(c.isToxica());
        comboPortoCarga.setValue(portosPorId.get(c.getPortoCarregamentoId()));
        comboPortoDescarga.setValue(portosPorId.get(c.getPortoDescargaId()));
        labelMensagem.setText("A editar: " + c.getDesignacao());
    }

    private void limpar() {
        campoDesignacao.clear();
        comboTipo.setValue(null);
        campoVolume.clear();
        campoPeso.clear();
        checkInflamavel.setSelected(false);
        checkCorrosiva.setSelected(false);
        checkToxica.setSelected(false);
        comboPortoCarga.setValue(null);
        comboPortoDescarga.setValue(null);
    }

    private String texto(Object o) { return o == null ? "" : o.toString(); }

    private String nomePorto(int id) {
        Porto p = portosPorId.get(id);
        return p == null ? "-" : p.getNome();
    }

    private double parseDouble(String s, String campo) {
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um numero."); }
    }
}
