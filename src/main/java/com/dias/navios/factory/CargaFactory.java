package com.dias.navios.factory;

import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;

public class CargaFactory {

    public static Carga criarPetroleo(String designacao, double volume, double peso, int portoCarregId, int portoDescargaId) {
        Carga carga = new Carga();
        carga.setDesignacao(designacao);
        carga.setTipo(TipoCarga.PETROLEO_BRUTO);
        carga.setVolume(volume);
        carga.setPeso(peso);
        carga.setInflamavel(true);
        carga.setCorrosiva(false);
        carga.setToxica(false);
        carga.setPortoCarregamentoId(portoCarregId);
        carga.setPortoDescargaId(portoDescargaId);
        return carga;
    }

    public static Carga criarProdutoQuimico(String designacao, double volume, double peso, int portoCarregId, int portoDescargaId) {
        Carga carga = new Carga();
        carga.setDesignacao(designacao);
        carga.setTipo(TipoCarga.PRODUTO_QUIMICO);
        carga.setVolume(volume);
        carga.setPeso(peso);
        carga.setInflamavel(false);
        carga.setCorrosiva(true);
        carga.setToxica(true);
        carga.setPortoCarregamentoId(portoCarregId);
        carga.setPortoDescargaId(portoDescargaId);
        return carga;
    }

    public static Carga criarGasolina(String designacao, double volume, double peso, int portoCarregId, int portoDescargaId) {
        Carga carga = new Carga();
        carga.setDesignacao(designacao);
        carga.setTipo(TipoCarga.GASOLINA);
        carga.setVolume(volume);
        carga.setPeso(peso);
        carga.setInflamavel(true);
        carga.setCorrosiva(false);
        carga.setToxica(false);
        carga.setPortoCarregamentoId(portoCarregId);
        carga.setPortoDescargaId(portoDescargaId);
        return carga;
    }
}
