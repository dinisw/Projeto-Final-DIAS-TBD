package com.dias.navios.ui.controller;

import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Porto;
import com.dias.navios.model.TipoNavio;
import com.dias.navios.ui.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavioController {

    @FXML private TableView<Navio> tabela;
    @FXML private TableColumn<Navio, String> colNome;
    @FXML private TableColumn<Navio, String> colIMO;
    @FXML private TableColumn<Navio, String> colTipo;
    @FXML private TableColumn<Navio, String> colCapacidade;
    @FXML private TableColumn<Navio, String> colEstado;
    @FXML private TableColumn<Navio, String> colPorto;

    @FXML private TextField campoNome;
    @FXML private TextField campoIMO;
    @FXML private ComboBox<TipoNavio> comboTipo;
    @FXML private TextField campoCapacidade;
    @FXML private TextField campoTanques;
    @FXML private TextField campoBandeira;
    @FXML private TextField campoAno;
    @FXML private ComboBox<EstadoNavio> comboEstado;
    @FXML private ComboBox<Porto> comboPorto;
    @FXML private Label labelMensagem;

    private final NavioService navioService = new NavioService();
    private final PortoService portoService = new PortoService();

    private Navio selecionado;                                  // null => criar novo
    private final Map<Integer, Porto> portosPorId = new HashMap<>();

    @FXML
    public void initialize() {
        // 1) Ligar cada coluna ao atributo do objeto (senao a tabela aparece vazia)
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colIMO.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoIMO()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getTipo())));
        colCapacidade.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCapacidadeMaxima())));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(texto(c.getValue().getEstado())));
        colPorto.setCellValueFactory(c -> new SimpleStringProperty(nomePorto(c.getValue().getPortoAtualId())));

        // 2) Preencher as ComboBox com os valores possiveis
        comboTipo.setItems(FXCollections.observableArrayList(TipoNavio.values()));
        comboEstado.setItems(FXCollections.observableArrayList(EstadoNavio.values()));

        // 3) Ao selecionar uma linha, carregar no formulario (permite editar)
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        try {
            // Portos: servem para a ComboBox e para mostrar o nome na tabela
            List<Porto> portos = portoService.listar();
            portosPorId.clear();
            for (Porto p : portos) portosPorId.put(p.getId(), p);
            comboPorto.setItems(FXCollections.observableArrayList(portos));

            tabela.setItems(FXCollections.observableArrayList(navioService.listarNavios()));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar dados: " + e.getMessage());
        }
    }

    @FXML
    public void novo() {
        tabela.getSelectionModel().clearSelection();
        selecionado = null;
        limpar();
        labelMensagem.setText("A criar um novo navio.");
    }

    @FXML
    public void guardar() {
        try {
            if (comboTipo.getValue() == null) throw new IllegalArgumentException("Selecione o tipo de navio.");
            if (comboEstado.getValue() == null) throw new IllegalArgumentException("Selecione o estado do navio.");
            if (comboPorto.getValue() == null) throw new IllegalArgumentException("Selecione o porto atual do navio.");

            Navio navio = (selecionado == null) ? new Navio() : selecionado;
            navio.setNome(campoNome.getText());
            navio.setCodigoIMO(campoIMO.getText());
            navio.setTipo(comboTipo.getValue());
            navio.setCapacidadeMaxima(parseDouble(campoCapacidade.getText(), "Capacidade"));
            navio.setNumTanques(parseInt(campoTanques.getText(), "Nº de tanques"));
            navio.setBandeira(campoBandeira.getText());
            navio.setAnoFabrico(parseInt(campoAno.getText(), "Ano de fabrico"));
            navio.setEstado(comboEstado.getValue());
            navio.setPortoAtualId(comboPorto.getValue().getId());

            if (selecionado == null) {
                navioService.registarNavio(navio);
                Dialogs.info("Navio criado com sucesso.");
            } else {
                navioService.editarNavio(navio);
                Dialogs.info("Navio atualizado com sucesso.");
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
        Navio sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione um navio na tabela para apagar."); return; }
        if (!Dialogs.confirmar("Apagar o navio \"" + sel.getNome() + "\"?")) return;
        try {
            navioService.apagarNavio(sel.getId());
            Dialogs.info("Navio apagado.");
            novo();
            recarregar();
        } catch (Exception e) {
            Dialogs.erro("Erro ao apagar: " + e.getMessage());
        }
    }

    private void preencher(Navio n) {
        selecionado = n;
        if (n == null) return;
        campoNome.setText(n.getNome());
        campoIMO.setText(n.getCodigoIMO());
        comboTipo.setValue(n.getTipo());
        campoCapacidade.setText(String.valueOf(n.getCapacidadeMaxima()));
        campoTanques.setText(String.valueOf(n.getNumTanques()));
        campoBandeira.setText(n.getBandeira());
        campoAno.setText(String.valueOf(n.getAnoFabrico()));
        comboEstado.setValue(n.getEstado());
        comboPorto.setValue(portosPorId.get(n.getPortoAtualId()));
        labelMensagem.setText("A editar: " + n.getNome());
    }

    private void limpar() {
        campoNome.clear();
        campoIMO.clear();
        campoCapacidade.clear();
        campoTanques.clear();
        campoBandeira.clear();
        campoAno.clear();
        comboTipo.setValue(null);
        comboEstado.setValue(null);
        comboPorto.setValue(null);
    }

    // ───── metodos auxiliares ─────
    private String texto(Object o) { return o == null ? "" : o.toString(); }

    private String nomePorto(int id) {
        Porto p = portosPorId.get(id);
        return p == null ? "-" : p.getNome();
    }

    private double parseDouble(String s, String campo) {
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um numero."); }
    }

    private int parseInt(String s, String campo) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um numero inteiro."); }
    }
}
