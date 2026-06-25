package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.TripulanteDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
@DisplayName("ViagemService — regras de negócio (com mocks de DAO)")
class ViagemServiceRegrasNegocioTest {

    @Mock ViagemDAO     viagemDAO;
    @Mock NavioDAO      navioDAO;
    @Mock CargaDAO      cargaDAO;
    @Mock TripulanteDAO tripulanteDAO;
    @InjectMocks ViagemService viagemService;

    private Viagem viagemBase;

    @BeforeEach
    void setUp() {
        viagemBase = new Viagem();
        viagemBase.setNavioId(1);
        viagemBase.setPortoOrigemId(1);
        viagemBase.setPortoDestinoId(2);
        viagemBase.setDataPartida(LocalDate.of(2025, 8, 1));
    }

    // =========================================================================
    // criarViagem — estado do navio
    // =========================================================================

    @Test
    @DisplayName("Navio ATIVO pode iniciar viagem")
    void navioAtivoPermitecriarViagem() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.ATIVO));
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> viagemService.criarViagem(viagemBase));
        verify(viagemDAO).inserir(viagemBase);
    }

    @Test
    @DisplayName("Navio INATIVO não pode iniciar viagem")
    void navioInativoRejeitaCriarViagem() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.INATIVO));

        assertThrows(IllegalStateException.class, () -> viagemService.criarViagem(viagemBase));
        verify(viagemDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Navio MANUTENCAO não pode iniciar viagem")
    void navioEmManutencaoRejeitaCriarViagem() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.MANUTENCAO));

        assertThrows(IllegalStateException.class, () -> viagemService.criarViagem(viagemBase));
        verify(viagemDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Navio com viagem PLANEADA não pode ter outra viagem")
    void navioComViagemPlaneadaRejeitaNova() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.ATIVO));
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.criarViagem(viagemBase));
        verify(viagemDAO, never()).inserir(any());
    }

    @Test
    @DisplayName("Viagem criada recebe estado PLANEADA automaticamente")
    void viagemCriadaFicaComEstadoPlaneada() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.ATIVO));
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        viagemService.criarViagem(viagemBase);

        assertEquals(EstadoViagem.PLANEADA, viagemBase.getEstado());
    }

    // =========================================================================
    // avancarEstado — máquina de estados
    // =========================================================================

    @Test
    @DisplayName("PLANEADA → EM_CURSO é uma transição válida")
    void planeadaAvancaParaEmCurso() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(viagemDAO.viagemTemCapitao(1)).thenReturn(true);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(1);

        viagemService.avancarEstado(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.EM_CURSO);
    }

    @Test
    @DisplayName("PLANEADA sem Capitão não pode avançar")
    void planeadaSemCapitaoNaoPodeAvancar() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(viagemDAO.viagemTemCapitao(1)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> viagemService.avancarEstado(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    @Test
    @DisplayName("PLANEADA sem cargas não pode avançar")
    void planeadaSemCargasNaoPodeAvancar() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(viagemDAO.viagemTemCapitao(1)).thenReturn(true);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> viagemService.avancarEstado(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    @Test
    @DisplayName("EM_CURSO → CONCLUIDA é uma transição válida")
    void emCursoAvancaParaConcluida() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.EM_CURSO));

        viagemService.avancarEstado(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.CONCLUIDA);
    }

    @Test
    @DisplayName("CONCLUIDA não pode avançar")
    void concluidaNaoPodeAvancar() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CONCLUIDA));

        assertThrows(IllegalStateException.class, () -> viagemService.avancarEstado(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    @Test
    @DisplayName("CANCELADA não pode avançar")
    void canceladaNaoPodeAvancar() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CANCELADA));

        assertThrows(IllegalStateException.class, () -> viagemService.avancarEstado(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    // =========================================================================
    // cancelarViagem
    // =========================================================================

    @Test
    @DisplayName("Viagem PLANEADA pode ser cancelada")
    void planeadaPodeSer_Cancelada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));

        viagemService.cancelarViagem(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.CANCELADA);
    }

    @Test
    @DisplayName("Viagem EM_CURSO pode ser cancelada")
    void emCursoPodeSer_Cancelada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.EM_CURSO));

        viagemService.cancelarViagem(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.CANCELADA);
    }

    @Test
    @DisplayName("Viagem CONCLUIDA não pode ser cancelada")
    void concluidaNaoPodeSerCancelada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CONCLUIDA));

        assertThrows(IllegalStateException.class, () -> viagemService.cancelarViagem(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    @Test
    @DisplayName("Viagem já CANCELADA não pode ser cancelada novamente")
    void canceladaNaoPodeSerCanceladaDuasVezes() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CANCELADA));

        assertThrows(IllegalStateException.class, () -> viagemService.cancelarViagem(1));
        verify(viagemDAO, never()).atualizarEstado(anyInt(), any());
    }

    // =========================================================================
    // associarCarga — compatibilidade e capacidade
    // =========================================================================

    @Test
    @DisplayName("Carga compatível com navio é aceite")
    void cargaCompativelEAceite() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        Navio n = navio(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);
        Carga c = carga(TipoCarga.PETROLEO_BRUTO, 10_000);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(1)).thenReturn(c);
        when(navioDAO.buscarMaxCargasDoTipo(1)).thenReturn(4);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> viagemService.associarCarga(1, 1));
        verify(viagemDAO).adicionarCarga(1, 1);
    }

    @Test
    @DisplayName("Carga incompatível com tipo de navio é rejeitada")
    void cargaIncompativelEhRejeitada() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        Navio n = navio(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);
        Carga c = carga(TipoCarga.QUIMICOS, 1_000);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(1)).thenReturn(c);

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 1));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Capacidade do navio não pode ser excedida")
    void capacidadeExcedidaEhRejeitada() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        Navio n = navio(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);

        Carga existente = carga(TipoCarga.PETROLEO_BRUTO, 45_000);
        Carga nova = carga(TipoCarga.PETROLEO_BRUTO, 10_000);
        nova.setId(2);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(2)).thenReturn(nova);
        when(navioDAO.buscarMaxCargasDoTipo(1)).thenReturn(4);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(List.of(existente));

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 2));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Capacidade exatamente no limite é aceite")
    void capacidadeExatamenteNoLimiteEAceite() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        Navio n = navio(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);

        Carga existente = carga(TipoCarga.PETROLEO_BRUTO, 40_000);
        Carga nova = carga(TipoCarga.PETROLEO_BRUTO, 10_000);
        nova.setId(2);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(2)).thenReturn(nova);
        when(navioDAO.buscarMaxCargasDoTipo(1)).thenReturn(4);
        when(viagemDAO.contarCargasDaViagem(1)).thenReturn(0);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(List.of(existente));

        assertDoesNotThrow(() -> viagemService.associarCarga(1, 2));
        verify(viagemDAO).adicionarCarga(1, 2);
    }

    @Test
    @DisplayName("Carga já associada à viagem é rejeitada")
    void cargaJaAssociadaEhRejeitada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(viagemDAO.cargaJaAssociada(1, 1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 1));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Não é possível adicionar carga a viagem CONCLUIDA")
    void naoPodeAdicionarCargaAViagemConcluida() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CONCLUIDA));

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 1));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Não é possível adicionar carga a viagem CANCELADA")
    void naoPodeAdicionarCargaAViagemCancelada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CANCELADA));

        assertThrows(IllegalStateException.class, () -> viagemService.associarCarga(1, 1));
        verify(viagemDAO, never()).adicionarCarga(anyInt(), anyInt());
    }

    // =========================================================================
    // removerCarga
    // =========================================================================

    @Test
    @DisplayName("Carga pode ser removida de viagem PLANEADA")
    void cargaPodeSerRemovidaDeViagemPlaneada() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));

        assertDoesNotThrow(() -> viagemService.removerCarga(1, 1));
        verify(viagemDAO).removerCarga(1, 1);
    }

    @Test
    @DisplayName("Carga não pode ser removida de viagem EM_CURSO")
    void cargaNaoPodeSerRemovidaDeViagemEmCurso() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.EM_CURSO));

        assertThrows(IllegalStateException.class, () -> viagemService.removerCarga(1, 1));
        verify(viagemDAO, never()).removerCarga(anyInt(), anyInt());
    }

    // =========================================================================
    // associarTripulante
    // =========================================================================

    @Test
    @DisplayName("Tripulante pode ser associado a viagem PLANEADA")
    void tripulantePodeSerAssociadoAViagemPlaneada() throws Exception {
        Tripulante t = new Tripulante();
        t.setId(1);
        t.setFuncao(FuncaoTripulante.CAPITAO);
        t.setEstadoDisponibilidade("DISPONIVEL");

        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(tripulanteDAO.buscarPorId(1)).thenReturn(t);

        assertDoesNotThrow(() -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO).adicionarTripulante(1, 1, "CAPITAO");
    }

    @Test
    @DisplayName("Tripulante já associado à viagem é rejeitado")
    void tripulanteJaAssociadoEhRejeitado() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(viagemDAO.tripulanteJaAssociado(1, 1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Não é possível adicionar tripulante a viagem CONCLUIDA")
    void naoPodeAdicionarTripulanteAViagemConcluida() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CONCLUIDA));

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Tripulante não pode ser removido de viagem EM_CURSO")
    void tripulanteNaoPodeSerRemovidoDeViagemEmCurso() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.EM_CURSO));

        assertThrows(IllegalStateException.class, () -> viagemService.removerTripulante(1, 1));
        verify(viagemDAO, never()).removerTripulante(anyInt(), anyInt());
    }

    // =========================================================================
    // editarViagem
    // =========================================================================

    @Test
    @DisplayName("Só é possível editar viagem PLANEADA")
    void soPodeEditarViagemPlaneada() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        when(viagemDAO.buscarPorId(1)).thenReturn(v);

        Viagem edicao = viagem(1, EstadoViagem.PLANEADA);
        edicao.setPortoOrigemId(1);
        edicao.setPortoDestinoId(3);
        edicao.setNavioId(1);
        edicao.setDataPartida(LocalDate.of(2025, 9, 1));

        assertDoesNotThrow(() -> viagemService.editarViagem(edicao));
        verify(viagemDAO).atualizar(edicao);
    }

    @Test
    @DisplayName("Não é possível editar viagem EM_CURSO")
    void naoPodeEditarViagemEmCurso() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.EM_CURSO));

        Viagem edicao = viagem(1, EstadoViagem.EM_CURSO);
        edicao.setPortoOrigemId(1);
        edicao.setPortoDestinoId(2);
        edicao.setNavioId(1);
        edicao.setDataPartida(LocalDate.of(2025, 9, 1));

        assertThrows(IllegalStateException.class, () -> viagemService.editarViagem(edicao));
        verify(viagemDAO, never()).atualizar(any());
    }

    // =========================================================================
    // editarViagem — troca de navio (F2)
    // =========================================================================

    @Test
    @DisplayName("Editar viagem trocando para navio com viagem ativa é rejeitado (F2)")
    void editarParaNavioOcupadoEhRejeitado() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA)); // navioId=1

        Viagem edicao = viagem(1, EstadoViagem.PLANEADA);
        edicao.setNavioId(9);                 // troca de navio
        edicao.setPortoOrigemId(1);
        edicao.setPortoDestinoId(2);
        edicao.setDataPartida(LocalDate.of(2025, 9, 1));

        when(navioDAO.buscarPorId(9)).thenReturn(navio(EstadoNavio.ATIVO));
        when(viagemDAO.navioTemViagemAtiva(9)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.editarViagem(edicao));
        verify(viagemDAO, never()).atualizar(any());
    }

    @Test
    @DisplayName("Editar viagem trocando para navio livre é aceite (F2)")
    void editarParaNavioLivreEhAceite() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA)); // navioId=1

        Viagem edicao = viagem(1, EstadoViagem.PLANEADA);
        edicao.setNavioId(9);
        edicao.setPortoOrigemId(1);
        edicao.setPortoDestinoId(2);
        edicao.setDataPartida(LocalDate.of(2025, 9, 1));

        when(navioDAO.buscarPorId(9)).thenReturn(navio(EstadoNavio.ATIVO));
        when(viagemDAO.navioTemViagemAtiva(9)).thenReturn(false);

        assertDoesNotThrow(() -> viagemService.editarViagem(edicao));
        verify(viagemDAO).atualizar(edicao);
    }

    // =========================================================================
    // associarTripulante — um só Capitão (F3) e libertação ao remover (F7)
    // =========================================================================

    @Test
    @DisplayName("Não é possível atribuir um segundo Capitão à mesma viagem (F3)")
    void segundoCapitaoEhRejeitado() throws Exception {
        Tripulante t = new Tripulante();
        t.setId(2);
        t.setFuncao(FuncaoTripulante.CAPITAO);
        t.setEstadoDisponibilidade("DISPONIVEL");

        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));
        when(tripulanteDAO.buscarPorId(2)).thenReturn(t);
        when(viagemDAO.viagemTemCapitao(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 2));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt(), anyString());
    }

    @Test
    @DisplayName("Remover tripulante de viagem PLANEADA liberta-o em DISPONIVEL (F7)")
    void removerTripulanteLibertaTripulante() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));

        assertDoesNotThrow(() -> viagemService.removerTripulante(1, 7));
        verify(viagemDAO).removerTripulante(1, 7);
        verify(tripulanteDAO).atualizarEstado(7, "DISPONIVEL");
    }

    // =========================================================================
    // helpers
    // =========================================================================

    private Navio navio(EstadoNavio estado) {
        Navio n = new Navio();
        n.setId(1);
        n.setEstado(estado);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);
        return n;
    }

    private Viagem viagem(int id, EstadoViagem estado) {
        Viagem v = new Viagem();
        v.setId(id);
        v.setNavioId(1);
        v.setEstado(estado);
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
