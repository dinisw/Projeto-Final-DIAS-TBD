package com.dias.navios.factory;

import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NavioFactory — padrão de criação de navios por tipo")
class NavioFactoryTest {

    private static final String NOME  = "Titan Star";
    private static final String IMO   = "IMO9876543";
    private static final double CAP   = 80_000;
    private static final int    TAN   = 12;
    private static final String BAND  = "Portugal";
    private static final int    ANO   = 2010;

    @Test
    @DisplayName("criarNavioCrude devolve navio do tipo CRUDE ativo")
    void criarNavioCrudeCorreto() {
        Navio n = NavioFactory.criarNavioCrude(NOME, IMO, CAP, TAN, BAND, ANO);
        assertEquals(TipoNavio.CRUDE, n.getTipo());
        assertEquals(EstadoNavio.ATIVO, n.getEstado());
        assertEquals(NOME, n.getNome());
        assertEquals(IMO, n.getCodigoIMO());
        assertEquals(CAP, n.getCapacidadeMaxima());
        assertEquals(TAN, n.getNumTanques());
        assertEquals(BAND, n.getBandeira());
        assertEquals(ANO, n.getAnoFabrico());
    }

    @Test
    @DisplayName("criarNavioRefinado devolve navio do tipo REFINADOS ativo")
    void criarNavioRefinadoCorreto() {
        Navio n = NavioFactory.criarNavioRefinado(NOME, IMO, CAP, TAN, BAND, ANO);
        assertEquals(TipoNavio.REFINADOS, n.getTipo());
        assertEquals(EstadoNavio.ATIVO, n.getEstado());
    }

    @Test
    @DisplayName("criarNavioQuimico devolve navio do tipo QUIMICO ativo")
    void criarNavioQuimicoCorreto() {
        Navio n = NavioFactory.criarNavioQuimico(NOME, IMO, CAP, TAN, BAND, ANO);
        assertEquals(TipoNavio.QUIMICO, n.getTipo());
        assertEquals(EstadoNavio.ATIVO, n.getEstado());
    }

    @Test
    @DisplayName("criarNavioQuimicoProduto devolve navio do tipo QUIMICO_PRODUTOS ativo")
    void criarNavioQuimicoProdutoCorreto() {
        Navio n = NavioFactory.criarNavioQuimicoProduto(NOME, IMO, CAP, TAN, BAND, ANO);
        assertEquals(TipoNavio.QUIMICO_PRODUTOS, n.getTipo());
        assertEquals(EstadoNavio.ATIVO, n.getEstado());
    }

    @Test
    @DisplayName("Cada factory devolve uma instância distinta")
    void factoryDevolvemInstanciasDistintas() {
        Navio n1 = NavioFactory.criarNavioCrude(NOME, IMO, CAP, TAN, BAND, ANO);
        Navio n2 = NavioFactory.criarNavioCrude(NOME, IMO, CAP, TAN, BAND, ANO);
        assertNotSame(n1, n2);
    }

    @Test
    @DisplayName("Navio criado pela factory aceita as suas cargas compatíveis")
    void navioFactoryAceitaCargasCompativeis() {
        Navio crude    = NavioFactory.criarNavioCrude(NOME, IMO, CAP, TAN, BAND, ANO);
        Navio refinado = NavioFactory.criarNavioRefinado(NOME, IMO, CAP, TAN, BAND, ANO);
        Navio quimico  = NavioFactory.criarNavioQuimico(NOME, IMO, CAP, TAN, BAND, ANO);
        Navio hibrido  = NavioFactory.criarNavioQuimicoProduto(NOME, IMO, CAP, TAN, BAND, ANO);

        assertTrue(crude.getTipo().aceitaCarga(com.dias.navios.model.TipoCarga.PETROLEO_BRUTO));
        assertTrue(refinado.getTipo().aceitaCarga(com.dias.navios.model.TipoCarga.GASOLINA));
        assertTrue(quimico.getTipo().aceitaCarga(com.dias.navios.model.TipoCarga.QUIMICOS));
        assertTrue(hibrido.getTipo().aceitaCarga(com.dias.navios.model.TipoCarga.GASOLINA));
        assertTrue(hibrido.getTipo().aceitaCarga(com.dias.navios.model.TipoCarga.QUIMICOS));
    }
}
