package com.dias.navios.ui.controller;

import com.dias.navios.ui.Dialogs;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

/**
 * Controller da janela principal. Trata apenas da NAVEGACAO:
 * cada botao da barra lateral carrega o respetivo modulo (.fxml)
 * para o centro do BorderPane.
 */
public class MainController {

    @FXML private BorderPane rootPane;

    @FXML public void abrirNavios()     { carregar("/fxml/navios.fxml"); }
    @FXML public void abrirCargas()     { carregar("/fxml/cargas.fxml"); }
    @FXML public void abrirViagens()    { carregar("/fxml/viagens.fxml"); }
    @FXML public void abrirTripulacao() { carregar("/fxml/tripulantes.fxml"); }
    @FXML public void abrirPortos()     { carregar("/fxml/portos.fxml"); }

    private void carregar(String caminhoFxml) {
        try {
            Parent modulo = FXMLLoader.load(getClass().getResource(caminhoFxml));
            rootPane.setCenter(modulo);
        } catch (Exception e) {
            Dialogs.erro("Nao foi possivel abrir o modulo:\n" + e.getMessage());
        }
    }
}
