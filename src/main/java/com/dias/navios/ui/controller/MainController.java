package com.dias.navios.ui.controller;

import atlantafx.base.controls.Card;
import atlantafx.base.theme.*;
import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.TripulanteService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.ui.Dialogs;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MainController {

    @FXML private BorderPane rootPane;
    @FXML private ComboBox<String> comboTema;
    @FXML private HBox cardsPane;

    private static final String[][] TEMAS = {
        { "Nord Dark",       new NordDark().getUserAgentStylesheet()       },
        { "Nord Light",      new NordLight().getUserAgentStylesheet()      },
        { "Primer Dark",     new PrimerDark().getUserAgentStylesheet()     },
        { "Primer Light",    new PrimerLight().getUserAgentStylesheet()    },
        { "Cupertino Dark",  new CupertinoDark().getUserAgentStylesheet()  },
        { "Cupertino Light", new CupertinoLight().getUserAgentStylesheet() },
        { "Dracula",         new Dracula().getUserAgentStylesheet()        },
    };

    private final NavioService      navioService      = new NavioService();
    private final PortoService      portoService      = new PortoService();
    private final ViagemService     viagemService     = new ViagemService();
    private final TripulanteService tripulanteService = new TripulanteService();

    @FXML
    public void initialize() {
        for (String[] tema : TEMAS) comboTema.getItems().add(tema[0]);
        comboTema.setValue("Nord Dark");
        comboTema.setOnAction(e -> aplicarTema(comboTema.getValue()));

        carregarCards();
    }

    private void aplicarTema(String nome) {
        for (String[] tema : TEMAS) {
            if (tema[0].equals(nome)) {
                Application.setUserAgentStylesheet(tema[1]);
                break;
            }
        }
    }

    private void carregarCards() {
        Thread t = new Thread(() -> {
            try {
                int navios      = navioService.listarNavios().size();
                int portos      = portoService.listarPortos().size();
                int viagens     = viagemService.listarViagens().size();
                int tripulantes = tripulanteService.listarTripulantes().size();

                Platform.runLater(() -> {
                    cardsPane.getChildren().clear();
                    cardsPane.getChildren().addAll(
                        criarCard("🚢", "Navios",      navios),
                        criarCard("⚓", "Portos",      portos),
                        criarCard("🗺", "Viagens",     viagens),
                        criarCard("👤", "Tripulantes", tripulantes)
                    );
                });
            } catch (Exception ignored) {
                // BD pode não estar disponível ainda — cards ficam vazios
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private Card criarCard(String emoji, String titulo, int total) {
        Label icone = new Label(emoji);
        icone.setFont(new Font(28));

        Label numero = new Label(String.valueOf(total));
        numero.setFont(new Font(32));

        Label nome = new Label(titulo);
        nome.setFont(new Font(12));

        VBox corpo = new VBox(4, icone, numero, nome);
        corpo.setAlignment(Pos.CENTER);
        corpo.setPadding(new Insets(16, 24, 16, 24));
        corpo.setMinWidth(110);

        Card card = new Card();
        card.setBody(corpo);
        return card;
    }

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
