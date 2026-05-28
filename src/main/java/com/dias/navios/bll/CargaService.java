package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.model.Carga;

import java.util.List;

public class CargaService {

    private CargaDAO cargaDAO = new CargaDAO();

    public void registarCarga(Carga carga) throws Exception {
        // TODO: validar campos obrigatorios
        if (carga.getDesignacao() == null || carga.getDesignacao().isEmpty()) {
            throw new IllegalArgumentException("A designacao da carga e obrigatoria.");
        }
        if (carga.getVolume() <= 0 || carga.getPeso() <= 0) {
            throw new IllegalArgumentException("Volume e peso devem ser maiores que zero.");
        }
        cargaDAO.inserir(carga);
    }

    public void editarCarga(Carga carga) throws Exception {
        // TODO: validar antes de atualizar
        cargaDAO.atualizar(carga);
    }

    public void apagarCarga(int id) throws Exception {
        // TODO: verificar se a carga nao esta associada a uma viagem ativa
        cargaDAO.apagar(id);
    }

    public Carga buscarCarga(int id) throws Exception {
        return cargaDAO.buscarPorId(id);
    }

    public List<Carga> listarCargas() throws Exception {
        return cargaDAO.listarTodos();
    }
}
