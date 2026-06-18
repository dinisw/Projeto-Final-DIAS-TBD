package com.dias.navios.ui.controller;

import com.dias.navios.bll.CargaService;
import com.dias.navios.bll.NavioService;
import com.dias.navios.bll.PortoService;
import com.dias.navios.bll.TripulanteService;
import com.dias.navios.bll.ViagemService;
import com.dias.navios.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ViagemController {

    // --- Tabela de viagens ---
    @FXML private TableView<Viagem> tabelaViagens;
    @FXML private TableColumn<Viagem, String> colViagemId;
    @FXML private TableColumn<Viagem, String> colViagemNavio;
    @FXML private TableColumn<Viagem, String> colViagemOrigem;
    @FXML private TableColumn<Viagem, String> colViagemDestino;
    @FXML private TableColumn<Viagem, String> colViagemEstado;
    @FXML private TableColumn<Viagem, String> colViagemPartida;

    // --- Formulário básico ---
    @FXML private ComboBox<Navio> comboNavio;
    @FXML private ComboBox<Porto> comboPortoOrigem;
    @FXML private ComboBox<Porto> comboPortoDestino;
    @FXML private DatePicker campoDataPartida;
    @FXML private DatePicker campoDataChegada;

    // --- Listas de cargas ---
    @FXML private ListView<Carga> listaCargasDisponiveis;
    @FXML private ListView<Carga> listaCargasSelecionadas;

    // --- Listas de tripulantes ---
    @FXML private ListView<Tripulante> listaTripulantesDisponiveis;
    @FXML private ListView<Tripulante> listaTripulantesSelecionados;

    // --- Pesquisa e mensagem ---
    @FXML private TextField campoPesquisa;
    @FXML private Label labelMensagem;
    @FXML private Label labelCapacidade;

    private ViagemService viagemService = new ViagemService();
    private NavioService navioService = new NavioService();
    private PortoService portoService = new PortoService();
    private CargaService cargaService = new CargaService();
    private TripulanteService tripulanteService = new TripulanteService();

    private Viagem viagemSelecionada = null;
    private ObservableList<Carga> cargasDisponiveis = FXCollections.observableArrayList();
    private ObservableList<Carga> cargasSelecionadas = FXCollections.observableArrayList();
    private ObservableList<Tripulante> tripulantesDisponiveis = FXCollections.observableArrayList();
    private ObservableList<Tripulante> tripulantesSelecionados = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColunas();
        carregarComboBoxes();

        listaCargasDisponiveis.setItems(cargasDisponiveis);
        listaCargasSelecionadas.setItems(cargasSelecionadas);
        listaTripulantesDisponiveis.setItems(tripulantesDisponiveis);
        listaTripulantesSelecionados.setItems(tripulantesSelecionados);

        tabelaViagens.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) carregarViagemNoFormulario(novo);
        });

        comboNavio.valueProperty().addListener((obs, old, novo) -> atualizarCapacidade());

        carregarTabela();
    }

    private void configurarColunas() {
        colViagemId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colViagemNavio.setCellValueFactory(c -> new SimpleStringProperty("Navio #" + c.getValue().getNavioId()));
        colViagemOrigem.setCellValueFactory(c -> new SimpleStringProperty("Porto #" + c.getValue().getPortoOrigemId()));
        colViagemDestino.setCellValueFactory(c -> new SimpleStringProperty("Porto #" + c.getValue().getPortoDestinoId()));
        colViagemEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstado().name()));
        colViagemPartida.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataPartida() != null ? c.getValue().getDataPartida().toString() : ""));
    }

    private void carregarComboBoxes() {
        try {
            List<Navio> navios = navioService.listarNavios();
            comboNavio.setItems(FXCollections.observableArrayList(navios));

            List<Porto> portos = portoService.listarPortos();
            ObservableList<Porto> obsPortos = FXCollections.observableArrayList(portos);
            comboPortoOrigem.setItems(obsPortos);
            comboPortoDestino.setItems(FXCollections.observableArrayList(portos));

            List<Carga> todasCargas = cargaService.listarCargas();
            cargasDisponiveis.setAll(todasCargas);

            List<Tripulante> todosTripulantes = tripulanteService.listarTripulantes();
            tripulantesDisponiveis.setAll(todosTripulantes);
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar dados: " + e.getMessage());
        }
    }

    private void carregarTabela() {
        try {
            List<Viagem> viagens = viagemService.listarViagens();
            String filtro = campoPesquisa != null ? campoPesquisa.getText().toLowerCase() : "";
            if (!filtro.isBlank()) {
                viagens.removeIf(v -> !v.getEstado().name().toLowerCase().contains(filtro)
                        && !String.valueOf(v.getId()).contains(filtro));
            }
            tabelaViagens.setItems(FXCollections.observableArrayList(viagens));
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar viagens: " + e.getMessage());
        }
    }

    private void carregarViagemNoFormulario(Viagem viagem) {
        viagemSelecionada = viagem;

        comboNavio.getItems().stream()
            .filter(n -> n.getId() == viagem.getNavioId())
            .findFirst().ifPresent(comboNavio::setValue);
        comboPortoOrigem.getItems().stream()
            .filter(p -> p.getId() == viagem.getPortoOrigemId())
            .findFirst().ifPresent(comboPortoOrigem::setValue);
        comboPortoDestino.getItems().stream()
            .filter(p -> p.getId() == viagem.getPortoDestinoId())
            .findFirst().ifPresent(comboPortoDestino::setValue);

        campoDataPartida.setValue(viagem.getDataPartida());
        campoDataChegada.setValue(viagem.getDataChegadaPrevista());

        // Carregar cargas e tripulantes associados
        try {
            List<Carga> cargasViagem = viagemService.listarCargasDaViagem(viagem.getId());
            List<Carga> todasCargas = cargaService.listarCargas();
            todasCargas.removeAll(cargasViagem);
            cargasSelecionadas.setAll(cargasViagem);
            cargasDisponiveis.setAll(todasCargas);

            List<Tripulante> tripulantesViagem = viagemService.listarTripulantesDaViagem(viagem.getId());
            List<Tripulante> todosTripulantes = tripulanteService.listarTripulantes();
            todosTripulantes.removeAll(tripulantesViagem);
            tripulantesSelecionados.setAll(tripulantesViagem);
            tripulantesDisponiveis.setAll(todosTripulantes);
        } catch (Exception e) {
            labelMensagem.setText("Erro ao carregar associações: " + e.getMessage());
        }

        atualizarCapacidade();
        labelMensagem.setText("Viagem #" + viagem.getId() + " carregada — estado: " + viagem.getEstado());
    }

    private void atualizarCapacidade() {
        if (labelCapacidade == null) return;
        Navio navio = comboNavio.getValue();
        if (navio == null) {
            labelCapacidade.setText("");
            return;
        }
        double pesoTotal = cargasSelecionadas.stream().mapToDouble(Carga::getPeso).sum();
        labelCapacidade.setText(String.format("Capacidade: %.1f / %.1f ton (%.0f%%)",
                pesoTotal, navio.getCapacidadeMaxima(),
                navio.getCapacidadeMaxima() > 0 ? (pesoTotal / navio.getCapacidadeMaxima() * 100) : 0));
    }

    // --- Acções sobre cargas ---

    @FXML
    public void adicionarCarga() {
        Carga carga = listaCargasDisponiveis.getSelectionModel().getSelectedItem();
        if (carga == null) {
            labelMensagem.setText("Seleccione uma carga para adicionar.");
            return;
        }
        if (viagemSelecionada != null) {
            try {
                viagemService.associarCarga(viagemSelecionada.getId(), carga.getId());
                cargasSelecionadas.add(carga);
                cargasDisponiveis.remove(carga);
                atualizarCapacidade();
                labelMensagem.setText("Carga associada com sucesso.");
            } catch (Exception e) {
                labelMensagem.setText(e.getMessage());
            }
        } else {
            // Modo criação: gestão em memória; a associação será feita após criar a viagem
            cargasSelecionadas.add(carga);
            cargasDisponiveis.remove(carga);
            atualizarCapacidade();
        }
    }

    @FXML
    public void removerCarga() {
        Carga carga = listaCargasSelecionadas.getSelectionModel().getSelectedItem();
        if (carga == null) {
            labelMensagem.setText("Seleccione uma carga para remover.");
            return;
        }
        if (viagemSelecionada != null) {
            try {
                viagemService.removerCarga(viagemSelecionada.getId(), carga.getId());
                cargasDisponiveis.add(carga);
                cargasSelecionadas.remove(carga);
                atualizarCapacidade();
                labelMensagem.setText("Carga removida.");
            } catch (Exception e) {
                labelMensagem.setText(e.getMessage());
            }
        } else {
            cargasDisponiveis.add(carga);
            cargasSelecionadas.remove(carga);
            atualizarCapacidade();
        }
    }

    // --- Acções sobre tripulantes ---

    @FXML
    public void adicionarTripulante() {
        Tripulante t = listaTripulantesDisponiveis.getSelectionModel().getSelectedItem();
        if (t == null) {
            labelMensagem.setText("Seleccione um tripulante para adicionar.");
            return;
        }
        if (viagemSelecionada != null) {
            try {
                viagemService.associarTripulante(viagemSelecionada.getId(), t.getId());
                tripulantesSelecionados.add(t);
                tripulantesDisponiveis.remove(t);
                labelMensagem.setText("Tripulante associado com sucesso.");
            } catch (Exception e) {
                labelMensagem.setText(e.getMessage());
            }
        } else {
            tripulantesSelecionados.add(t);
            tripulantesDisponiveis.remove(t);
        }
    }

    @FXML
    public void removerTripulante() {
        Tripulante t = listaTripulantesSelecionados.getSelectionModel().getSelectedItem();
        if (t == null) {
            labelMensagem.setText("Seleccione um tripulante para remover.");
            return;
        }
        if (viagemSelecionada != null) {
            try {
                viagemService.removerTripulante(viagemSelecionada.getId(), t.getId());
                tripulantesDisponiveis.add(t);
                tripulantesSelecionados.remove(t);
                labelMensagem.setText("Tripulante removido.");
            } catch (Exception e) {
                labelMensagem.setText(e.getMessage());
            }
        } else {
            tripulantesDisponiveis.add(t);
            tripulantesSelecionados.remove(t);
        }
    }

    // --- CRUD de viagens ---

    @FXML
    public void criarViagem() {
        try {
            Viagem viagem = new Viagem();
            preencherViagemDoFormulario(viagem);

            // Validar compatibilidade de cargas antes de criar
            Navio navio = comboNavio.getValue();
            for (Carga c : cargasSelecionadas) {
                if (!navio.getTipo().aceitaCarga(c.getTipo())) {
                    labelMensagem.setText("Incompatibilidade: navio " + navio.getTipo()
                            + " não aceita carga " + c.getTipo() + " (" + c.getDesignacao() + ").");
                    return;
                }
            }
            double pesoTotal = cargasSelecionadas.stream().mapToDouble(Carga::getPeso).sum();
            if (pesoTotal > navio.getCapacidadeMaxima()) {
                labelMensagem.setText("Capacidade excedida: " + pesoTotal + " ton > " + navio.getCapacidadeMaxima() + " ton.");
                return;
            }

            viagemService.criarViagem(viagem);

            // Associar cargas e tripulantes
            for (Carga c : cargasSelecionadas) {
                viagemService.associarCarga(viagem.getId(), c.getId());
            }
            for (Tripulante t : tripulantesSelecionados) {
                viagemService.associarTripulante(viagem.getId(), t.getId());
            }

            labelMensagem.setText("Viagem criada com sucesso (ID: " + viagem.getId() + ").");
            novaViagem();
            carregarTabela();
        } catch (IllegalArgumentException | IllegalStateException e) {
            labelMensagem.setText("Regra de negócio: " + e.getMessage());
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void editarViagem() {
        if (viagemSelecionada == null) {
            labelMensagem.setText("Seleccione uma viagem na tabela para editar.");
            return;
        }
        try {
            preencherViagemDoFormulario(viagemSelecionada);
            viagemService.editarViagem(viagemSelecionada);
            labelMensagem.setText("Viagem actualizada com sucesso.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void avancarEstado() {
        Viagem selecionada = tabelaViagens.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Seleccione uma viagem.");
            return;
        }
        try {
            viagemService.avancarEstado(selecionada.getId());
            labelMensagem.setText("Estado avançado com sucesso.");
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void cancelarViagem() {
        Viagem selecionada = tabelaViagens.getSelectionModel().getSelectedItem();
        if (selecionada == null) {
            labelMensagem.setText("Seleccione uma viagem.");
            return;
        }
        try {
            viagemService.cancelarViagem(selecionada.getId());
            labelMensagem.setText("Viagem cancelada.");
            novaViagem();
            carregarTabela();
        } catch (Exception e) {
            labelMensagem.setText("Erro: " + e.getMessage());
        }
    }

    @FXML
    public void pesquisar() {
        carregarTabela();
    }

    @FXML
    public void novaViagem() {
        viagemSelecionada = null;
        comboNavio.setValue(null);
        comboPortoOrigem.setValue(null);
        comboPortoDestino.setValue(null);
        campoDataPartida.setValue(null);
        campoDataChegada.setValue(null);
        cargasSelecionadas.clear();
        tripulantesSelecionados.clear();
        carregarComboBoxes();
        tabelaViagens.getSelectionModel().clearSelection();
        if (labelCapacidade != null) labelCapacidade.setText("");
        labelMensagem.setText("Formulário pronto para nova viagem.");
    }

    private void preencherViagemDoFormulario(Viagem viagem) {
        viagem.setNavioId(comboNavio.getValue() != null ? comboNavio.getValue().getId() : 0);
        viagem.setPortoOrigemId(comboPortoOrigem.getValue() != null ? comboPortoOrigem.getValue().getId() : 0);
        viagem.setPortoDestinoId(comboPortoDestino.getValue() != null ? comboPortoDestino.getValue().getId() : 0);
        viagem.setDataPartida(campoDataPartida.getValue());
        viagem.setDataChegadaPrevista(campoDataChegada.getValue());
    }
}
