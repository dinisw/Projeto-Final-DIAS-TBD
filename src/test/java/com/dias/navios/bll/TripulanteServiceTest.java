package com.dias.navios.bll;

import com.dias.navios.dal.TripulanteDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;
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
@DisplayName("TripulanteService — validações e regras de negócio")
class TripulanteServiceTest {

    @Mock TripulanteDAO tripulanteDAO;
    @Mock ViagemDAO viagemDAO;
    @InjectMocks TripulanteService tripulanteService;

    private Tripulante tripulanteValido;

    @BeforeEach
    void setUp() {
        tripulanteValido = new Tripulante();
        tripulanteValido.setNome("João Silva");
        tripulanteValido.setNumeroCertificado("CERT-2024-001");
        tripulanteValido.setFuncao(FuncaoTripulante.CAPITAO);
        tripulanteValido.setEstadoDisponibilidade("DISPONIVEL");
    }

    // ─── registarTripulante ───────────────────────────────────────────────────

    @Test
    @DisplayName("Tripulante válido é registado com sucesso")
    void tripulanteValidoERegistado() throws Exception {
        assertDoesNotThrow(() -> tripulanteService.registarTripulante(tripulanteValido));
        verify(tripulanteDAO).inserir(tripulanteValido);
    }

    @Test
    @DisplayName("Nome nulo é rejeitado")
    void nomeNuloEhRejeitado() {
        tripulanteValido.setNome(null);
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.registarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }

    @Test
    @DisplayName("Nome em branco é rejeitado")
    void nomeBrancoEhRejeitado() {
        tripulanteValido.setNome("  ");
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.registarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }

    @Test
    @DisplayName("Número de certificado nulo é rejeitado")
    void certificadoNuloEhRejeitado() {
        tripulanteValido.setNumeroCertificado(null);
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.registarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }

    @Test
    @DisplayName("Número de certificado em branco é rejeitado")
    void certificadoBrancoEhRejeitado() {
        tripulanteValido.setNumeroCertificado("");
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.registarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }

    @Test
    @DisplayName("Função nula é rejeitada")
    void funcaoNulaEhRejeitada() {
        tripulanteValido.setFuncao(null);
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.registarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }

    @Test
    @DisplayName("Todas as funções válidas são aceites")
    void todasAsFuncoesValidasSaoAceites() throws Exception {
        for (FuncaoTripulante funcao : FuncaoTripulante.values()) {
            tripulanteValido.setFuncao(funcao);
            tripulanteValido.setNumeroCertificado("CERT-" + funcao.name());
            assertDoesNotThrow(() -> tripulanteService.registarTripulante(tripulanteValido),
                    "Função " + funcao + " deveria ser aceite");
        }
    }

    // ─── apagarTripulante ────────────────────────────────────────────────────

    @Test
    @DisplayName("Tripulante sem viagem ativa pode ser apagado")
    void tripulanteSemViagemAtivaEApagado() throws Exception {
        when(viagemDAO.tripulanteTemViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> tripulanteService.apagarTripulante(1));
        verify(tripulanteDAO).apagar(1);
    }

    @Test
    @DisplayName("Tripulante com viagem ativa não pode ser apagado")
    void tripulanteComViagemAtivaNaoPodeSerApagado() throws Exception {
        when(viagemDAO.tripulanteTemViagemAtiva(1)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> tripulanteService.apagarTripulante(1));
        verify(tripulanteDAO, never()).apagar(anyInt());
    }

    // ─── editarTripulante ────────────────────────────────────────────────────

    @Test
    @DisplayName("Edição com dados válidos é aceite")
    void edicaoValidaEAceite() throws Exception {
        tripulanteValido.setId(1);
        assertDoesNotThrow(() -> tripulanteService.editarTripulante(tripulanteValido));
        verify(tripulanteDAO).atualizar(tripulanteValido);
    }

    @Test
    @DisplayName("Edição com nome em branco é rejeitada")
    void edicaoComNomeBrancoEhRejeitada() {
        tripulanteValido.setNome("");
        assertThrows(IllegalArgumentException.class,
                () -> tripulanteService.editarTripulante(tripulanteValido));
        verifyNoInteractions(tripulanteDAO);
    }
}
