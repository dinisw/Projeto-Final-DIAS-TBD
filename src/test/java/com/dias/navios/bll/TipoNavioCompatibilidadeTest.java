package com.dias.navios.bll;

import com.dias.navios.model.TipoCarga;
import com.dias.navios.model.TipoNavio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cobre todas as 24 combinações TipoNavio × TipoCarga.
 * Não precisa de BD — testa lógica pura do enum.
 */
@DisplayName("Compatibilidade TipoNavio × TipoCarga")
class TipoNavioCompatibilidadeTest {

    // ─── CRUDE (só aceita PETROLEO_BRUTO) ────────────────────────────────────

    @Test void crude_aceita_Petroleo() {
        assertTrue(TipoNavio.CRUDE.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void crude_rejeita_Gasolina() {
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void crude_rejeita_Diesel() {
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void crude_rejeita_JetFuel() {
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void crude_rejeita_Fueloleo() {
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void crude_rejeita_Quimico() {
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.PRODUTO_QUIMICO));
    }

    // ─── REFINADO (gasolina, diesel, jet fuel, fuelóleo) ─────────────────────

    @Test void refinado_rejeita_Petroleo() {
        assertFalse(TipoNavio.REFINADO.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void refinado_aceita_Gasolina() {
        assertTrue(TipoNavio.REFINADO.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void refinado_aceita_Diesel() {
        assertTrue(TipoNavio.REFINADO.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void refinado_aceita_JetFuel() {
        assertTrue(TipoNavio.REFINADO.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void refinado_aceita_Fueloleo() {
        assertTrue(TipoNavio.REFINADO.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void refinado_rejeita_Quimico() {
        assertFalse(TipoNavio.REFINADO.aceitaCarga(TipoCarga.PRODUTO_QUIMICO));
    }

    // ─── QUIMICO (só aceita PRODUTO_QUIMICO) ─────────────────────────────────

    @Test void quimico_rejeita_Petroleo() {
        assertFalse(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void quimico_rejeita_Gasolina() {
        assertFalse(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void quimico_rejeita_Diesel() {
        assertFalse(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void quimico_rejeita_JetFuel() {
        assertFalse(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void quimico_rejeita_Fueloleo() {
        assertFalse(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void quimico_aceita_ProdutoQuimico() {
        assertTrue(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.PRODUTO_QUIMICO));
    }

    // ─── QUIMICO_PRODUTO (refinados + químico, mas NÃO crude) ────────────────

    @Test void quimicoProduto_rejeita_Petroleo() {
        assertFalse(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void quimicoProduto_aceita_Gasolina() {
        assertTrue(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void quimicoProduto_aceita_Diesel() {
        assertTrue(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void quimicoProduto_aceita_JetFuel() {
        assertTrue(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void quimicoProduto_aceita_Fueloleo() {
        assertTrue(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void quimicoProduto_aceita_ProdutoQuimico() {
        assertTrue(TipoNavio.QUIMICO_PRODUTO.aceitaCarga(TipoCarga.PRODUTO_QUIMICO));
    }
}
