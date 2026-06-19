package com.dias.navios.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modelo Navio — construtores, getters/setters, toString")
class ModeloNavioTest {

    @Test
    @DisplayName("Construtor por defeito cria navio vazio")
    void construtorPorDefeitoCriaNavioVazio() {
        Navio n = new Navio();
        assertEquals(0, n.getId());
        assertNull(n.getNome());
        assertNull(n.getCodigoIMO());
        assertNull(n.getTipo());
        assertNull(n.getEstado());
    }

    @Test
    @DisplayName("Construtor completo preenche todos os campos")
    void construtorCompletoPreencheTodosCampos() {
        Navio n = new Navio(1, "Titan Star", "IMO9876543", TipoNavio.CRUDE,
                80_000, 12, "Portugal", 2010, EstadoNavio.ATIVO, 5);
        assertEquals(1, n.getId());
        assertEquals("Titan Star", n.getNome());
        assertEquals("IMO9876543", n.getCodigoIMO());
        assertEquals(TipoNavio.CRUDE, n.getTipo());
        assertEquals(80_000, n.getCapacidadeMaxima());
        assertEquals(12, n.getNumTanques());
        assertEquals("Portugal", n.getBandeira());
        assertEquals(2010, n.getAnoFabrico());
        assertEquals(EstadoNavio.ATIVO, n.getEstado());
        assertEquals(5, n.getPortoAtualId());
    }

    @Test
    @DisplayName("Setters alteram os valores corretamente")
    void settersAlteramValoresCorretamente() {
        Navio n = new Navio();
        n.setNome("Aurora");
        n.setCodigoIMO("IMO1111111");
        n.setTipo(TipoNavio.QUIMICO);
        n.setCapacidadeMaxima(50_000);
        n.setNumTanques(8);
        n.setBandeira("Espanha");
        n.setAnoFabrico(2015);
        n.setEstado(EstadoNavio.MANUTENCAO);
        n.setPortoAtualId(3);

        assertEquals("Aurora", n.getNome());
        assertEquals("IMO1111111", n.getCodigoIMO());
        assertEquals(TipoNavio.QUIMICO, n.getTipo());
        assertEquals(50_000, n.getCapacidadeMaxima());
        assertEquals(8, n.getNumTanques());
        assertEquals("Espanha", n.getBandeira());
        assertEquals(2015, n.getAnoFabrico());
        assertEquals(EstadoNavio.MANUTENCAO, n.getEstado());
        assertEquals(3, n.getPortoAtualId());
    }

    @Test
    @DisplayName("toString inclui nome e código IMO")
    void toStringIncluiNomeEIMO() {
        Navio n = new Navio(1, "Titan Star", "IMO9876543", TipoNavio.CRUDE,
                80_000, 12, "Portugal", 2010, EstadoNavio.ATIVO, 0);
        String s = n.toString();
        assertTrue(s.contains("Titan Star"));
        assertTrue(s.contains("IMO9876543"));
    }

    @Test
    @DisplayName("EstadoNavio tem os 3 estados obrigatórios do enunciado")
    void estadoNavioTemTresEstados() {
        EstadoNavio[] estados = EstadoNavio.values();
        assertEquals(3, estados.length);
        assertArrayEquals(new EstadoNavio[]{
                EstadoNavio.ATIVO, EstadoNavio.MANUTENCAO, EstadoNavio.INATIVO
        }, estados);
    }

    @Test
    @DisplayName("TipoNavio tem os 4 tipos obrigatórios do enunciado")
    void tipoNavioTemQuatroTipos() {
        TipoNavio[] tipos = TipoNavio.values();
        assertEquals(4, tipos.length);
        assertArrayEquals(new TipoNavio[]{
                TipoNavio.CRUDE, TipoNavio.REFINADOS, TipoNavio.QUIMICO, TipoNavio.QUIMICO_PRODUTOS
        }, tipos);
    }
}
