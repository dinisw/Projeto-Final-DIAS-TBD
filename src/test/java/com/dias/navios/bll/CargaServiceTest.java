package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;
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
@DisplayName("CargaService — validações e regras de negócio")
class CargaServiceTest {

    @Mock CargaDAO cargaDAO;
    @Mock ViagemDAO viagemDAO;
    @InjectMocks CargaService cargaService;

    private Carga cargaValida;

    @BeforeEach
    void setUp() {
        cargaValida = new Carga();
        cargaValida.setDesignacao("Petróleo Bruto Árabe");
        cargaValida.setTipo(TipoCarga.PETROLEO_BRUTO);
        cargaValida.setVolume(12_000);
        cargaValida.setPeso(10_000);
        cargaValida.setInflamavel(true);
        cargaValida.setCorrosiva(false);
        cargaValida.setToxica(false);
    }

    // ─── registarCarga ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga válida é registada com sucesso")
    void cargaValidaERegistada() throws Exception {
        assertDoesNotThrow(() -> cargaService.registarCarga(cargaValida));
        verify(cargaDAO).inserir(cargaValida);
    }

    @Test
    @DisplayName("Designação nula é rejeitada")
    void designacaoNulaEhRejeitada() {
        cargaValida.setDesignacao(null);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Designação em branco é rejeitada")
    void designacaoBrancoEhRejeitada() {
        cargaValida.setDesignacao("  ");
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Tipo de carga nulo é rejeitado")
    void tipoNuloEhRejeitado() {
        cargaValida.setTipo(null);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Volume zero é rejeitado")
    void volumeZeroEhRejeitado() {
        cargaValida.setVolume(0);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Volume negativo é rejeitado")
    void volumeNegativoEhRejeitado() {
        cargaValida.setVolume(-100);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Peso zero é rejeitado")
    void pesoZeroEhRejeitado() {
        cargaValida.setPeso(0);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    @Test
    @DisplayName("Peso negativo é rejeitado")
    void pesoNegativoEhRejeitado() {
        cargaValida.setPeso(-500);
        assertThrows(IllegalArgumentException.class, () -> cargaService.registarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }

    // ─── apagarCarga ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carga sem viagem ativa pode ser apagada")
    void cargaSemViagemAtivaEApagada() throws Exception {
        when(viagemDAO.cargaEstaEmViagemAtiva(1)).thenReturn(false);

        assertDoesNotThrow(() -> cargaService.apagarCarga(1));
        verify(cargaDAO).apagar(1);
    }

    @Test
    @DisplayName("Carga em viagem ativa não pode ser apagada")
    void cargaEmViagemAtivaNaoPodeSerApagada() throws Exception {
        when(viagemDAO.cargaEstaEmViagemAtiva(1)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> cargaService.apagarCarga(1));
        verify(cargaDAO, never()).apagar(anyInt());
    }

    // ─── editarCarga ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("Edição com dados válidos é aceite")
    void edicaoValidaEAceite() throws Exception {
        cargaValida.setId(1);
        assertDoesNotThrow(() -> cargaService.editarCarga(cargaValida));
        verify(cargaDAO).atualizar(cargaValida);
    }

    @Test
    @DisplayName("Edição com peso zero é rejeitada")
    void edicaoComPesoZeroEhRejeitada() {
        cargaValida.setPeso(0);
        assertThrows(IllegalArgumentException.class, () -> cargaService.editarCarga(cargaValida));
        verifyNoInteractions(cargaDAO);
    }
}
