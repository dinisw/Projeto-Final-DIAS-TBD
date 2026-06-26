package com.dias.navios.ui;

import com.dias.navios.model.*;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Fábrica de diálogos de formulário.
 * Cada método abre um Dialog JavaFX modal, valida campos em tempo real
 * e devolve Optional com o objecto preenchido (ou empty se cancelado).
 */
public final class FormDialogs {

    private FormDialogs() {}

    // ─────────────────────────────────────────────────────────────────────────
    // Porto
    // ─────────────────────────────────────────────────────────────────────────

    public static Optional<Porto> mostrarPorto(Porto existente) {
        boolean isNovo = (existente == null);

        Dialog<Porto> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Novo Porto" : "Editar Porto");
        dialog.setHeaderText(isNovo
                ? "Preencha os dados do porto."
                : "A editar: " + existente.getNome() + " (" + existente.getCodigo() + ")");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(400);

        GridPane grid = grid();

        TextField campoNome   = field(isNovo ? "" : existente.getNome(),   "ex: Porto de Leixões");
        TextField campoPais   = field(isNovo ? "" : existente.getPais(),   "ex: Portugal");
        TextField campoCodigo = field(isNovo ? "" : existente.getCodigo(), "ex: PTLEI");
        campoCodigo.setPrefWidth(110);

        grid.add(lbl("Nome:"),              0, 0); grid.add(campoNome,   1, 0);
        grid.add(lbl("País:"),              0, 1); grid.add(campoPais,   1, 1);
        grid.add(lbl("Código UNLOCODE:"),   0, 2); grid.add(campoCodigo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Node btnOk = dialog.getDialogPane().lookupButton(btnGuardar);
        Runnable validar = () -> btnOk.setDisable(
                campoNome.getText().isBlank() ||
                campoPais.getText().isBlank() ||
                campoCodigo.getText().isBlank());
        campoNome.textProperty().addListener((o, a, n) -> validar.run());
        campoPais.textProperty().addListener((o, a, n) -> validar.run());
        campoCodigo.textProperty().addListener((o, a, n) -> validar.run());
        validar.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            Porto p = new Porto();
            p.setNome(campoNome.getText().trim());
            p.setPais(campoPais.getText().trim());
            p.setCodigo(campoCodigo.getText().trim().toUpperCase());
            return p;
        });

        return dialog.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Navio
    // ─────────────────────────────────────────────────────────────────────────

