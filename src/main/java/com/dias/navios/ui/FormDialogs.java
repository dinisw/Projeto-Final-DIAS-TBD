package com.dias.navios.ui;

import atlantafx.base.controls.ToggleSwitch;
import com.dias.navios.model.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fábrica de diálogos modais para criação/edição de entidades.
 * Todos os métodos são estáticos e retornam Optional<T>.
 */
public class FormDialogs {

    // ── Porto ─────────────────────────────────────────────────────────────────

    public static Optional<Porto> mostrarPorto(Porto existente) {
        Dialog<Porto> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Novo Porto" : "Editar Porto");
        dlg.setHeaderText(existente == null ? "⚓  Criar novo porto" : "⚓  Editar porto");
        dlg.getDialogPane().setMinWidth(360);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane g = grid();
        TextField fNome   = field(existente != null ? existente.getNome()   : "", "Nome do porto");
        TextField fPais   = field(existente != null ? existente.getPais()   : "", "País");
        TextField fCodigo = field(existente != null ? existente.getCodigo() : "", "Código (ex: PTLEI)");

        g.add(lbl("Nome:"),   0, 0); g.add(fNome,   1, 0);
        g.add(lbl("País:"),   0, 1); g.add(fPais,   1, 1);
        g.add(lbl("Código:"), 0, 2); g.add(fCodigo, 1, 2);
        dlg.getDialogPane().setContent(g);

        javafx.scene.Node okBtn = dlg.getDialogPane().lookupButton(btnOk);
        okBtn.setDisable(true);
        Runnable validar = () -> okBtn.setDisable(
                fNome.getText().isBlank() || fPais.getText().isBlank() || fCodigo.getText().isBlank());
        fNome.textProperty().addListener((o, a, n) -> validar.run());
        fPais.textProperty().addListener((o, a, n) -> validar.run());
        fCodigo.textProperty().addListener((o, a, n) -> validar.run());

        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            Porto p = new Porto();
            p.setNome(fNome.getText().trim());
            p.setPais(fPais.getText().trim());
            p.setCodigo(fCodigo.getText().trim().toUpperCase());
            return p;
        });
        return dlg.showAndWait();
    }

    // ── Navio ─────────────────────────────────────────────────────────────────

    public static Optional<Navio> mostrarNavio(Navio existente, List<Porto> portos) {
        Dialog<Navio> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Novo Navio" : "Editar Navio");
        dlg.setHeaderText(existente == null ? "🚢  Registar navio" : "🚢  Editar navio");
        dlg.getDialogPane().setMinWidth(420);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane g = grid();
        TextField fNome  = field(existente != null ? existente.getNome()       : "", "Nome do navio");
        TextField fIMO   = field(existente != null ? existente.getCodigoIMO()  : "", "IMO1234567");
        TextField fCap   = field(existente != null ? fmtDouble(existente.getCapacidadeMaxima()) : "", "Capacidade em toneladas");
        TextField fTanq  = field(existente != null ? String.valueOf(existente.getNumTanques())  : "", "Número de tanques");
        TextField fBand  = field(existente != null ? existente.getBandeira()   : "", "Bandeira (país)");
        TextField fAno   = field(existente != null ? String.valueOf(existente.getAnoFabrico())  : "", "Ano de fabrico");

        ComboBox<TipoNavio>   cbTipo   = combo(TipoNavio.values(),   existente != null ? existente.getTipo()   : null, "Tipo de navio");
        ComboBox<EstadoNavio> cbEstado = combo(EstadoNavio.values(), existente != null ? existente.getEstado() : null, "Estado");
        ComboBox<Porto>       cbPorto  = new ComboBox<>();
        cbPorto.getItems().addAll(portos);
        cbPorto.setPromptText("Porto atual");
        cbPorto.setConverter(new StringConverter<>() {
            @Override public String toString(Porto p)    { return p == null ? "" : p.getNome(); }
            @Override public Porto fromString(String s)  { return null; }
        });
        if (existente != null) {
            portos.stream().filter(p -> p.getId() == existente.getPortoAtualId()).findFirst().ifPresent(cbPorto::setValue);
        }

        int r = 0;
        g.add(lbl("Nome:"),       0, r); g.add(fNome,   1, r++);
        g.add(lbl("Código IMO:"), 0, r); g.add(fIMO,    1, r++);
        g.add(lbl("Tipo:"),       0, r); g.add(cbTipo,  1, r++);
        g.add(lbl("Estado:"),     0, r); g.add(cbEstado,1, r++);
        g.add(lbl("Capacidade (t):"), 0, r); g.add(fCap, 1, r++);
        g.add(lbl("Nº Tanques:"), 0, r); g.add(fTanq,  1, r++);
        g.add(lbl("Bandeira:"),   0, r); g.add(fBand,  1, r++);
        g.add(lbl("Ano Fabrico:"),0, r); g.add(fAno,   1, r++);
        g.add(lbl("Porto Atual:"),0, r); g.add(cbPorto,1, r++);
        dlg.getDialogPane().setContent(g);

        javafx.scene.Node okBtn = dlg.getDialogPane().lookupButton(btnOk);
        okBtn.setDisable(true);
        Runnable validar = () -> okBtn.setDisable(
                fNome.getText().isBlank() || fIMO.getText().isBlank() ||
                fCap.getText().isBlank()  || fTanq.getText().isBlank() ||
                fBand.getText().isBlank() || fAno.getText().isBlank()  ||
                cbTipo.getValue() == null || cbEstado.getValue() == null || cbPorto.getValue() == null);
        fNome.textProperty().addListener((o, a, n) -> validar.run());
        fIMO.textProperty().addListener((o, a, n) -> validar.run());
        fCap.textProperty().addListener((o, a, n) -> validar.run());
        fTanq.textProperty().addListener((o, a, n) -> validar.run());
        fBand.textProperty().addListener((o, a, n) -> validar.run());
        fAno.textProperty().addListener((o, a, n) -> validar.run());
        cbTipo.valueProperty().addListener((o, a, n) -> validar.run());
        cbEstado.valueProperty().addListener((o, a, n) -> validar.run());
        cbPorto.valueProperty().addListener((o, a, n) -> validar.run());

        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            Double cap  = parseDouble(fCap.getText());
            Integer tanq = parseInt(fTanq.getText());
            Integer ano  = parseInt(fAno.getText());
            if (cap == null || tanq == null || ano == null) return null;
            Navio n = new Navio();
            n.setNome(fNome.getText().trim());
            n.setCodigoIMO(fIMO.getText().trim());
            n.setTipo(cbTipo.getValue());
            n.setEstado(cbEstado.getValue());
            n.setCapacidadeMaxima(cap);
            n.setNumTanques(tanq);
            n.setBandeira(fBand.getText().trim());
            n.setAnoFabrico(ano);
            n.setPortoAtualId(cbPorto.getValue().getId());
            return n;
        });
        return dlg.showAndWait();
    }

    // ── Carga ─────────────────────────────────────────────────────────────────

    public static Optional<Carga> mostrarCarga(Carga existente, List<Porto> portos) {
        Dialog<Carga> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Nova Carga" : "Editar Carga");
        dlg.setHeaderText(existente == null ? "🛢  Registar carga" : "🛢  Editar carga");
        dlg.getDialogPane().setMinWidth(420);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane g = grid();
        TextField fDesig  = field(existente != null ? existente.getDesignacao()         : "", "Designação da carga");
        TextField fVolume = field(existente != null ? fmtDouble(existente.getVolume())  : "", "Volume em m³");
        TextField fPeso   = field(existente != null ? fmtDouble(existente.getPeso())    : "", "Peso em toneladas");

        ComboBox<TipoCarga> cbTipo = combo(TipoCarga.values(), existente != null ? existente.getTipo() : null, "Tipo de carga");

        CheckBox chkInflam  = new CheckBox("Inflamável");
        CheckBox chkCorros  = new CheckBox("Corrosiva");
        CheckBox chkToxica  = new CheckBox("Tóxica");
        if (existente != null) {
            chkInflam.setSelected(existente.isInflamavel());
            chkCorros.setSelected(existente.isCorrosiva());
            chkToxica.setSelected(existente.isToxica());
        }

        ComboBox<Porto> cbCarga   = portoCombo(portos, existente != null ? existente.getPortoCarregamentoId()  : -1, "Porto de carga");
        ComboBox<Porto> cbDescarga = portoCombo(portos, existente != null ? existente.getPortoDescargaId()     : -1, "Porto de descarga");

        int r = 0;
        g.add(lbl("Designação:"),     0, r); g.add(fDesig,     1, r++);
        g.add(lbl("Tipo:"),           0, r); g.add(cbTipo,     1, r++);
        g.add(lbl("Volume (m³):"),    0, r); g.add(fVolume,    1, r++);
        g.add(lbl("Peso (t):"),       0, r); g.add(fPeso,      1, r++);
        g.add(lbl("Propriedades:"),   0, r); g.add(chkInflam,  1, r++);
        g.add(new Label(""),          0, r); g.add(chkCorros,  1, r++);
        g.add(new Label(""),          0, r); g.add(chkToxica,  1, r++);
        g.add(lbl("Porto Carga:"),    0, r); g.add(cbCarga,    1, r++);
        g.add(lbl("Porto Descarga:"), 0, r); g.add(cbDescarga, 1, r++);
        dlg.getDialogPane().setContent(g);

        javafx.scene.Node okBtn = dlg.getDialogPane().lookupButton(btnOk);
        okBtn.setDisable(true);
        Runnable validar = () -> okBtn.setDisable(
                fDesig.getText().isBlank() || fVolume.getText().isBlank() || fPeso.getText().isBlank() ||
                cbTipo.getValue() == null  || cbCarga.getValue() == null  || cbDescarga.getValue() == null);
        fDesig.textProperty().addListener((o, a, n) -> validar.run());
        fVolume.textProperty().addListener((o, a, n) -> validar.run());
        fPeso.textProperty().addListener((o, a, n) -> validar.run());
        cbTipo.valueProperty().addListener((o, a, n) -> validar.run());
        cbCarga.valueProperty().addListener((o, a, n) -> validar.run());
        cbDescarga.valueProperty().addListener((o, a, n) -> validar.run());

        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            Double vol = parseDouble(fVolume.getText());
            Double pes = parseDouble(fPeso.getText());
            if (vol == null || pes == null) return null;
            Carga c = new Carga();
            c.setDesignacao(fDesig.getText().trim());
            c.setTipo(cbTipo.getValue());
            c.setVolume(vol);
            c.setPeso(pes);
            c.setInflamavel(chkInflam.isSelected());
            c.setCorrosiva(chkCorros.isSelected());
            c.setToxica(chkToxica.isSelected());
            c.setPortoCarregamentoId(cbCarga.getValue().getId());
            c.setPortoDescargaId(cbDescarga.getValue().getId());
            return c;
        });
        return dlg.showAndWait();
    }

    // ── Tripulante ────────────────────────────────────────────────────────────

    public static Optional<Tripulante> mostrarTripulante(Tripulante existente) {
        Dialog<Tripulante> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Novo Tripulante" : "Editar Tripulante");
        dlg.setHeaderText(existente == null ? "👤  Registar tripulante" : "👤  Editar tripulante");
        dlg.getDialogPane().setMinWidth(400);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane g = grid();
        TextField fNome  = field(existente != null ? existente.getNome()              : "", "Nome completo");
        TextField fCert  = field(existente != null ? existente.getNumeroCertificado() : "", "Nº de certificado");
        TextField fEmail = field(existente != null ? existente.getEmail()             : "", "E-mail");

        ComboBox<FuncaoTripulante> cbFuncao = combo(FuncaoTripulante.values(),
                existente != null ? existente.getFuncao() : null, "Função");

        ToggleSwitch chkDisp = new ToggleSwitch("Disponível");
        chkDisp.setSelected(existente == null || "DISPONIVEL".equalsIgnoreCase(existente.getEstadoDisponibilidade()));

        int r = 0;
        g.add(lbl("Nome:"),        0, r); g.add(fNome,   1, r++);
        g.add(lbl("Certificado:"), 0, r); g.add(fCert,   1, r++);
        g.add(lbl("Função:"),      0, r); g.add(cbFuncao,1, r++);
        g.add(lbl("E-mail:"),      0, r); g.add(fEmail,  1, r++);
        g.add(lbl("Estado:"),      0, r); g.add(chkDisp, 1, r++);
        dlg.getDialogPane().setContent(g);

        javafx.scene.Node okBtn = dlg.getDialogPane().lookupButton(btnOk);
        okBtn.setDisable(true);
        Runnable validar = () -> okBtn.setDisable(
                fNome.getText().isBlank() || fCert.getText().isBlank() || cbFuncao.getValue() == null);
        fNome.textProperty().addListener((o, a, n) -> validar.run());
        fCert.textProperty().addListener((o, a, n) -> validar.run());
        cbFuncao.valueProperty().addListener((o, a, n) -> validar.run());

        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            Tripulante t = new Tripulante();
            t.setNome(fNome.getText().trim());
            t.setNumeroCertificado(fCert.getText().trim());
            t.setFuncao(cbFuncao.getValue());
            t.setEmail(fEmail.getText().trim());
            t.setEstadoDisponibilidade(chkDisp.isSelected() ? "DISPONIVEL" : "INDISPONIVEL");
            return t;
        });
        return dlg.showAndWait();
    }

    // ── Viagem ────────────────────────────────────────────────────────────────

    public static Optional<Viagem> mostrarViagem(Viagem existente, List<Navio> navios, List<Porto> portos) {
        Dialog<Viagem> dlg = new Dialog<>();
        dlg.setTitle(existente == null ? "Nova Viagem" : "Editar Viagem");
        dlg.setHeaderText(existente == null ? "🗺  Planear viagem" : "🗺  Editar viagem");
        dlg.getDialogPane().setMinWidth(440);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);

        GridPane g = grid();

        ComboBox<Navio> cbNavio = new ComboBox<>();
        cbNavio.getItems().addAll(navios);
        cbNavio.setPromptText("Selecionar navio…");
        cbNavio.setConverter(new StringConverter<>() {
            @Override public String toString(Navio n)   { return n == null ? "" : n.getNome() + "  [" + n.getTipo() + "]"; }
            @Override public Navio fromString(String s) { return null; }
        });

        ComboBox<Porto> cbOrigem  = portoCombo(portos, existente != null ? existente.getPortoOrigemId()   : -1, "Porto de origem");
        ComboBox<Porto> cbDestino = portoCombo(portos, existente != null ? existente.getPortoDestinoId()  : -1, "Porto de destino");

        DatePicker dpPartida  = new DatePicker(existente != null ? existente.getDataPartida()        : LocalDate.now());
        DatePicker dpChegada  = new DatePicker(existente != null ? existente.getDataChegadaPrevista(): LocalDate.now().plusDays(7));
        dpPartida.setPromptText("Data de partida");
        dpChegada.setPromptText("Data de chegada prevista");

        if (existente != null) {
            navios.stream().filter(n -> n.getId() == existente.getNavioId()).findFirst().ifPresent(cbNavio::setValue);
        }

        int r = 0;
        g.add(lbl("Navio:"),    0, r); g.add(cbNavio,   1, r++);
        g.add(lbl("Origem:"),   0, r); g.add(cbOrigem,  1, r++);
        g.add(lbl("Destino:"),  0, r); g.add(cbDestino, 1, r++);
        g.add(lbl("Partida:"),  0, r); g.add(dpPartida, 1, r++);
        g.add(lbl("Chegada Prev.:"), 0, r); g.add(dpChegada, 1, r++);
        dlg.getDialogPane().setContent(g);

        javafx.scene.Node okBtn = dlg.getDialogPane().lookupButton(btnOk);
        okBtn.setDisable(true);
        Runnable validar = () -> okBtn.setDisable(
                cbNavio.getValue() == null || cbOrigem.getValue() == null || cbDestino.getValue() == null ||
                dpPartida.getValue() == null || dpChegada.getValue() == null);
        cbNavio.valueProperty().addListener((o, a, n) -> validar.run());
        cbOrigem.valueProperty().addListener((o, a, n) -> validar.run());
        cbDestino.valueProperty().addListener((o, a, n) -> validar.run());
        dpPartida.valueProperty().addListener((o, a, n) -> validar.run());
        dpChegada.valueProperty().addListener((o, a, n) -> validar.run());

        dlg.setResultConverter(bt -> {
            if (bt != btnOk) return null;
            Viagem v = new Viagem();
            if (existente != null) v.setId(existente.getId());
            v.setNavioId(cbNavio.getValue().getId());
            v.setPortoOrigemId(cbOrigem.getValue().getId());
            v.setPortoDestinoId(cbDestino.getValue().getId());
            v.setDataPartida(dpPartida.getValue());
            v.setDataChegadaPrevista(dpChegada.getValue());
            return v;
        });
        return dlg.showAndWait();
    }

    // ── Auxiliares ────────────────────────────────────────────────────────────

    private static GridPane grid() {
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(16));
        return g;
    }

    private static Label lbl(String text) {
        Label l = new Label(text);
        l.setMinWidth(120);
        return l;
    }

    private static TextField field(String valor, String prompt) {
        TextField tf = new TextField(valor);
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        return tf;
    }

    private static <T> ComboBox<T> combo(T[] values, T selected, String prompt) {
        ComboBox<T> cb = new ComboBox<>();
        cb.getItems().addAll(values);
        cb.setPromptText(prompt);
        cb.setPrefWidth(220);
        if (selected != null) cb.setValue(selected);
        return cb;
    }

    private static ComboBox<Porto> portoCombo(List<Porto> portos, int selectedId, String prompt) {
        ComboBox<Porto> cb = new ComboBox<>();
        cb.getItems().addAll(portos);
        cb.setPromptText(prompt);
        cb.setPrefWidth(220);
        cb.setConverter(new StringConverter<>() {
            @Override public String toString(Porto p)   { return p == null ? "" : p.getNome(); }
            @Override public Porto fromString(String s) { return null; }
        });
        if (selectedId > 0) {
            portos.stream().filter(p -> p.getId() == selectedId).findFirst().ifPresent(cb::setValue);
        }
        return cb;
    }

    private static Double parseDouble(String s) {
        try { return Double.parseDouble(s.trim().replace(",", ".")); } catch (Exception e) { return null; }
    }

    private static Integer parseInt(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return null; }
    }

    private static String fmtDouble(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }
}
