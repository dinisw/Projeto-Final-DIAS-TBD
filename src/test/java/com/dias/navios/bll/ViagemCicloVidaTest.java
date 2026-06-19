package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.TripulanteDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViagemService — ciclo de vida completo de uma viagem")
class ViagemCicloVidaTest {

    @Mock ViagemDAO     viagemDAO;
    @Mock NavioDAO      navioDAO;
    @Mock CargaDAO      cargaDAO;
    @Mock TripulanteDAO tripulanteDAO;
    @InjectMocks ViagemService viagemService;

    private Navio navioAtivo;
    private Viagem viagemPlaneada;

    @BeforeEach
    void setUp() {
        navioAtivo = new Navio();
        navioAtivo.setId(1);
        navioAtivo.setEstado(EstadoNavio.ATIVO);
        navioAtivo.setTipo(TipoNavio.CRUDE);
        navioAtivo.setCapacidadeMaxima(100_000);

        viagemPlaneada = new Viagem();
        viagemPlaneada.setId(1);
        viagemPlaneada.setNavioId(1);
        viagemPlaneada.setEstado(EstadoViagem.PLANEADA);
    }

    // =========================================================================
    // Cenário 1: Criação → PLANEADA
    // =========================================================================

    @Test
    @DisplayName("Cenário 1a: Criar viagem com dados válidos — fica PLANEADA")
    void criarViagemFicaPlaneada() throws Exception {
        Viagem v = new Viagem();
        v.setNavioId(1);
        v.setPortoOrigemId(10);
        v.setPortoDestinoId(20);
        v.setDataPartida(LocalDate.of(2025, 9, 1));

        when(navioDAO.buscarPorId(1)).thenReturn(navioAtivo);
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        viagemService.criarViagem(v);

        assertEquals(EstadoViagem.PLANEADA, v.getEstado());
        verify(viagemDAO).inserir(v);
    }

    @Test
    @DisplayName("Cenário 1b: Dois navios distintos podem ter viagens simultâneas")
    void doisNaviosPodemTerViagensSeparadas() throws Exception {
        Navio navio2 = new Navio();
        navio2.setId(2);
        navio2.setEstado(EstadoNavio.ATIVO);
        navio2.setTipo(TipoNavio.REFINADOS);
        navio2.setCapacidadeMaxima(50_000);

        Viagem v1 = viagem(1, 1, 10, 20);
        Viagem v2 = viagem(2, 2, 30, 40);

        when(navioDAO.buscarPorId(1)).thenReturn(navioAtivo);
        when(navioDAO.buscarPorId(2)).thenReturn(navio2);
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);
        when(viagemDAO.navioTemViagemAtiva(2)).thenReturn(false);