    public static Optional<Navio> mostrarNavio(Navio existente, List<Porto> portos) {
        boolean isNovo = (existente == null);

        Dialog<Navio> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Novo Navio" : "Editar Navio");
        dialog.setHeaderText(isNovo
                ? "Preencha os dados do navio."
                : "A editar: " + existente.getNome() + " [" + existente.getCodigoIMO() + "]");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(540);

        GridPane grid = grid();

        TextField campoNome       = field(isNovo ? "" : existente.getNome(),                        "ex: NP Atlântico");
        TextField campoIMO        = field(isNovo ? "" : existente.getCodigoIMO(),                   "ex: IMO1234567");
        TextField campoCapacidade = field(isNovo ? "" : fmtDouble(existente.getCapacidadeMaxima()), "ex: 80000");
        TextField campoTanques    = field(isNovo ? "" : String.valueOf(existente.getNumTanques()),   "ex: 12");
        TextField campoBandeira   = field(isNovo ? "" : existente.getBandeira(),                    "ex: Portugal");
        TextField campoAno        = field(isNovo ? "" : String.valueOf(existente.getAnoFabrico()),  "ex: 2005");

        ComboBox<TipoNavio>   comboTipo   = combo(TipoNavio.values(),   isNovo ? null : existente.getTipo(),   "Selecione o tipo…");
        ComboBox<EstadoNavio> comboEstado = combo(EstadoNavio.values(), isNovo ? EstadoNavio.ATIVO : existente.getEstado(), null);

        ComboBox<Porto> comboPorto = new ComboBox<>();
        comboPorto.getItems().setAll(portos);
        comboPorto.setConverter(converterPorto());
        comboPorto.setPromptText("Porto atual…");
        comboPorto.setMaxWidth(Double.MAX_VALUE);
        if (!isNovo && existente.getPortoAtualId() > 0)
            portos.stream().filter(p -> p.getId() == existente.getPortoAtualId())
                    .findFirst().ifPresent(comboPorto::setValue);

        // 2 colunas: label | campo | label | campo
        grid.add(lbl("Nome:"),           0, 0); grid.add(campoNome,       1, 0);
        grid.add(lbl("Código IMO:"),     2, 0); grid.add(campoIMO,        3, 0);
        grid.add(lbl("Tipo:"),           0, 1); grid.add(comboTipo,       1, 1);
        grid.add(lbl("Estado:"),         2, 1); grid.add(comboEstado,     3, 1);
        grid.add(lbl("Capacidade (t):"), 0, 2); grid.add(campoCapacidade, 1, 2);
        grid.add(lbl("Nº Tanques:"),     2, 2); grid.add(campoTanques,    3, 2);
        grid.add(lbl("Bandeira:"),       0, 3); grid.add(campoBandeira,   1, 3);
        grid.add(lbl("Ano de Fabrico:"), 2, 3); grid.add(campoAno,        3, 3);
        grid.add(lbl("Porto Atual:"),    0, 4);
        GridPane.setColumnSpan(comboPorto, 3);
        grid.add(comboPorto, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Node btnOk = dialog.getDialogPane().lookupButton(btnGuardar);
        Runnable validar = () -> btnOk.setDisable(
                campoNome.getText().isBlank() || campoIMO.getText().isBlank() ||
                comboTipo.getValue() == null  || comboEstado.getValue() == null ||
                campoCapacidade.getText().isBlank() || campoTanques.getText().isBlank() ||
                campoBandeira.getText().isBlank()   || campoAno.getText().isBlank());
        campoNome.textProperty().addListener((o, a, n) -> validar.run());
        campoIMO.textProperty().addListener((o, a, n) -> validar.run());
        comboTipo.valueProperty().addListener((o, a, n) -> validar.run());
        comboEstado.valueProperty().addListener((o, a, n) -> validar.run());
        campoCapacidade.textProperty().addListener((o, a, n) -> validar.run());
        campoTanques.textProperty().addListener((o, a, n) -> validar.run());
        campoBandeira.textProperty().addListener((o, a, n) -> validar.run());
        campoAno.textProperty().addListener((o, a, n) -> validar.run());
        validar.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            try {
                Navio n = new Navio();
                n.setNome(campoNome.getText().trim());
                n.setCodigoIMO(campoIMO.getText().trim());
                n.setTipo(comboTipo.getValue());
                n.setEstado(comboEstado.getValue());
                n.setCapacidadeMaxima(parseDouble(campoCapacidade.getText(), "Capacidade"));
                n.setNumTanques(parseInt(campoTanques.getText(), "Nº Tanques"));
                n.setBandeira(campoBandeira.getText().trim());
                n.setAnoFabrico(parseInt(campoAno.getText(), "Ano de Fabrico"));
                n.setPortoAtualId(comboPorto.getValue() != null ? comboPorto.getValue().getId() : 0);
                return n;
            } catch (IllegalArgumentException e) {
                Dialogs.erro(e.getMessage());
                return null;
            }
        });

        return dialog.showAndWait().filter(Objects::nonNull);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Carga
    // ─────────────────────────────────────────────────────────────────────────

