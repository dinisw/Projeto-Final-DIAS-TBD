package com.dias.navios.bll;

import com.dias.navios.dal.PortoDAO;
import com.dias.navios.model.Porto;

import java.util.List;

public class PortoService {

    private PortoDAO portoDAO = new PortoDAO();

    public void registarPorto(Porto porto) throws Exception {
        validarPorto(porto);
        portoDAO.inserir(porto);
    }

    public void editarPorto(Porto porto) throws Exception {
        validarPorto(porto);
        portoDAO.atualizar(porto);
    }

    public void apagarPorto(int id) throws Exception {
        portoDAO.apagar(id);
    }

    public List<Porto> listarPortos() throws Exception {
        return portoDAO.listarTodos();
    }

    public Porto buscarPorto(int id) throws Exception {
        return portoDAO.buscarPorId(id);
    }

    private void validarPorto(Porto porto) {
        if (porto.getNome() == null || porto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do porto é obrigatório.");
        }
        if (porto.getPais() == null || porto.getPais().isBlank()) {
            throw new IllegalArgumentException("O país do porto é obrigatório.");
        }
        if (porto.getCodigo() == null || porto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("O código do porto é obrigatório.");
        }
    }
}
