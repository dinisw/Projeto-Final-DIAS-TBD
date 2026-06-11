package com.dias.navios.bll;

import com.dias.navios.dal.PortoDAO;
import com.dias.navios.model.Porto;

import java.util.List;

/**
 * Camada de logica de negocio (BLL) para os Portos.
 * Faz as validacoes antes de delegar a persistencia no PortoDAO.
 */
public class PortoService {

    private final PortoDAO portoDAO = new PortoDAO();

    public void registar(Porto porto) throws Exception {
        validar(porto);
        portoDAO.inserir(porto);
    }

    public void editar(Porto porto) throws Exception {
        validar(porto);
        portoDAO.atualizar(porto);
    }

    public void apagar(int id) throws Exception {
        portoDAO.apagar(id);
    }

    public List<Porto> listar() throws Exception {
        return portoDAO.listarTodos();
    }

    private void validar(Porto porto) {
        if (porto.getNome() == null || porto.getNome().isBlank()) {
            throw new IllegalArgumentException("O nome do porto e obrigatorio.");
        }
        if (porto.getCodigo() == null || porto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("O codigo do porto e obrigatorio.");
        }
    }
}
