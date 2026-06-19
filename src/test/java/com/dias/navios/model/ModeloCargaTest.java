package com.dias.navios.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modelo Carga — construtores, getters/setters, propriedades")
class ModeloCargaTest {

    @Test
    @DisplayName("Construtor por defeito cria carga vazia")
    void construtorPorDefeitoCriaCargaVazia() {
        Carga c = new Carga();
        assertEquals(0, c.getId());
        assertNull(c.getDesignacao());
        assertNull(c.getTipo());
        assertEquals(0.0, c.getVolume());
        assertEquals(0.0, c.getPeso());
    }

    @Test
    @DisplayName("Construtor completo preenche todos os campos")
    void construtorCompletoPreencheTodosCampos() {
        Carga c = new Carga(1, "Petróleo Árabe", TipoCarga.PETROLEO_BRUTO,
                12_000, 10_000, true, false, false, 1, 2);
        assertEquals(1, c.getId());
        assertEquals("Petróleo Árabe", c.getDesignacao());
        assertEquals(TipoCarga.PETROLEO_BRUTO, c.getTipo());
        assertEquals(12_000, c.getVolume());
        assertEquals(10_000, c.getPeso());
        assertTrue(c.isInflamavel());
        assertFalse(c.isCorrosiva());
        assertFalse(c.isToxica());
        assertEquals(1, c.getPortoCarregamentoId());
        assertEquals(2, c.getPortoDescargaId());
    }

    @Test
    @DisplayName("Propriedades de segurança são independentes")
    void propriedadesSegurancaSaoIndependentes() {
        Carga c = new Carga();
        c.setInflamavel(true);
        c.setCorrosiva(true);
        c.setToxica(false);

        assertTrue(c.isInflamavel());
        assertTrue(c.isCorrosiva());
        assertFalse(c.isToxica());
    }

    @Test
    @DisplayName("toString inclui designação e tipo")
    void toStringIncluiDesignacaoETipo() {
        Carga c = new Carga(1, "Crude Árabe", TipoCarga.PETROLEO_BRUTO,
                1000, 800, true, false, false, 1, 2);
        String s = c.toString();
        assertTrue(s.contains("Crude Árabe"));
        assertTrue(s.contains("PETROLEO_BRUTO"));
    }

    @Test
    @DisplayName("TipoCarga tem os 6 tipos obrigatórios do enunciado")
    void tipoCargaTemSeisTipos() {
        TipoCarga[] tipos = TipoCarga.values();
        assertEquals(6, tipos.length);
        // Verifica cada tipo do enunciado
        assertNotNull(TipoCarga.PETROLEO_BRUTO);
        assertNotNull(TipoCarga.GASOLINA);
        assertNotNull(TipoCarga.DIESEL);
        assertNotNull(TipoCarga.JET_FUEL);
        assertNotNull(TipoCarga.FUELOLEO);
        assertNotNull(TipoCarga.PRODUTO_QUIMICO);
    }
}