        assertDoesNotThrow(() -> viagemService.criarViagem(v1));
        assertDoesNotThrow(() -> viagemService.criarViagem(v2));
    }

    // =========================================================================
    // Cenário 2: Associar carga compatível e verificar capacidade
    // =========================================================================

    @Test
    @DisplayName("Cenário 2a: Associar carga compatível dentro da capacidade")
    void associarCargaCompativelDentroCapacidade() throws Exception {
        Carga carga = carga(TipoCarga.PETROLEO_BRUTO, 30_000);
        carga.setId(5);

        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(navioDAO.buscarPorId(1)).thenReturn(navioAtivo);
        when(cargaDAO.buscarPorId(5)).thenReturn(carga);
        when(navioDAO.buscarMaxCargasDoTipo(1)).thenReturn(4);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> viagemService.associarCarga(1, 5));
        verify(viagemDAO).adicionarCarga(1, 5);
    }

    @Test
    @DisplayName("Cenário 2b: Segunda carga acumula peso — recusada quando excede capacidade")
    void segundaCargaExcedeCapacidade() throws Exception {
        Carga existente = carga(TipoCarga.PETROLEO_BRUTO, 80_000);
        Carga nova = carga(TipoCarga.PETROLEO_BRUTO, 30_000);
        nova.setId(9);

        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(navioDAO.buscarPorId(1)).thenReturn(navioAtivo);
        when(cargaDAO.buscarPorId(9)).thenReturn(nova);
        when(navioDAO.buscarMaxCargasDoTipo(1)).thenReturn(4);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(List.of(existente));

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 9));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Cenário 2c: Carga incompatível com navio CRUDE é recusada")
    void cargaQuimicaRecusadaPorNavioCrude() throws Exception {
        Carga quimica = carga(TipoCarga.QUIMICOS, 5_000);
        quimica.setId(7);

        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(navioDAO.buscarPorId(1)).thenReturn(navioAtivo);
        when(cargaDAO.buscarPorId(7)).thenReturn(quimica);

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 7));
    }

    // =========================================================================
    // Cenário 3: Associar tripulante
    // =========================================================================

    @Test
    @DisplayName("Cenário 3a: Associar tripulante a viagem PLANEADA")
    void associarTripulanteAViagemPlaneada() throws Exception {
        Tripulante t = new Tripulante();
        t.setId(42);
        t.setFuncao(FuncaoTripulante.CAPITAO);
        t.setEstadoDisponibilidade("DISPONIVEL");

        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(tripulanteDAO.buscarPorId(42)).thenReturn(t);

        assertDoesNotThrow(() -> viagemService.associarTripulante(1, 42));
        verify(viagemDAO).adicionarTripulante(1, 42, "CAPITAO");
    }

    @Test
    @DisplayName("Cenário 3b: Mesmo tripulante não pode ser associado duas vezes")
    void tripulanteDuplicadoEhRejeitado() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(viagemDAO.tripulanteJaAssociado(1, 42)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 42));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt(), anyString());
    }

    // =========================================================================
    // Cenário 4: PLANEADA → EM_CURSO → CONCLUIDA
    // =========================================================================

    @Test
    @DisplayName("Cenário 4a: PLANEADA → EM_CURSO (avançar estado)")
    void planeadaAvancaParaEmCurso() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);
        when(viagemDAO.viagemTemCapitao(1)).thenReturn(true);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(1);

        viagemService.avancarEstado(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.EM_CURSO);
    }

    @Test
    @DisplayName("Cenário 4b: EM_CURSO → CONCLUIDA (avançar estado)")
    void emCursoAvancaParaConcluida() throws Exception {
        Viagem emCurso = new Viagem();
        emCurso.setId(1);
        emCurso.setEstado(EstadoViagem.EM_CURSO);
        when(viagemDAO.buscarPorId(1)).thenReturn(emCurso);

        viagemService.avancarEstado(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.CONCLUIDA);
    }

    @Test
    @DisplayName("Cenário 4c: CONCLUIDA não pode avançar mais")
    void concluidaNaoPodeAvancar() throws Exception {
        Viagem concluida = new Viagem();
        concluida.setId(1);
        concluida.setEstado(EstadoViagem.CONCLUIDA);
        when(viagemDAO.buscarPorId(1)).thenReturn(concluida);

        assertThrows(IllegalStateException.class, () -> viagemService.avancarEstado(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    // =========================================================================
    // Cenário 5: Cancelamento
    // =========================================================================

    @Test
    @DisplayName("Cenário 5a: Cancelar viagem PLANEADA")
    void cancelarPlaneada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);

        viagemService.cancelarViagem(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.CANCELADA);
    }

    @Test
    @DisplayName("Cenário 5b: Cancelar viagem CONCLUIDA é impossível")
    void cancelarConcluidaEhImpossivel() throws Exception {
        Viagem concluida = new Viagem();
        concluida.setId(1);
        concluida.setEstado(EstadoViagem.CONCLUIDA);
        when(viagemDAO.buscarPorId(1)).thenReturn(concluida);

        assertThrows(IllegalStateException.class, () -> viagemService.cancelarViagem(1));
    }

    @Test
    @DisplayName("Cenário 5c: Cancelar viagem já CANCELADA é impossível (idempotência)")
    void cancelarJaCanceladaEhImpossivel() throws Exception {
        Viagem cancelada = new Viagem();
        cancelada.setId(1);
        cancelada.setEstado(EstadoViagem.CANCELADA);
        when(viagemDAO.buscarPorId(1)).thenReturn(cancelada);

        assertThrows(IllegalStateException.class, () -> viagemService.cancelarViagem(1));
    }

    // =========================================================================
    // Cenário 6: Editar viagem
    // =========================================================================

    @Test
    @DisplayName("Cenário 6a: Editar porto de destino em viagem PLANEADA")
    void editarPortoDestinoEmViagemPlaneada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagemPlaneada);

        Viagem edicao = new Viagem();
        edicao.setId(1);
        edicao.setNavioId(1);
        edicao.setPortoOrigemId(10);
        edicao.setPortoDestinoId(99);
        edicao.setDataPartida(LocalDate.of(2025, 10, 1));

        assertDoesNotThrow(() -> viagemService.editarViagem(edicao));
        verify(viagemDAO).atualizar(edicao);
    }

    @Test
    @DisplayName("Cenário 6b: Não é possível editar viagem EM_CURSO")
    void naoPodeEditarViagemEmCurso() throws Exception {
        Viagem emCurso = new Viagem();
        emCurso.setId(1);
        emCurso.setEstado(EstadoViagem.EM_CURSO);
        when(viagemDAO.buscarPorId(1)).thenReturn(emCurso);

        Viagem edicao = new Viagem();
        edicao.setId(1);

        assertThrows(IllegalStateException.class, () -> viagemService.editarViagem(edicao));
        verify(viagemDAO, never()).atualizar(any());
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private Viagem viagem(int id, int navioId, int origem, int destino) {
        Viagem v = new Viagem();
        v.setNavioId(navioId);
        v.setPortoOrigemId(origem);
        v.setPortoDestinoId(destino);
        v.setDataPartida(LocalDate.of(2025, 10, 1));
        return v;
    }

    private Carga carga(TipoCarga tipo, double peso) {
        Carga c = new Carga();
        c.setId(1);
        c.setTipo(tipo);
        c.setPeso(peso);
        c.setVolume(peso * 1.2);
        return c;
    }
}
