package com.dias.navios.bll;

import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.EstadoViagem;
import com.dias.navios.model.Navio;
import com.dias.navios.model.Viagem;

import java.util.List;

public class ViagemService {

    private ViagemDAO viagemDAO = new ViagemDAO();
    private NavioDAO navioDAO = new NavioDAO();

    public void criarViagem(Viagem viagem) throws Exception {
        Navio navio = navioDAO.buscarPorId(viagem.getNavioId());

        // Regra: o navio tem de existir (evita NullPointerException)
        if (navio == null) {
            throw new IllegalArgumentException("Selecione um navio valido para a viagem.");
        }

        // Regra: navio deve estar ATIVO
        if (navio.getEstado() != EstadoNavio.ATIVO) {
            throw new IllegalStateException("O navio nao esta ativo e nao pode iniciar uma viagem.");
        }

        // Regra: navio nao pode ter outra viagem em curso
        if (viagemDAO.navioTemViagemAtiva(viagem.getNavioId())) {
            throw new IllegalStateException("O navio ja tem uma viagem em curso.");
        }

        // TODO: validar compatibilidade entre tipo de navio e tipo de cargas

        viagem.setEstado(EstadoViagem.PLANEADA);
        viagemDAO.inserir(viagem);
    }

    public void atualizarViagem(Viagem viagem) throws Exception {
        Viagem atual = viagemDAO.buscarPorId(viagem.getId());
        if (atual == null) {
            throw new IllegalArgumentException("Viagem não encontrada.");
        }
        if (atual.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível editar viagens no estado PLANEADA.");
        }
        if (viagem.getPortoOrigemId() == viagem.getPortoDestinoId()) {
            throw new IllegalArgumentException("A origem e o destino não podem ser o mesmo porto.");
        }
        if (viagem.getDataChegadaPrevista() != null && viagem.getDataPartida() != null
                && viagem.getDataChegadaPrevista().isBefore(viagem.getDataPartida())) {
            throw new IllegalArgumentException("A data de chegada não pode ser anterior à de partida.");
        }
        viagem.setEstado(atual.getEstado());
        viagemDAO.atualizar(viagem);
    }

    public void avancarEstado(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        EstadoViagem estadoAtual = viagem.getEstado();
        EstadoViagem novoEstado;

        // Progressao logica dos estados
        switch (estadoAtual) {
            case PLANEADA:
                novoEstado = EstadoViagem.EM_CURSO;
                break;
            case EM_CURSO:
                novoEstado = EstadoViagem.CONCLUIDA;
                break;
            default:
                throw new IllegalStateException("Nao e possivel avancar o estado desta viagem.");
        }

        viagemDAO.atualizarEstado(viagemId, novoEstado);
    }

    public void cancelarViagem(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() == EstadoViagem.CONCLUIDA) {
            throw new IllegalStateException("Nao e possivel cancelar uma viagem ja concluida.");
        }
        viagemDAO.atualizarEstado(viagemId, EstadoViagem.CANCELADA);
    }

    public List<Viagem> listarViagens() throws Exception {
        return viagemDAO.listarTodos();
    }

    public Viagem buscarViagem(int id) throws Exception {
        return viagemDAO.buscarPorId(id);
    }
}
