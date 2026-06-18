package com.dias.navios.ui.controller;

import com.dias.navios.bll.PortoService;
import com.dias.navios.model.Porto;
import com.dias.navios.ui.Dialogs;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PortoController {

    @FXML private TableView<Porto> tabela;
    @FXML private TableColumn<Porto, String> colNome;
    @FXML private TableColumn<Porto, String> colPais;
    @FXML private TableColumn<Porto, String> colCodigo;

    @FXML private TextField campoNome;
    @FXML private TextField campoPais;
    @FXML private TextField campoCodigo;
    @FXML private Label labelMensagem;

    private final PortoService portoService = new PortoService();
    private Porto selecionado;   // null => estamos a criar um novo

    @FXML
    public void initialize() {
        // Ligar cada coluna ao atributo correspondente do objeto Porto
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colPais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPais()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo()));

        // Ao clicar numa linha, carrega os dados no formulario
        tabela.getSelectionModel().selectedItemProperty()
                .addListener((obs, antigo, novo) -> preencher(novo));

        recarregar();
    }

    private void recarregar() {
        try {
            tabela.setItems(FXCollections.observableArrayList(portoService.listar()));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar: " + e.getMessage());
        }
    }

    @FXML
    public void novo() {
        tabela.getSelectionModel().clearSelection();
        selecionado = null;
        limpar();
        labelMensagem.setText("A criar um novo porto.");
    }

    @FXML
    public void guardar() {
        try {
            Porto porto = (selecionado == null) ? new Porto() : selecionado;
            porto.setNome(campoNome.getText());
            porto.setPais(campoPais.getText());
            porto.setCodigo(campoCodigo.getText());

            if (selecionado == null) {
                portoService.registar(porto);
                Dialogs.info("Porto criado com sucesso.");
            } else {
                portoService.editar(porto);
                Dialogs.info("Porto atualizado com sucesso.");
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
        Porto sel = tabela.getSelectionModel().getSelectedItem();
        if (sel == null) { Dialogs.erro("Selecione um porto para apagar."); return; }
        if (!Dialogs.confirmar("Apagar o porto \"" + sel.getNome() + "\"?")) return;
        try {
            portoService.apagar(sel.getId());
            Dialogs.info("Porto apagado.");
            novo();
            recarregar();
        } catch (Exception e) {
            Dialogs.erro("Erro ao apagar: " + e.getMessage());
        }
    }

    private void preencher(Porto p) {
        selecionado = p;
        if (p == null) return;
        campoNome.setText(p.getNome());
        campoPais.setText(p.getPais());
        campoCodigo.setText(p.getCodigo());
        labelMensagem.setText("A editar: " + p.getNome());
    }

    private void limpar() {
        campoNome.clear();
        campoPais.clear();
        campoCodigo.clear();
    }
}
