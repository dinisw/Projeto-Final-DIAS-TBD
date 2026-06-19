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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testa ViagemService.criarViagem — validações que disparam ANTES
 * de qualquer acesso à BD (navioId, portos, datas).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ViagemService — validações básicas de campos")
class ViagemServiceValidacaoTest {

    @Mock ViagemDAO viagemDAO;
    @Mock NavioDAO navioDAO;
    @Mock CargaDAO cargaDAO;
    @InjectMocks ViagemService viagemService;

    private Viagem viagem;

    @BeforeEach
    void setUp() {
        viagem = new Viagem();
        viagem.setNavioId(1);
        viagem.setPortoOrigemId(1);
        viagem.setPortoDestinoId(2);
        viagem.setDataPartida(LocalDate.of(2025, 7, 1));
        viagem.setDataChegadaPrevista(LocalDate.of(2025, 7, 10));
    }

    @Test
    @DisplayName("navioId=0 deve lançar IllegalArgumentException antes de aceder à BD")
    void navioIdZeroDeveLancarExcecao() {
        viagem.setNavioId(0);
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("portoOrigemId=0 deve lançar exceção")
    void portoOrigemZeroDeveLancarExcecao() {
        viagem.setPortoOrigemId(0);
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("portoDestinoId=0 deve lançar exceção")
    void portoDestinoZeroDeveLancarExcecao() {
        viagem.setPortoDestinoId(0);
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("origem == destino deve lançar exceção")
    void portoOrigemIgualDestinoDeveLancarExcecao() {
        viagem.setPortoOrigemId(5);
        viagem.setPortoDestinoId(5);
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("dataPartida nula deve lançar exceção")
    void dataPartidaNulaDeveLancarExcecao() {
        viagem.setDataPartida(null);
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("dataChegada anterior à dataPartida deve lançar exceção")
    void dataChegadaAnteriorAPartidaDeveLancarExcecao() {
        viagem.setDataPartida(LocalDate.of(2025, 7, 10));
        viagem.setDataChegadaPrevista(LocalDate.of(2025, 7, 5));
        assertThrows(IllegalArgumentException.class, () -> viagemService.criarViagem(viagem));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("dataChegada igual à dataPartida é válida (mesmo dia)")
    void dataChegadaIgualAPartidaEValida() throws Exception {
        LocalDate dia = LocalDate.of(2025, 7, 1);
        viagem.setDataPartida(dia);
        viagem.setDataChegadaPrevista(dia);

        Navio navio = navioAtivo();
        when(navioDAO.buscarPorId(1)).thenReturn(navio);
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> viagemService.criarViagem(viagem));
    }

    @Test
    @DisplayName("dataChegada nula é aceite (opcional)")
    void dataChegadaNulaEAceite() throws Exception {
        viagem.setDataChegadaPrevista(null);

        Navio navio = navioAtivo();
        when(navioDAO.buscarPorId(1)).thenReturn(navio);
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> viagemService.criarViagem(viagem));
    }

    // ─── helper ───────────────────────────────────────────────────────────────

    private Navio navioAtivo() {
        Navio n = new Navio();
        n.setId(1);
        n.setEstado(EstadoNavio.ATIVO);
        n.setTipo(TipoNavio.CRUDE);
        n.setCapacidadeMaxima(50000);
        return n;
    }
}