    public static Optional<Carga> mostrarCarga(Carga existente, List<Porto> portos) {
        boolean isNovo = (existente == null);

        Dialog<Carga> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Nova Carga" : "Editar Carga");
        dialog.setHeaderText(isNovo
                ? "Preencha os dados da carga."
                : "A editar: " + existente.getDesignacao() + " [" + existente.getTipo() + "]");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(520);

        GridPane grid = grid();

        TextField campoDesignacao = field(isNovo ? "" : existente.getDesignacao(), "ex: Crude Árabe Leve");
        TextField campoVolume     = field(isNovo ? "" : fmtDouble(existente.getVolume()), "m³");
        TextField campoPeso       = field(isNovo ? "" : fmtDouble(existente.getPeso()),   "toneladas");

        ComboBox<TipoCarga> comboTipo = new ComboBox<>();
        comboTipo.getItems().setAll(TipoCarga.values());
        comboTipo.setValue(isNovo ? null : existente.getTipo());
        comboTipo.setPromptText("Tipo de carga…");
        comboTipo.setMaxWidth(Double.MAX_VALUE);

        // Propriedades derivadas do tipo — apenas visualização (F4)
        CheckBox checkInfl = new CheckBox("Inflamável");
        CheckBox checkCorr = new CheckBox("Corrosiva");
        CheckBox checkTox  = new CheckBox("Tóxica");
        checkInfl.setDisable(true);
        checkCorr.setDisable(true);
        checkTox.setDisable(true);
        comboTipo.valueProperty().addListener((o, a, tipo) -> {
            checkInfl.setSelected(tipo != null && tipo.isInflamavel());
            checkCorr.setSelected(tipo != null && tipo.isCorrosiva());
            checkTox.setSelected(tipo != null && tipo.isToxica());
        });
        if (!isNovo && existente.getTipo() != null) {
            checkInfl.setSelected(existente.getTipo().isInflamavel());
            checkCorr.setSelected(existente.getTipo().isCorrosiva());
            checkTox.setSelected(existente.getTipo().isToxica());
        }

        StringConverter<Porto> convPorto = converterPorto();
        ComboBox<Porto> comboCarga    = new ComboBox<>();
        comboCarga.getItems().setAll(portos);
        comboCarga.setConverter(convPorto);
        comboCarga.setPromptText("Porto de carga…");
        comboCarga.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Porto> comboDescarga = new ComboBox<>();
        comboDescarga.getItems().setAll(portos);
        comboDescarga.setConverter(convPorto);
        comboDescarga.setPromptText("Porto de descarga…");
        comboDescarga.setMaxWidth(Double.MAX_VALUE);

        if (!isNovo) {
            portos.stream().filter(p -> p.getId() == existente.getPortoCarregamentoId())
                    .findFirst().ifPresent(comboCarga::setValue);
            portos.stream().filter(p -> p.getId() == existente.getPortoDescargaId())
                    .findFirst().ifPresent(comboDescarga::setValue);
        }

        HBox hboxProps = new HBox(16, checkInfl, checkCorr, checkTox);

        grid.add(lbl("Designação:"),       0, 0); grid.add(campoDesignacao, 1, 0);
        grid.add(lbl("Tipo de Carga:"),    2, 0); grid.add(comboTipo,       3, 0);
        grid.add(lbl("Volume (m³):"),      0, 1); grid.add(campoVolume,     1, 1);
        grid.add(lbl("Peso (t):"),         2, 1); grid.add(campoPeso,       3, 1);
        grid.add(lbl("Propriedades:"),     0, 2);
        GridPane.setColumnSpan(hboxProps, 3);
        grid.add(hboxProps, 1, 2);
        grid.add(lbl("Porto de Carga:"),   0, 3); grid.add(comboCarga,      1, 3);
        grid.add(lbl("Porto Descarga:"),   2, 3); grid.add(comboDescarga,   3, 3);

        dialog.getDialogPane().setContent(grid);

        Node btnOk = dialog.getDialogPane().lookupButton(btnGuardar);
        Runnable validar = () -> btnOk.setDisable(
                campoDesignacao.getText().isBlank() || comboTipo.getValue() == null ||
                campoVolume.getText().isBlank()     || campoPeso.getText().isBlank() ||
                comboCarga.getValue() == null       || comboDescarga.getValue() == null);
        campoDesignacao.textProperty().addListener((o, a, n) -> validar.run());
        comboTipo.valueProperty().addListener((o, a, n) -> validar.run());
        campoVolume.textProperty().addListener((o, a, n) -> validar.run());
        campoPeso.textProperty().addListener((o, a, n) -> validar.run());
        comboCarga.valueProperty().addListener((o, a, n) -> validar.run());
        comboDescarga.valueProperty().addListener((o, a, n) -> validar.run());
        validar.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            try {
                TipoCarga tipo = comboTipo.getValue();
                Carga c = new Carga();
                c.setDesignacao(campoDesignacao.getText().trim());
                c.setTipo(tipo);
                c.setInflamavel(tipo.isInflamavel());
                c.setCorrosiva(tipo.isCorrosiva());
                c.setToxica(tipo.isToxica());
                c.setVolume(parseDouble(campoVolume.getText(), "Volume"));
                c.setPeso(parseDouble(campoPeso.getText(), "Peso"));
                c.setPortoCarregamentoId(comboCarga.getValue().getId());
                c.setPortoDescargaId(comboDescarga.getValue().getId());
                return c;
            } catch (IllegalArgumentException e) {
                Dialogs.erro(e.getMessage());
                return null;
            }
        });

        return dialog.showAndWait().filter(Objects::nonNull);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Tripulante
    // ─────────────────────────────────────────────────────────────────────────

    public static Optional<Tripulante> mostrarTripulante(Tripulante existente) {
        boolean isNovo = (existente == null);

        Dialog<Tripulante> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Novo Tripulante" : "Editar Tripulante");
        dialog.setHeaderText(isNovo
                ? "Preencha os dados do tripulante."
                : "A editar: " + existente.getNome() + " [" + existente.getFuncao() + "]");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(500);

        GridPane grid = grid();

        TextField campoNome  = field(isNovo ? "" : existente.getNome(),               "Nome completo");
        TextField campoCert  = field(isNovo ? "" : existente.getNumeroCertificado(),   "ex: CERT-001");
        TextField campoEmail = field(isNovo ? "" : existente.getEmail(),               "ex: joao@example.com");

        ComboBox<FuncaoTripulante> comboFuncao = combo(
                FuncaoTripulante.values(),
                isNovo ? null : existente.getFuncao(),
                "Selecione a função…");

        javafx.scene.control.DatePicker campoDN = new javafx.scene.control.DatePicker(
                isNovo ? null : existente.getDataNascimento());
        campoDN.setPromptText("dd/mm/aaaa");
        campoDN.setMaxWidth(Double.MAX_VALUE);

        grid.add(lbl("Nome:"),              0, 0); grid.add(campoNome,   1, 0);
        grid.add(lbl("Nº Certificado:"),    2, 0); grid.add(campoCert,   3, 0);
        grid.add(lbl("Função:"),            0, 1); grid.add(comboFuncao, 1, 1);
        grid.add(lbl("Email:"),             2, 1); grid.add(campoEmail,  3, 1);
        grid.add(lbl("Data Nascimento:"),   0, 2); grid.add(campoDN,     1, 2);

        dialog.getDialogPane().setContent(grid);

        Node btnOk = dialog.getDialogPane().lookupButton(btnGuardar);
        Runnable validar = () -> btnOk.setDisable(
                campoNome.getText().isBlank() || campoCert.getText().isBlank() ||
                campoEmail.getText().isBlank() || comboFuncao.getValue() == null ||
                campoDN.getValue() == null);
        campoNome.textProperty().addListener((o, a, n) -> validar.run());
        campoCert.textProperty().addListener((o, a, n) -> validar.run());
        campoEmail.textProperty().addListener((o, a, n) -> validar.run());
        comboFuncao.valueProperty().addListener((o, a, n) -> validar.run());
        campoDN.valueProperty().addListener((o, a, n) -> validar.run());
        validar.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            Tripulante t = new Tripulante();
            t.setNome(campoNome.getText().trim());
            t.setNumeroCertificado(campoCert.getText().trim());
            t.setEmail(campoEmail.getText().trim());
            t.setFuncao(comboFuncao.getValue());
            t.setDataNascimento(campoDN.getValue());
            t.setEstadoDisponibilidade(isNovo ? "DISPONIVEL" : existente.getEstadoDisponibilidade());
            return t;
        });

        return dialog.showAndWait();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Viagem (só campos principais — cargas/tripulantes ficam na vista principal)
    // ─────────────────────────────────────────────────────────────────────────

    public static Optional<Viagem> mostrarViagem(Viagem existente, List<Navio> navios, List<Porto> portos) {
        boolean isNovo = (existente == null);

        Dialog<Viagem> dialog = new Dialog<>();
        dialog.setTitle(isNovo ? "Nova Viagem" : "Editar Viagem");
        dialog.setHeaderText(isNovo
                ? "Preencha os dados da viagem."
                : "A editar: Viagem #" + existente.getId() + "  [" + existente.getEstado() + "]");

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dialog.getDialogPane().setMinWidth(540);

        GridPane grid = grid();

        ComboBox<Navio> comboNavio = new ComboBox<>();
        comboNavio.getItems().setAll(navios);
        comboNavio.setConverter(new StringConverter<>() {
            @Override public String toString(Navio n)  { return n == null ? "" : n.getNome() + "  (" + n.getTipo() + ")"; }
            @Override public Navio fromString(String s) { return null; }
        });
        comboNavio.setPromptText("Selecione o navio…");
        comboNavio.setMaxWidth(Double.MAX_VALUE);

        StringConverter<Porto> convPorto = converterPorto();

        ComboBox<Porto> comboOrigem = new ComboBox<>();
        comboOrigem.getItems().setAll(portos);
        comboOrigem.setConverter(convPorto);
        comboOrigem.setPromptText("Porto de origem…");
        comboOrigem.setMaxWidth(Double.MAX_VALUE);

        ComboBox<Porto> comboDestino = new ComboBox<>();
        comboDestino.getItems().setAll(portos);
        comboDestino.setConverter(convPorto);
        comboDestino.setPromptText("Porto de destino…");
        comboDestino.setMaxWidth(Double.MAX_VALUE);

        javafx.scene.control.DatePicker dpPartida = new javafx.scene.control.DatePicker();
        dpPartida.setPromptText("Data de partida");
        dpPartida.setMaxWidth(Double.MAX_VALUE);
        javafx.scene.control.DatePicker dpChegada = new javafx.scene.control.DatePicker();
        dpChegada.setPromptText("Data de chegada prevista (opcional)");
        dpChegada.setMaxWidth(Double.MAX_VALUE);

        if (!isNovo) {
            navios.stream().filter(n -> n.getId() == existente.getNavioId()).findFirst().ifPresent(comboNavio::setValue);
            portos.stream().filter(p -> p.getId() == existente.getPortoOrigemId()).findFirst().ifPresent(comboOrigem::setValue);
            portos.stream().filter(p -> p.getId() == existente.getPortoDestinoId()).findFirst().ifPresent(comboDestino::setValue);
            dpPartida.setValue(existente.getDataPartida());
            dpChegada.setValue(existente.getDataChegadaPrevista());
        }

        GridPane.setColumnSpan(comboNavio, 3);
        grid.add(lbl("Navio:"),             0, 0); grid.add(comboNavio,   1, 0);
        grid.add(lbl("Porto de Origem:"),   0, 1); grid.add(comboOrigem,  1, 1);
        grid.add(lbl("Porto de Destino:"),  2, 1); grid.add(comboDestino, 3, 1);
        grid.add(lbl("Data de Partida:"),   0, 2); grid.add(dpPartida,    1, 2);
        grid.add(lbl("Chegada Prevista:"),  2, 2); grid.add(dpChegada,    3, 2);

        dialog.getDialogPane().setContent(grid);

        Node btnOk = dialog.getDialogPane().lookupButton(btnGuardar);
        Runnable validar = () -> btnOk.setDisable(
                comboNavio.getValue() == null || comboOrigem.getValue() == null ||
                comboDestino.getValue() == null || dpPartida.getValue() == null);
        comboNavio.valueProperty().addListener((o, a, n) -> validar.run());
        comboOrigem.valueProperty().addListener((o, a, n) -> validar.run());
        comboDestino.valueProperty().addListener((o, a, n) -> validar.run());
        dpPartida.valueProperty().addListener((o, a, n) -> validar.run());
        validar.run();

        dialog.setResultConverter(bt -> {
            if (bt != btnGuardar) return null;
            // validação de negócio inline
            if (comboOrigem.getValue().getId() == comboDestino.getValue().getId()) {
                Dialogs.erro("A origem e o destino não podem ser o mesmo porto.");
                return null;
            }
            if (dpChegada.getValue() != null && dpChegada.getValue().isBefore(dpPartida.getValue())) {
                Dialogs.erro("A data de chegada não pode ser anterior à de partida.");
                return null;
            }
            Viagem v = new Viagem();
            if (!isNovo) v.setId(existente.getId());
            v.setNavioId(comboNavio.getValue().getId());
            v.setPortoOrigemId(comboOrigem.getValue().getId());
            v.setPortoDestinoId(comboDestino.getValue().getId());
            v.setDataPartida(dpPartida.getValue());
            v.setDataChegadaPrevista(dpChegada.getValue());
            return v;
        });

        return dialog.showAndWait().filter(Objects::nonNull);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilitários internos
    // ─────────────────────────────────────────────────────────────────────────

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(20, 24, 10, 24));
        return g;
    }

    private static Label lbl(String texto) {
        return new Label(texto);
    }

    private static TextField field(String valor, String prompt) {
        TextField tf = new TextField(valor);
        tf.setPromptText(prompt);
        tf.setPrefWidth(160);
        return tf;
    }

    private static <T> ComboBox<T> combo(T[] values, T selected, String prompt) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().setAll(values);
        cb.setValue(selected);
        if (prompt != null) cb.setPromptText(prompt);
        cb.setMaxWidth(Double.MAX_VALUE);
        return cb;
    }

    private static StringConverter<Porto> converterPorto() {
        return new StringConverter<>() {
            @Override public String toString(Porto p)   { return p == null ? "" : p.getNome() + "  (" + p.getCodigo() + ")"; }
            @Override public Porto fromString(String s) { return null; }
        };
    }

    private static double parseDouble(String s, String campo) {
        try { return Double.parseDouble(s.trim().replace(",", ".")); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um número."); }
    }

    private static int parseInt(String s, String campo) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { throw new IllegalArgumentException("O campo \"" + campo + "\" deve ser um número inteiro."); }
    }

    private static String fmtDouble(double v) {
        // evita ".0" desnecessário
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }
}
