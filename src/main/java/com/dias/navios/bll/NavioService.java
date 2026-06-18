package com.dias.navios.bll;

import com.dias.navios.dal.NavioDAO;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;

import java.util.List;

public class NavioService {

    private NavioDAO navioDAO = new NavioDAO();

    public void registarNavio(Navio navio) throws Exception {
        // TODO: validar campos obrigatorios antes de inserir
        if (navio.getNome() == null || navio.getNome().isEmpty()) {
            throw new IllegalArgumentException("O nome do navio é obrigatorio.");
        }
        if (navio.getCodigoIMO() == null || navio.getCodigoIMO().isEmpty()) {
            throw new IllegalArgumentException("O codigo IMO e obrigatorio.");
        }
        navioDAO.inserir(navio);
    }

    public void editarNavio(Navio navio) throws Exception {
        // TODO: validar antes de atualizar
        navioDAO.atualizar(navio);
    }

    public void apagarNavio(int id) throws Exception {
        navioDAO.apagar(id);
    }

    public Navio buscarNavio(int id) throws Exception {
        return navioDAO.buscarPorId(id);
    }

    public List<Navio> listarNavios() throws Exception {
        return navioDAO.listarTodos();
    }
}
