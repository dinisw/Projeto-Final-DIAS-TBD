package com.dias.navios.ui;

import atlantafx.base.theme.NordDark;
import com.dias.navios.dal.db.DatabaseConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Tema inicial — pode ser alterado em runtime pelo seletor no menu
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());

        // Acorda a BD em background enquanto a UI carrega
        Thread warmUp = new Thread(() -> DatabaseConnection.getInstance().warmUp());
        warmUp.setDaemon(true);
        warmUp.start();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        BorderPane root = loader.load();

        Scene scene = new Scene(root, 1100, 720);

        primaryStage.setTitle("Sistema de Gestão de Navios Petroleiros");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Fecha a ligacao partilhada a BD quando a aplicacao termina (ver F5).
        DatabaseConnection.getInstance().disconnect();
    }
}
