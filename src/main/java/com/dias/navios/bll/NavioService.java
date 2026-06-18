package com.dias.navios.bll;

import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;

import java.util.List;

public class NavioService {

    private NavioDAO navioDAO = new NavioDAO();
    private ViagemDAO viagemDAO = new ViagemDAO();

    public void registarNavio(Navio navio) throws Exception {
        validarNavio(navio);
        navioDAO.inserir(navio);
    }

    public void editarNavio(Navio navio) throws Exception {
        validarNavio(navio);
        navioDAO.atualizar(navio);
    }

    public void apagarNavio(int id) throws Exception {
        if (viagemDAO.navioTemViagemAtiva(id)) {
            throw new IllegalStateException("Não é possível apagar um navio com viagens planeadas ou em curso.");
        }
        navioDAO.apagar(id);
    }

    public Navio buscarNavio(int id) throws Exception {
        return navioDAO.buscarPorId(id);
    }

    public List<Navio> listarNavios() throws Exception {
        return navioDAO.listarTodos();
    }

    public List<Navio> listarNaviosAtivos() throws Exception {
        return navioDAO.listarPorEstado(EstadoNavio.ATIVO);
    }

    private void validarNavio(Navio navio) {
        if (navio.getNome() == null || navio.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do navio é obrigatório.");
        }
        if (navio.getCodigoIMO() == null || navio.getCodigoIMO().isBlank()) {
            throw new IllegalArgumentException("O código IMO é obrigatório.");
        }
        if (navio.getTipo() == null) {
            throw new IllegalArgumentException("O tipo de navio é obrigatório.");
        }
        if (navio.getCapacidadeMaxima() <= 0) {
            throw new IllegalArgumentException("A capacidade máxima deve ser maior que zero.");
        }
        if (navio.getNumTanques() <= 0) {
            throw new IllegalArgumentException("O número de tanques deve ser maior que zero.");
        }
        if (navio.getEstado() == null) {
            throw new IllegalArgumentException("O estado do navio é obrigatório.");
        }
    }
}
