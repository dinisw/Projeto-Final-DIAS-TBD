package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Carga;
import com.dias.navios.model.Porto;
import com.dias.navios.model.TipoCarga;
import com.dias.navios.ui.Dialogs;
import javafx.application.Platform;
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
        colInflamavel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isInflamavel() ? "Sim" : "Não"));
        colCarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoCarregamentoId())));
        colDescarga.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoDescargaId())));

        comboTipo.setItems(FXCollections.observableArrayList(TipoCarga.values()));

        // As propriedades de perigo são determinadas pelo TIPO de carga, não são
        // editáveis pelo utilizador (ver F4): só-leitura e atualizadas a partir do tipo.
        checkInflamavel.setDisable(true);
        checkCorrosiva.setDisable(true);
        checkToxica.setDisable(true);
        comboTipo.valueProperty().addListener((obs, antigo, novo) -> mostrarPropriedadesDoTipo(novo));

        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        labelMensagem.setText("A carregar...");
        Thread t = new Thread(() -> {
            try {
                List<Porto> portos = portoService.listarPortos();
                List<Carga> cargas = cargaService.listarCargas();
                Platform.runLater(() -> {
                    portosPorId.clear();
                    portos.forEach(p -> portosPorId.put(p.getId(), p));
                    comboPortoCarga.setItems(FXCollections.observableArrayList(portos));
                    comboPortoDescarga.setItems(FXCollections.observableArrayList(portos));
                    tabela.setItems(FXCollections.observableArrayList(cargas));
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
        labelMensagem.setText("A criar uma nova carga.");
    }

    @FXML
    public void guardar() {
        try {
            if (comboTipo.getValue() == null) throw new IllegalArgumentException("Selecione o tipo de carga.");

            final TipoCarga tipo = comboTipo.getValue();
            final Carga carga = (selecionado == null) ? new Carga() : selecionado;
            carga.setDesignacao(campoDesignacao.getText());
            carga.setTipo(tipo);
            carga.setVolume(parseDouble(campoVolume.getText(), "Volume"));
            carga.setPeso(parseDouble(campoPeso.getText(), "Peso"));
            // Propriedades de perigo derivam do tipo (não dos checkboxes) — ver F4.
            carga.setInflamavel(tipo.isInflamavel());
            carga.setCorrosiva(tipo.isCorrosiva());
            carga.setToxica(tipo.isToxica());
            carga.setPortoCarregamentoId(comboPortoCarga.getValue() == null ? 0 : comboPortoCarga.getValue().getId());
            carga.setPortoDescargaId(comboPortoDescarga.getValue() == null ? 0 : comboPortoDescarga.getValue().getId());

            final boolean isNova = (selecionado == null);
            Thread t = new Thread(() -> {
                try {
                    if (isNova) cargaService.registarCarga(carga);
                    else        cargaService.editarCarga(carga);
                    Platform.runLater(() -> {
                        Dialogs.info(isNova ? "Carga criada com sucesso." : "Carga atualizada com sucesso.");
                        novo();
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
        } catch (IllegalArgumentException e) {
            Dialogs.erro(e.getMessage());
        }
    }

    @FXML
    public void apagar() {
        Carga sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione uma carga para apagar."); return; }
        if (!Dialogs.confirmar("Apagar a carga \"" + sel.getDesignacao() + "\"?")) return;

        final int id = sel.getId();
        Thread t = new Thread(() -> {
            try {
                cargaService.apagarCarga(id);
                Platform.runLater(() -> {
                    Dialogs.info("Carga apagada.");
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

    /** Reflete nos checkboxes (só-leitura) as propriedades de perigo do tipo escolhido. */
    private void mostrarPropriedadesDoTipo(TipoCarga tipo) {
        checkInflamavel.setSelected(tipo != null && tipo.isInflamavel());
        checkCorrosiva.setSelected(tipo != null && tipo.isCorrosiva());
        checkToxica.setSelected(tipo != null && tipo.isToxica());
    }

    private String texto(Object o) { return o == null ? "" : o.toString(); }

    private String nomePorto(int id) {
        Porto p = portosPorId.get(id);
        return p == null ? "-" : p.getNome();
    }

    private double parseDouble(String s, String campo) {
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um número."); }
    }
}
