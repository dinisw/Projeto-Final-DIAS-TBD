package com.dias.navios.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp {

    public static void main(String[] args) {
        Application.launch(JavaFxApp.class, args);
    }

    public static class JavaFxApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            BorderPane root = loader.load();

            Scene scene = new Scene(root, 1024, 680);

            primaryStage.setTitle("Sistema de Gestao de Navios Petroleiros");
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}
