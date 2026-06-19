package com.dias.navios.factory;

import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CargaFactory — padrão de criação de cargas por tipo")
class CargaFactoryTest {

    @Test
    @DisplayName("criarPetroleo — tipo correto e inflamável")
    void criarPetroleoCorreto() {
        Carga c = CargaFactory.criarPetroleo("Crude Árabe", 12_000, 10_000, 1, 2);
        assertEquals(TipoCarga.PETROLEO_BRUTO, c.getTipo());
        assertEquals("Crude Árabe", c.getDesignacao());
        assertEquals(12_000, c.getVolume());
        assertEquals(10_000, c.getPeso());
        assertTrue(c.isInflamavel());
        assertFalse(c.isCorrosiva());
        assertFalse(c.isToxica());
        assertEquals(1, c.getPortoCarregamentoId());
        assertEquals(2, c.getPortoDescargaId());
    }

    @Test
    @DisplayName("criarProdutoQuimico — tipo correto, corrosivo e tóxico")
    void criarProdutoQuimicoCorreto() {
        Carga c = CargaFactory.criarProdutoQuimico("Soda Cáustica", 5_000, 6_000, 3, 4);
        assertEquals(TipoCarga.QUIMICOS, c.getTipo());
        assertEquals("Soda Cáustica", c.getDesignacao());
        assertFalse(c.isInflamavel());
        assertTrue(c.isCorrosiva());
        assertTrue(c.isToxica());
    }

    @Test
    @DisplayName("criarGasolina — tipo correto e inflamável")
    void criarGasolinaCorreto() {
        Carga c = CargaFactory.criarGasolina("Gasolina 95", 8_000, 6_000, 1, 3);
        assertEquals(TipoCarga.GASOLINA, c.getTipo());
        assertTrue(c.isInflamavel());
        assertFalse(c.isCorrosiva());
        assertFalse(c.isToxica());
    }

    @Test
    @DisplayName("Factory devolve instâncias distintas a cada chamada")
    void factoryDevolvemInstanciasDistintas() {
        Carga c1 = CargaFactory.criarPetroleo("A", 1000, 800, 1, 2);
        Carga c2 = CargaFactory.criarPetroleo("A", 1000, 800, 1, 2);
        assertNotSame(c1, c2);
    }

    @Test
    @DisplayName("Petróleo é compatível com navio CRUDE")
    void petroleoCompatívelComNavioCrude() {
        Carga c = CargaFactory.criarPetroleo("Crude", 1000, 800, 1, 2);
        assertTrue(com.dias.navios.model.TipoNavio.CRUDE.aceitaCarga(c.getTipo()));
        assertFalse(com.dias.navios.model.TipoNavio.REFINADOS.aceitaCarga(c.getTipo()));
        assertFalse(com.dias.navios.model.TipoNavio.QUIMICO.aceitaCarga(c.getTipo()));
    }

    @Test
    @DisplayName("Produto químico não é compatível com navio CRUDE nem REFINADOS")
    void produtoQuimicoIncompativelComCrudeERefinados() {
        Carga c = CargaFactory.criarProdutoQuimico("Químico", 1000, 900, 1, 2);
        assertFalse(com.dias.navios.model.TipoNavio.CRUDE.aceitaCarga(c.getTipo()));
        assertFalse(com.dias.navios.model.TipoNavio.REFINADOS.aceitaCarga(c.getTipo()));
        assertTrue(com.dias.navios.model.TipoNavio.QUIMICO.aceitaCarga(c.getTipo()));
        assertTrue(com.dias.navios.model.TipoNavio.QUIMICO_PRODUTOS.aceitaCarga(c.getTipo()));
    }
}
