package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.NavioDAO;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViagemService — regras de negócio (com mocks de DAO)")
class ViagemServiceRegrasNegocioTest {

    @Mock ViagemDAO viagemDAO;
    @Mock NavioDAO  navioDAO;
    @Mock CargaDAO  cargaDAO;
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
    @DisplayName("Navio EM_MANUTENCAO não pode iniciar viagem")
    void navioEmManutencaoRejeitaCriarViagem() throws Exception {
        when(navioDAO.buscarPorId(1)).thenReturn(navio(EstadoNavio.EM_MANUTENCAO));

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

        viagemService.avancarEstado(1);

        verify(viagemDAO).atualizarEstado(1, EstadoViagem.EM_CURSO);
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
        Carga c = carga(TipoCarga.PRODUTO_QUIMICO, 1_000); // CRUDE não aceita QUIMICO

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

        // Já tem 45 000 ton carregadas
        Carga existente = carga(TipoCarga.PETROLEO_BRUTO, 45_000);
        // Nova carga de 10 000 ton → 55 000 > 50 000
        Carga nova = carga(TipoCarga.PETROLEO_BRUTO, 10_000);
        nova.setId(2);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(2)).thenReturn(nova);
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
        Carga nova = carga(TipoCarga.PETROLEO_BRUTO, 10_000); // 40 000 + 10 000 = 50 000 == limite
        nova.setId(2);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(2)).thenReturn(nova);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(List.of(existente));

        assertDoesNotThrow(() -> viagemService.associarCarga(1, 2));
        verify(viagemDAO).adicionarCarga(1, 2);
    }

    @Test
    @DisplayName("Carga já associada à viagem é rejeitada")
    void cargaJaAssociadaEhRejeitada() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        v.getCargasIds().add(1); // carga 1 já está na viagem

        Navio n = navio(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50_000);
        Carga c = carga(TipoCarga.PETROLEO_BRUTO, 1_000);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);
        when(navioDAO.buscarPorId(1)).thenReturn(n);
        when(cargaDAO.buscarPorId(1)).thenReturn(c);
        when(viagemDAO.listarCargasDaViagem(1)).thenReturn(Collections.emptyList());

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
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.PLANEADA));

        assertDoesNotThrow(() -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO).adicionarTripulante(1, 1);
    }

    @Test
    @DisplayName("Tripulante já associado à viagem é rejeitado")
    void tripulanteJaAssociadoEhRejeitado() throws Exception {
        Viagem v = viagem(1, EstadoViagem.PLANEADA);
        v.getTripulantesIds().add(1);

        when(viagemDAO.buscarPorId(1)).thenReturn(v);

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt());
    }

    @Test
    @DisplayName("Não é possível adicionar tripulante a viagem CONCLUIDA")
    void naoPodeAdicionarTripulanteAViagemConcluida() throws Exception {
        when(viagemDAO.buscarPorId(1)).thenReturn(viagem(1, EstadoViagem.CONCLUIDA));

        assertThrows(IllegalStateException.class, () -> viagemService.associarTripulante(1, 1));
        verify(viagemDAO, never()).adicionarTripulante(anyInt(), anyInt());
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
