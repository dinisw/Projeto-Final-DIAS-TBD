package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Porto;
import com.dias.navios.model.TipoNavio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class NavioController {

    @FXML private TableView<Navio> tabelaNavios;
    @FXML private TableColumn<Navio, String> colNome;
    @FXML private TableColumn<Navio, String> colIMO;
    @FXML private TableColumn<Navio, String> colTipo;
    @FXML private TableColumn<Navio, String> colCapacidade;
    @FXML private TableColumn<Navio, String> colEstado;

    @FXML private TextField campoNome;
    @FXML private TextField campoIMO;
    @FXML private ComboBox<TipoNavio> comboTipo;
    @FXML private TextField campoCapacidade;
    @FXML private TextField campoTanques;
    @FXML private TextField campoBandeira;
    @FXML private TextField campoAno;
    @FXML private ComboBox<EstadoNavio> comboEstado;
    @FXML private ComboBox<Porto> comboPorto;
    @FXML private TextField campoPesquisa;
    @FXML private Label labelMensagem;

    private NavioService navioService = new NavioService();
    private PortoService portoService = new PortoService();
    private Navio navioSelecionado = null;

    @FXML
    public void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colIMO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoIMO()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().name()));
        colCapacidade.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacidadeMaxima()) + " ton"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));

        comboTipo.setItems(FXCollections.observableArrayList(TipoNavio.values()));
        comboEstado.setItems(FXCollections.observableArrayList(EstadoNavio.values()));

        tabelaNavios.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) carregarNoFormulario(novo);
        });

        carregarPortos();
        carregarTabela();
    }

    private void carregarPortos() {
        try {
            List<Porto> portos = portoService.listarPortos();
            comboPorto.setItems(FXCollections.observableArrayList(portos));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar portos: " + e.getMessage());
        }
    }

    private void carregarTabela() {
        try {
            List<Navio> navios = navioService.listarNavios();
            String filtro = campoPesquisa != null ? campoPesquisa.getText().toLowerCase() : "";
            if (!filtro.isBlank()) {
                navios.removeIf(n -> !n.getNome().toLowerCase().contains(filtro)
                        && !n.getCodigoIMO().toLowerCase().contains(filtro));
            }
            tabelaNavios.setItems(FXCollections.observableArrayList(navios));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar navios: " + e.getMessage());
        }
    }

    private void carregarNoFormulario(Navio navio) {
        navioSelecionado = navio;
        campoNome.setText(navio.getNome());
        campoIMO.setText(navio.getCodigoIMO());
        comboTipo.setValue(navio.getTipo());
        campoCapacidade.setText(String.valueOf(navio.getCapacidadeMaxima()));
        campoTanques.setText(String.valueOf(navio.getNumTanques()));
        campoBandeira.setText(navio.getBandeira() != null ? navio.getBandeira() : "");
        campoAno.setText(String.valueOf(navio.getAnoFabrico()));
        comboEstado.setValue(navio.getEstado());

        ObservableList<Porto> portos = comboPorto.getItems();
        portos.stream()
              .filter(p -> p.getId() == navio.getPortoAtualId())
              .findFirst()
              .ifPresent(comboPorto::setValue);

        labelMensagem.setText("Navio carregado para edição.");
    }

    @FXML
    public void guardarNavio() {
        try {
            Navio navio = navioSelecionado != null ? navioSelecionado : new Navio();
            navio.setNome(campoNome.getText());
            navio.setCodigoIMO(campoIMO.getText());
            navio.setTipo(comboTipo.getValue());
            navio.setCapacidadeMaxima(Double.parseDouble(campoCapacidade.getText()));
            navio.setNumTanques(Integer.parseInt(campoTanques.getText()));
            navio.setBandeira(campoBandeira.getText());
            navio.setAnoFabrico(Integer.parseInt(campoAno.getText()));
            navio.setEstado(comboEstado.getValue());
            navio.setPortoAtualId(comboPorto.getValue() != null ? comboPorto.getValue().getId() : 0);

            if (navioSelecionado != null) {
                navioService.editarNavio(navio);
                labelMensagem.setText("Navio actualizado com sucesso.");
            } else {
                navioService.registarNavio(navio);
                labelMensagem.setText("Navio registado com sucesso.");
            }
            navioSelecionado = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            labelMensagem.setText("Validação/Formato: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void apagarNavio() {
        Navio selecionado = tabelaNavios.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            labelMensagem.setText("Seleccione um navio para apagar.");
            return;
        }
        try {
            navioService.apagarNavio(selecionado.getId());
            labelMensagem.setText("Navio apagado.");
            navioSelecionado = null;
            limparFormulario();
            carregarTabela();
        } catch (IllegalStateException e) {
            labelMensagem.setText("Regra de negócio: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void pesquisar() {
        carregarTabela();
    }

    @FXML
    public void novoNavio() {
        navioSelecionado = null;
        limparFormulario();
        labelMensagem.setText("Formulário pronto para novo navio.");
    }

    private void limparFormulario() {
        campoNome.clear();
        campoIMO.clear();
        campoCapacidade.clear();
        campoTanques.clear();
        campoBandeira.clear();
        campoAno.clear();
        comboTipo.setValue(null);
        comboEstado.setValue(null);
        comboPorto.setValue(null);
        tabelaNavios.getSelectionModel().clearSelection();
    }
}
