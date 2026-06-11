package com.dias.navios.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicacao — arranca-se por aqui (MainApp.main).
 *
 * IMPORTANTE: a classe que tem o main NAO estende Application de proposito.
 * Se estendesse, o JavaFX exigiria estar no "module-path" e dava o erro
 * "JavaFX runtime components are missing". Como o main esta numa classe normal
 * e a parte JavaFX esta na classe interna JavaFxApp, a aplicacao arranca
 * a partir do classpath (as dependencias do Maven).
 */
public class MainApp {

    public static void main(String[] args) {
        Application.launch(JavaFxApp.class, args);
    }

    /** Classe JavaFX propriamente dita: constroi e mostra a janela principal. */
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
