package com.dias.navios.ui;

import javafx.application.Application;

/**
 * Ponto de entrada da aplicação.
 * Não estende Application — evita a verificação prévia de módulos JavaFX
 * que causaria "JavaFX runtime components are missing" ao correr pelo classpath.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
