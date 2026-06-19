package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.Carga;

import java.util.List;

public class CargaService {

    private CargaDAO cargaDAO = new CargaDAO();
    private ViagemDAO viagemDAO = new ViagemDAO();

    public void registarCarga(Carga carga) throws Exception {
        validarCarga(carga);
        cargaDAO.inserir(carga);
    }

    public void editarCarga(Carga carga) throws Exception {
        validarCarga(carga);
        cargaDAO.atualizar(carga);
    }

    public void apagarCarga(int id) throws Exception {
        if (viagemDAO.cargaEstaEmViagemAtiva(id)) {
            throw new IllegalStateException("Não é possível apagar uma carga associada a uma viagem activa.");
        }
        cargaDAO.apagar(id);
    }

    public Carga buscarCarga(int id) throws Exception {
        return cargaDAO.buscarPorId(id);
    }

    public List<Carga> listarCargas() throws Exception {
        return cargaDAO.listarTodos();
    }

    private void validarCarga(Carga carga) {
        if (carga.getDesignacao() == null || carga.getDesignacao().isBlank()) {
            throw new IllegalArgumentException("A designação da carga é obrigatória.");
        }
        if (carga.getTipo() == null) {
            throw new IllegalArgumentException("O tipo de carga é obrigatório.");
        }
        if (carga.getVolume() <= 0) {
            throw new IllegalArgumentException("O volume deve ser maior que zero.");
        }
        if (carga.getPeso() <= 0) {
            throw new IllegalArgumentException("O peso deve ser maior que zero.");
        }
    }
}
