package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.*;

import java.util.List;

public class ViagemService {

    private ViagemDAO viagemDAO = new ViagemDAO();
    private NavioDAO  navioDAO  = new NavioDAO();
    private CargaDAO  cargaDAO  = new CargaDAO();

    // ─── Criar viagem ─────────────────────────────────────────────────────────

    public void criarViagem(Viagem viagem) throws Exception {
        validarDadosBasicos(viagem);

        Navio navio = navioDAO.buscarPorId(viagem.getNavioId());
        if (navio == null) {
            throw new IllegalArgumentException("Navio não encontrado.");
        }
        if (navio.getEstado() != EstadoNavio.ATIVO) {
            throw new IllegalStateException("O navio não está ativo e não pode iniciar uma viagem.");
        }
        if (viagemDAO.navioTemViagemAtiva(viagem.getNavioId())) {
            throw new IllegalStateException("O navio já tem uma viagem em curso ou planeada.");
        }

        viagem.setEstado(EstadoViagem.PLANEADA);
        viagemDAO.inserir(viagem);
    }

    // ─── Editar viagem (apenas PLANEADA) ──────────────────────────────────────

    public void editarViagem(Viagem viagem) throws Exception {
        Viagem atual = viagemDAO.buscarPorId(viagem.getId());
        if (atual == null) {
            throw new IllegalArgumentException("Viagem não encontrada.");
        }
        if (atual.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível editar viagens no estado PLANEADA.");
        }
        viagem.setEstado(EstadoViagem.PLANEADA);
        viagemDAO.atualizar(viagem);
    }

    // ─── Máquina de estados ───────────────────────────────────────────────────

    public void avancarEstado(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        EstadoViagem novoEstado;
        switch (viagem.getEstado()) {
            case PLANEADA:  novoEstado = EstadoViagem.EM_CURSO;  break;
            case EM_CURSO:  novoEstado = EstadoViagem.CONCLUIDA; break;
            default:
                throw new IllegalStateException("Não é possível avançar o estado desta viagem.");
        }
        viagemDAO.atualizarEstado(viagemId, novoEstado);
    }

    public void cancelarViagem(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() == EstadoViagem.CONCLUIDA ||
            viagem.getEstado() == EstadoViagem.CANCELADA) {
            throw new IllegalStateException("Não é possível cancelar uma viagem " +
                    viagem.getEstado().name().toLowerCase() + ".");
        }
        viagemDAO.atualizarEstado(viagemId, EstadoViagem.CANCELADA);
    }

    // ─── Associação de cargas ─────────────────────────────────────────────────

    public void associarCarga(int viagemId, int cargaId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível adicionar cargas a viagens PLANEADAS.");
        }

        Navio navio = navioDAO.buscarPorId(viagem.getNavioId());
        Carga carga = cargaDAO.buscarPorId(cargaId);

        if (viagem.getCargasIds().contains(cargaId)) {
            throw new IllegalStateException("Esta carga já está associada à viagem.");
        }

        if (!navio.getTipo().aceitaCarga(carga.getTipo())) {
            throw new IllegalStateException(
                    "Carga incompatível: navio do tipo " + navio.getTipo() +
                    " não aceita carga do tipo " + carga.getTipo() + ".");
        }

        List<Carga> cargasExistentes = viagemDAO.listarCargasDaViagem(viagemId);
        double pesoTotal = cargasExistentes.stream().mapToDouble(Carga::getPeso).sum() + carga.getPeso();
        if (pesoTotal > navio.getCapacidadeMaxima()) {
            throw new IllegalStateException(String.format(
                    "Capacidade excedida: %.0f t carregadas + %.0f t nova > %.0f t máximo.",
                    pesoTotal - carga.getPeso(), carga.getPeso(), navio.getCapacidadeMaxima()));
        }

        viagemDAO.adicionarCarga(viagemId, cargaId);
    }

    public void removerCarga(int viagemId, int cargaId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível remover cargas de viagens PLANEADAS.");
        }
        viagemDAO.removerCarga(viagemId, cargaId);
    }

    // ─── Associação de tripulantes ────────────────────────────────────────────

    public void associarTripulante(int viagemId, int tripulanteId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível adicionar tripulantes a viagens PLANEADAS.");
        }
        if (viagem.getTripulantesIds().contains(tripulanteId)) {
            throw new IllegalStateException("Este tripulante já está associado à viagem.");
        }
        viagemDAO.adicionarTripulante(viagemId, tripulanteId);
    }

    public void removerTripulante(int viagemId, int tripulanteId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível remover tripulantes de viagens PLANEADAS.");
        }
        viagemDAO.removerTripulante(viagemId, tripulanteId);
    }

    // ─── Consultas ────────────────────────────────────────────────────────────

    public List<Viagem> listarViagens() throws Exception {
        return viagemDAO.listarTodos();
    }

    public Viagem buscarViagem(int id) throws Exception {
        return viagemDAO.buscarPorId(id);
    }

    public List<Carga> listarCargasDaViagem(int viagemId) throws Exception {
        return viagemDAO.listarCargasDaViagem(viagemId);
    }

    public List<Tripulante> listarTripulantesDaViagem(int viagemId) throws Exception {
        return viagemDAO.listarTripulantesDaViagem(viagemId);
    }

    // ─── Validação básica dos campos (antes de qualquer acesso à BD) ──────────

    private void validarDadosBasicos(Viagem viagem) {
        if (viagem.getNavioId() <= 0) {
            throw new IllegalArgumentException("Selecione um navio válido.");
        }
        if (viagem.getPortoOrigemId() <= 0) {
            throw new IllegalArgumentException("Selecione o porto de origem.");
        }
        if (viagem.getPortoDestinoId() <= 0) {
            throw new IllegalArgumentException("Selecione o porto de destino.");
        }
        if (viagem.getPortoOrigemId() == viagem.getPortoDestinoId()) {
            throw new IllegalArgumentException("A origem e o destino não podem ser o mesmo porto.");
        }
        if (viagem.getDataPartida() == null) {
            throw new IllegalArgumentException("A data de partida é obrigatória.");
        }
        if (viagem.getDataChegadaPrevista() != null &&
            viagem.getDataChegadaPrevista().isBefore(viagem.getDataPartida())) {
            throw new IllegalArgumentException("A data de chegada não pode ser anterior à de partida.");
        }
    }
}
