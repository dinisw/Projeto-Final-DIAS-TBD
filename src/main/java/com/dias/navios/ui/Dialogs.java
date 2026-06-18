package com.dias.navios.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Pequena classe utilitaria para mostrar caixas de dialogo (Alert) de forma
 * consistente em toda a aplicacao. Evita repetir o mesmo codigo em cada controller.
 */
public final class Dialogs {

    private Dialogs() { } // classe so com metodos estaticos — nao se instancia

    /** Mensagem de sucesso/informacao. */
    public static void info(String mensagem) {
        mostrar(AlertType.INFORMATION, "Informacao", mensagem);
    }

    /** Mensagem de erro. */
    public static void erro(String mensagem) {
        mostrar(AlertType.ERROR, "Erro", mensagem);
    }

    /** Pergunta de confirmacao. Devolve true se o utilizador carregar em OK. */
    public static boolean confirmar(String mensagem) {
        Alert alert = new Alert(AlertType.CONFIRMATION, mensagem, ButtonType.OK, ButtonType.CANCEL);
        alert.setTitle("Confirmacao");
        alert.setHeaderText(null);
        Optional<ButtonType> resposta = alert.showAndWait();
        return resposta.isPresent() && resposta.get() == ButtonType.OK;
    }

    private static void mostrar(AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo, mensagem);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
