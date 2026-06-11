package com.dias.navios.bll;

import com.dias.navios.dal.TripulanteDAO;
import com.dias.navios.model.Tripulante;

import java.util.List;

public class TripulanteService {

    private TripulanteDAO tripulanteDAO = new TripulanteDAO();

    public void registarTripulante(Tripulante t) throws Exception {
        // TODO: validar campos obrigatorios
        if (t.getNome() == null || t.getNome().isEmpty()) {
            throw new IllegalArgumentException("O nome do tripulante e obrigatorio.");
        }
        tripulanteDAO.inserir(t);
    }

    public void editarTripulante(Tripulante t) throws Exception {
        // TODO: validar antes de atualizar
        tripulanteDAO.atualizar(t);
    }

    public void apagarTripulante(int id) throws Exception {
        // TODO: verificar historico antes de apagar
        tripulanteDAO.apagar(id);
    }

    public Tripulante buscarTripulante(int id) throws Exception {
        return tripulanteDAO.buscarPorId(id);
    }

    public List<Tripulante> listarTripulantes() throws Exception {
        return tripulanteDAO.listarTodos();
    }
}
