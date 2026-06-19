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
        assertFalse(TipoNavio.CRUDE.aceitaCarga(TipoCarga.QUIMICOS));
    }

    // ─── REFINADOS (gasolina, diesel, jet fuel, fuelóleo) ────────────────────

    @Test void refinados_rejeita_Petroleo() {
        assertFalse(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void refinados_aceita_Gasolina() {
        assertTrue(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void refinados_aceita_Diesel() {
        assertTrue(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void refinados_aceita_JetFuel() {
        assertTrue(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void refinados_aceita_Fueloleo() {
        assertTrue(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void refinados_rejeita_Quimico() {
        assertFalse(TipoNavio.REFINADOS.aceitaCarga(TipoCarga.QUIMICOS));
    }

    // ─── QUIMICO (só aceita QUIMICOS) ────────────────────────────────────────

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
    @Test void quimico_aceita_Quimicos() {
        assertTrue(TipoNavio.QUIMICO.aceitaCarga(TipoCarga.QUIMICOS));
    }

    // ─── QUIMICO_PRODUTOS (químicos + gasolina + diesel + jet fuel) ───────────

    @Test void quimicoProdutos_rejeita_Petroleo() {
        assertFalse(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.PETROLEO_BRUTO));
    }
    @Test void quimicoProdutos_aceita_Gasolina() {
        assertTrue(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.GASOLINA));
    }
    @Test void quimicoProdutos_aceita_Diesel() {
        assertTrue(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.DIESEL));
    }
    @Test void quimicoProdutos_aceita_JetFuel() {
        assertTrue(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.JET_FUEL));
    }
    @Test void quimicoProdutos_rejeita_Fueloleo() {
        assertFalse(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.FUELOLEO));
    }
    @Test void quimicoProdutos_aceita_Quimicos() {
        assertTrue(TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(TipoCarga.QUIMICOS));
    }
}
