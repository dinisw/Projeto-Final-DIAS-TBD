package com.dias.navios.bll;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NavioService — validações e regras de negócio")
class NavioServiceTest {

    @Mock NavioDAO navioDAO;
    @Mock ViagemDAO viagemDAO;
    @InjectMocks NavioService navioService;

    private Navio navioValido;

    @BeforeEach
    void setUp() {
        navioValido = new Navio();
        navioValido.setNome("Titan Star");
        navioValido.setCodigoIMO("IMO9876543");
        navioValido.setTipo(TipoNavio.CRUDE);
        navioValido.setCapacidadeMaxima(80_000);
        navioValido.setNumTanques(12);
        navioValido.setBandeira("Portugal");
        navioValido.setAnoFabrico(2010);
        navioValido.setEstado(EstadoNavio.ATIVO);
    }

    // ─── registarNavio ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Navio válido é registado com sucesso")
    void navioValidoERegistado() throws Exception {
        assertDoesNotThrow(() -> navioService.registarNavio(navioValido));
        verify(navioDAO).inserir(navioValido);
    }

    @Test
    @DisplayName("Nome em branco é rejeitado")
    void nomeBrancoEhRejeitado() {
        navioValido.setNome("   ");
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Nome nulo é rejeitado")
    void nomeNuloEhRejeitado() {
        navioValido.setNome(null);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Código IMO em branco é rejeitado")
    void codigoIMOBrancoEhRejeitado() {
        navioValido.setCodigoIMO("");
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Código IMO nulo é rejeitado")
    void codigoIMONuloEhRejeitado() {
        navioValido.setCodigoIMO(null);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Tipo de navio nulo é rejeitado")
    void tipoNuloEhRejeitado() {
        navioValido.setTipo(null);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Capacidade zero é rejeitada")
    void capacidadeZeroEhRejeitada() {
        navioValido.setCapacidadeMaxima(0);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Capacidade negativa é rejeitada")
    void capacidadeNegativaEhRejeitada() {
        navioValido.setCapacidadeMaxima(-1);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Número de tanques zero é rejeitado")
    void tanquesZeroEhRejeitado() {
        navioValido.setNumTanques(0);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    @Test
    @DisplayName("Estado nulo é rejeitado")
    void estadoNuloEhRejeitado() {
        navioValido.setEstado(null);
        assertThrows(IllegalArgumentException.class, () -> navioService.registarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }

    // ─── apagarNavio ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Navio sem viagens ativas pode ser apagado")
    void navioSemViagensAtivasEApagado() throws Exception {
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> navioService.apagarNavio(1));
        verify(navioDAO).apagar(1);
    }

    @Test
    @DisplayName("Navio com viagem PLANEADA não pode ser apagado")
    void navioComViagemPlaneadaNaoPodeSerApagado() throws Exception {
        when(viagemDAO.navioTemViagemAtiva(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> navioService.apagarNavio(1));
        verify(navioDAO, never()).apagar(anyInt());
    }

    // ─── editarNavio ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Edição com dados válidos é aceite")
    void edicaoComDadosValidosEAceite() throws Exception {
        navioValido.setId(1);
        assertDoesNotThrow(() -> navioService.editarNavio(navioValido));
        verify(navioDAO).atualizar(navioValido);
    }

    @Test
    @DisplayName("Edição com nome em branco é rejeitada")
    void edicaoComNomeBrancoEhRejeitada() {
        navioValido.setNome("");
        assertThrows(IllegalArgumentException.class, () -> navioService.editarNavio(navioValido));
        verifyNoInteractions(navioDAO);
    }
}
