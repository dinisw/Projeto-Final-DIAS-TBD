package com.dias.navios.ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class MainController {

    @FXML
    private BorderPane rootPane;

    // Navega para o modulo de Navios
    @FXML
    public void abrirNavios() throws Exception {
        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/navios.fxml"));
        rootPane.setCenter(pane);
    }

    // Navega para o modulo de Cargas
    @FXML
    public void abrirCargas() throws Exception {
        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/cargas.fxml"));
        rootPane.setCenter(pane);
    }

    // Navega para o modulo de Viagens
    @FXML
    public void abrirViagens() throws Exception {
        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/viagens.fxml"));
        rootPane.setCenter(pane);
    }

    // Navega para o modulo de Tripulacao
    @FXML
    public void abrirTripulacao() throws Exception {
        Pane pane = FXMLLoader.load(getClass().getResource("/fxml/tripulantes.fxml"));
        rootPane.setCenter(pane);
    }
}
