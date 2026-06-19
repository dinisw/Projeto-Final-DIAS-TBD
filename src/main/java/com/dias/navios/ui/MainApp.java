package com.dias.navios.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Aplicação JavaFX principal.
 * Não tem método main — o único entry point é Launcher.main().
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 1100, 720);

        primaryStage.setTitle("Sistema de Gestão de Navios Petroleiros");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
