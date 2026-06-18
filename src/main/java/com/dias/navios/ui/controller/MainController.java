package com.dias.navios.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainController {

    @FXML
    private BorderPane rootPane;

    @FXML
    public void abrirNavios() throws Exception {
        rootPane.setCenter(FXMLLoader.load(getClass().getResource("/fxml/navios.fxml")));
    }

    @FXML
    public void abrirCargas() throws Exception {
        rootPane.setCenter(FXMLLoader.load(getClass().getResource("/fxml/cargas.fxml")));
    }

    @FXML
    public void abrirViagens() throws Exception {
        rootPane.setCenter(FXMLLoader.load(getClass().getResource("/fxml/viagens.fxml")));
    }

    @FXML
    public void abrirTripulacao() throws Exception {
        rootPane.setCenter(FXMLLoader.load(getClass().getResource("/fxml/tripulantes.fxml")));
    }

    @FXML
    public void abrirPortos() throws Exception {
        rootPane.setCenter(FXMLLoader.load(getClass().getResource("/fxml/portos.fxml")));
    }
}
