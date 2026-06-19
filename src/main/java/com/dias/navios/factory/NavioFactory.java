package com.dias.navios.factory;

import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;

public class NavioFactory {

    // Cria um navio Petroleiro de Crude
    public static Navio criarNavioCrude(String nome, String codigoIMO, double capacidade, int numTanques, String bandeira, int anoFabrico) {
        Navio navio = new Navio();
        navio.setNome(nome);
        navio.setCodigoIMO(codigoIMO);
        navio.setTipo(TipoNavio.CRUDE);
        navio.setCapacidadeMaxima(capacidade);
        navio.setNumTanques(numTanques);
        navio.setBandeira(bandeira);
        navio.setAnoFabrico(anoFabrico);
        navio.setEstado(EstadoNavio.ATIVO);
        return navio;
    }

    // Cria um navio de Produtos Refinados
    public static Navio criarNavioRefinado(String nome, String codigoIMO, double capacidade, int numTanques, String bandeira, int anoFabrico) {
        Navio navio = new Navio();
        navio.setNome(nome);
        navio.setCodigoIMO(codigoIMO);
        navio.setTipo(TipoNavio.REFINADOS);
        navio.setCapacidadeMaxima(capacidade);
        navio.setNumTanques(numTanques);
        navio.setBandeira(bandeira);
        navio.setAnoFabrico(anoFabrico);
        navio.setEstado(EstadoNavio.ATIVO);
        return navio;
    }

    // Cria um navio Quimico
    public static Navio criarNavioQuimico(String nome, String codigoIMO, double capacidade, int numTanques, String bandeira, int anoFabrico) {
        Navio navio = new Navio();
        navio.setNome(nome);
        navio.setCodigoIMO(codigoIMO);
        navio.setTipo(TipoNavio.QUIMICO);
        navio.setCapacidadeMaxima(capacidade);
        navio.setNumTanques(numTanques);
        navio.setBandeira(bandeira);
        navio.setAnoFabrico(anoFabrico);
        navio.setEstado(EstadoNavio.ATIVO);
        return navio;
    }

    // Cria um navio hibrido Quimico/Produtos
    public static Navio criarNavioQuimicoProduto(String nome, String codigoIMO, double capacidade, int numTanques, String bandeira, int anoFabrico) {
        Navio navio = new Navio();
        navio.setNome(nome);
        navio.setCodigoIMO(codigoIMO);
        navio.setTipo(TipoNavio.QUIMICO_PRODUTOS);
        navio.setCapacidadeMaxima(capacidade);
        navio.setNumTanques(numTanques);
        navio.setBandeira(bandeira);
        navio.setAnoFabrico(anoFabrico);
        navio.setEstado(EstadoNavio.ATIVO);
        return navio;
    }
}
