package com.dias.navios.bll;

import com.dias.navios.dal.TripulanteDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.Tripulante;
import com.dias.navios.model.Viagem;

import java.util.List;

public class TripulanteService {

    private TripulanteDAO tripulanteDAO = new TripulanteDAO();
    private ViagemDAO viagemDAO = new ViagemDAO();

    public void registarTripulante(Tripulante t) throws Exception {
        validarTripulante(t);
        tripulanteDAO.inserir(t);
    }

    public void editarTripulante(Tripulante t) throws Exception {
        validarTripulante(t);
        tripulanteDAO.atualizar(t);
    }

    public void apagarTripulante(int id) throws Exception {
        if (viagemDAO.tripulanteTemViagemAtiva(id)) {
            throw new IllegalStateException("Não é possível apagar um tripulante com viagens activas.");
        }
        tripulanteDAO.apagar(id);
    }

    public Tripulante buscarTripulante(int id) throws Exception {
        return tripulanteDAO.buscarPorId(id);
    }

    public List<Tripulante> listarTripulantes() throws Exception {
        return tripulanteDAO.listarTodos();
    }

    public List<Viagem> listarHistoricoTripulante(int tripulanteId) throws Exception {
        return viagemDAO.listarViagensDeTripulante(tripulanteId);
    }

    private void validarTripulante(Tripulante t) {
        if (t.getNome() == null || t.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do tripulante é obrigatório.");
        }
        if (t.getNumeroCertificado() == null || t.getNumeroCertificado().isBlank()) {
            throw new IllegalArgumentException("O número de certificado é obrigatório.");
        }
        if (t.getFuncao() == null) {
            throw new IllegalArgumentException("A função do tripulante é obrigatória.");
        }
    }
}
