package com.dias.navios.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Modelos Tripulante, Porto e Viagem")
class ModeloTripulantePortoViagemTest {

    // ─── Tripulante ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("Tripulante — construtor completo e getters")
    void tripulanteConstrutorCompleto() {
        Tripulante t = new Tripulante(1, "João Silva", "CERT-001",
                FuncaoTripulante.CAPITAO, true, "Portuguesa");
        assertEquals(1, t.getId());
        assertEquals("João Silva", t.getNome());
        assertEquals("CERT-001", t.getNumeroCertificado());
        assertEquals(FuncaoTripulante.CAPITAO, t.getFuncao());
        assertTrue(t.isDisponivel());
        assertEquals("Portuguesa", t.getNacionalidade());
    }

    @Test
    @DisplayName("Tripulante — disponibilidade pode ser alterada")
    void tripulanteDisponibilidadeAlteravel() {
        Tripulante t = new Tripulante();
        t.setDisponivel(true);
        assertTrue(t.isDisponivel());
        t.setDisponivel(false);
        assertFalse(t.isDisponivel());
    }

    @Test
    @DisplayName("FuncaoTripulante tem os 4 valores obrigatórios do enunciado")
    void funcaoTripulanteTemQuatroValores() {
        FuncaoTripulante[] funcoes = FuncaoTripulante.values();
        assertEquals(4, funcoes.length);
        assertNotNull(FuncaoTripulante.CAPITAO);
        assertNotNull(FuncaoTripulante.OFICIAL);
        assertNotNull(FuncaoTripulante.ENGENHEIRO);
        assertNotNull(FuncaoTripulante.OPERADOR);
    }

    // ─── Porto ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Porto — construtor completo e getters")
    void portoConstrutorCompleto() {
        Porto p = new Porto(1, "Porto de Leixões", "Portugal", "PTLEI");
        assertEquals(1, p.getId());
        assertEquals("Porto de Leixões", p.getNome());
        assertEquals("Portugal", p.getPais());
        assertEquals("PTLEI", p.getCodigo());
    }

    @Test
    @DisplayName("Porto — toString inclui nome e código")
    void portoToStringIncluiNomeECodigo() {
        Porto p = new Porto(1, "Rotterdam", "Países Baixos", "NLRTM");
        String s = p.toString();
        assertTrue(s.contains("Rotterdam"));
        assertTrue(s.contains("NLRTM"));
    }

    @Test
    @DisplayName("Porto — construtor por defeito permite setters")
    void portoConstrutorPorDefeito() {
        Porto p = new Porto();
        p.setNome("Sines");
        p.setPais("Portugal");
        p.setCodigo("PTSIN");
        assertEquals("Sines", p.getNome());
        assertEquals("Portugal", p.getPais());
        assertEquals("PTSIN", p.getCodigo());
    }

    // ─── Viagem ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Viagem — construtor por defeito inicializa listas não nulas")
    void viagemConstrutorPorDefeitoListasNaoNulas() {
        Viagem v = new Viagem();
        assertNotNull(v.getCargasIds());
        assertNotNull(v.getTripulantesIds());
        assertTrue(v.getCargasIds().isEmpty());
        assertTrue(v.getTripulantesIds().isEmpty());
    }

    @Test
    @DisplayName("Viagem — construtor completo inicializa listas")
    void viagemConstrutorCompletoInicializaListas() {
        Viagem v = new Viagem(1, 1, 2,
                LocalDate.of(2025, 8, 1),
                LocalDate.of(2025, 8, 10),
                5, EstadoViagem.PLANEADA);
        assertNotNull(v.getCargasIds());
        assertNotNull(v.getTripulantesIds());
        assertEquals(EstadoViagem.PLANEADA, v.getEstado());
    }

    @Test
    @DisplayName("Viagem — adicionar cargasIds e tripulantesIds")
    void viagemAdicionarAssociacoes() {
        Viagem v = new Viagem();
        v.getCargasIds().add(10);
        v.getCargasIds().add(20);
        v.getTripulantesIds().add(5);

        assertEquals(2, v.getCargasIds().size());
        assertTrue(v.getCargasIds().contains(10));
        assertTrue(v.getCargasIds().contains(20));
        assertEquals(1, v.getTripulantesIds().size());
        assertTrue(v.getTripulantesIds().contains(5));
    }

    @Test
    @DisplayName("EstadoViagem tem os 4 estados obrigatórios do enunciado")
    void estadoViagemTemQuatroEstados() {
        EstadoViagem[] estados = EstadoViagem.values();
        assertEquals(4, estados.length);
        assertNotNull(EstadoViagem.PLANEADA);
        assertNotNull(EstadoViagem.EM_CURSO);
        assertNotNull(EstadoViagem.CONCLUIDA);
        assertNotNull(EstadoViagem.CANCELADA);
    }

    @Test
    @DisplayName("Viagem — toString inclui id e estado")
    void viagemToStringIncluiIdEEstado() {
        Viagem v = new Viagem(42, 1, 2,
                LocalDate.of(2025, 8, 1), null, 3, EstadoViagem.EM_CURSO);
        String s = v.toString();
        assertTrue(s.contains("42"));
        assertTrue(s.contains("EM_CURSO"));
    }
}
