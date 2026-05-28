package com.dias.navios.ui.controller;

import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.Viagem;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ViagemController {

    @FXML private TableView<Viagem> tabelaViagens;
    @FXML private TextField campoNavioId;
    @FXML private TextField campoPortoOrigemId;
    @FXML private TextField campoPortoDestinoId;
    @FXML private DatePicker campoDataPartida;
    @FXML private DatePicker campoDataChegada;
    @FXML private Label labelMensagem;

    private ViagemService viagemService = new ViagemService();

    @FXML
    public void initialize() {
        carregarTabela();
    }

    private void carregarTabela() {
        try {
            List<Viagem> viagens = viagemService.listarViagens();
            tabelaViagens.setItems(FXCollections.observableArrayList(viagens));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar viagens: " + e.getMessage());
        }
    }

    @FXML
    public void criarViagem() {
        // TODO: ler os campos e chamar viagemService.criarViagem()
        try {
            Viagem viagem = new Viagem();
            viagem.setNavioId(Integer.parseInt(campoNavioId.getText()));
            viagem.setPortoOrigemId(Integer.parseInt(campoPortoOrigemId.getText()));
            viagem.setPortoDestinoId(Integer.parseInt(campoPortoDestinoId.getText()));
            viagem.setDataPartida(campoDataPartida.getValue());
            viagem.setDataChegadaPrevista(campoDataChegada.getValue());

            viagemService.criarViagem(viagem);
            labelMensagem.setText("Viagem criada com sucesso.");
            carregarTabela();
        } catch (IllegalStateException e) {
            labelMensagem.setText("Regra de negocio: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void avancarEstado() {
        // TODO: avancar o estado da viagem selecionada
        Viagem selecionada = tabelaViagens.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Selecione uma viagem.");
            return;
        }
        try {
            viagemService.avancarEstado(selecionada.getId());
            labelMensagem.setText("Estado avancado com sucesso.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarViagem() {
        // TODO: cancelar a viagem selecionada
        Viagem selecionada = tabelaViagens.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Selecione uma viagem.");
            return;
        }
        try {
            viagemService.cancelarViagem(selecionada.getId());
            labelMensagem.setText("Viagem cancelada.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }
}
