package com.dias.navios.bll;

import com.dias.navios.dal.CargaDAO;
import com.dias.navios.dal.NavioDAO;
import com.dias.navios.dal.ViagemDAO;
import com.dias.navios.model.*;

import java.util.List;

public class ViagemService {

    private ViagemDAO viagemDAO = new ViagemDAO();
    private NavioDAO navioDAO = new NavioDAO();
    private CargaDAO cargaDAO = new CargaDAO();

    public void criarViagem(Viagem viagem) throws Exception {
        validarDadosBasicos(viagem);

        Navio navio = navioDAO.buscarPorId(viagem.getNavioId());
        if (navio == null) {
            throw new IllegalArgumentException("Navio não encontrado.");
        }
        if (navio.getEstado() != EstadoNavio.ATIVO) {
            throw new IllegalStateException("O navio não está activo e não pode efectuar viagens.");
        }
        if (viagemDAO.navioTemViagemAtiva(viagem.getNavioId())) {
            throw new IllegalStateException("O navio já tem uma viagem planeada ou em curso.");
        }

        viagem.setEstado(EstadoViagem.PLANEADA);
        viagemDAO.inserir(viagem);
    }

    public void editarViagem(Viagem viagem) throws Exception {
        Viagem atual = viagemDAO.buscarPorId(viagem.getId());
        if (atual == null) {
            throw new IllegalArgumentException("Viagem não encontrada.");
        }
        if (atual.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível editar viagens no estado PLANEADA.");
        }
        validarDadosBasicos(viagem);
        viagem.setEstado(EstadoViagem.PLANEADA);
        viagemDAO.atualizar(viagem);
    }

    public void associarCarga(int viagemId, int cargaId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem == null) throw new IllegalArgumentException("Viagem não encontrada.");
        if (viagem.getEstado() == EstadoViagem.CONCLUIDA || viagem.getEstado() == EstadoViagem.CANCELADA) {
            throw new IllegalStateException("Não é possível adicionar carga a uma viagem concluída ou cancelada.");
        }

        Navio navio = navioDAO.buscarPorId(viagem.getNavioId());
        Carga carga = cargaDAO.buscarPorId(cargaId);

        // Validar compatibilidade navio/carga
        if (!navio.getTipo().aceitaCarga(carga.getTipo())) {
            throw new IllegalStateException(
                "Incompatibilidade: navio do tipo " + navio.getTipo()
                + " não aceita carga do tipo " + carga.getTipo()
                + ". Tipos aceites: " + navio.getTipo().descricaoCompativel()
            );
        }

        // Validar capacidade
        List<Carga> cargasActuais = viagemDAO.listarCargasDaViagem(viagemId);
        double pesoTotal = cargasActuais.stream().mapToDouble(Carga::getPeso).sum() + carga.getPeso();
        if (pesoTotal > navio.getCapacidadeMaxima()) {
            throw new IllegalStateException(
                "Capacidade excedida: a carga adicional de " + carga.getPeso()
                + " ton ultrapassa a capacidade máxima de " + navio.getCapacidadeMaxima()
                + " ton. Peso actual: " + (pesoTotal - carga.getPeso()) + " ton."
            );
        }

        if (viagem.getCargasIds().contains(cargaId)) {
            throw new IllegalStateException("Esta carga já está associada a esta viagem.");
        }

        viagemDAO.adicionarCarga(viagemId, cargaId);
    }

    public void removerCarga(int viagemId, int cargaId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem == null) throw new IllegalArgumentException("Viagem não encontrada.");
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível remover cargas de viagens no estado PLANEADA.");
        }
        viagemDAO.removerCarga(viagemId, cargaId);
    }

    public void associarTripulante(int viagemId, int tripulanteId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem == null) throw new IllegalArgumentException("Viagem não encontrada.");
        if (viagem.getEstado() == EstadoViagem.CONCLUIDA || viagem.getEstado() == EstadoViagem.CANCELADA) {
            throw new IllegalStateException("Não é possível adicionar tripulante a uma viagem concluída ou cancelada.");
        }
        if (viagem.getTripulantesIds().contains(tripulanteId)) {
            throw new IllegalStateException("Este tripulante já está associado a esta viagem.");
        }
        viagemDAO.adicionarTripulante(viagemId, tripulanteId);
    }

    public void removerTripulante(int viagemId, int tripulanteId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem == null) throw new IllegalArgumentException("Viagem não encontrada.");
        if (viagem.getEstado() != EstadoViagem.PLANEADA) {
            throw new IllegalStateException("Só é possível remover tripulantes de viagens no estado PLANEADA.");
        }
        viagemDAO.removerTripulante(viagemId, tripulanteId);
    }

    public void avancarEstado(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        EstadoViagem novoEstado;
        switch (viagem.getEstado()) {
            case PLANEADA:
                novoEstado = EstadoViagem.EM_CURSO;
                break;
            case EM_CURSO:
                novoEstado = EstadoViagem.CONCLUIDA;
                break;
            default:
                throw new IllegalStateException("Não é possível avançar o estado desta viagem.");
        }
        viagemDAO.atualizarEstado(viagemId, novoEstado);
    }

    public void cancelarViagem(int viagemId) throws Exception {
        Viagem viagem = viagemDAO.buscarPorId(viagemId);
        if (viagem.getEstado() == EstadoViagem.CONCLUIDA) {
            throw new IllegalStateException("Não é possível cancelar uma viagem já concluída.");
        }
        if (viagem.getEstado() == EstadoViagem.CANCELADA) {
            throw new IllegalStateException("A viagem já está cancelada.");
        }
        viagemDAO.atualizarEstado(viagemId, EstadoViagem.CANCELADA);
    }

    public List<Viagem> listarViagens() throws Exception {
        return viagemDAO.listarTodos();
    }

    public Viagem buscarViagem(int id) throws Exception {
        return viagemDAO.buscarPorId(id);
    }

    public List<Carga> listarCargasDaViagem(int viagemId) throws Exception {
        return viagemDAO.listarCargasDaViagem(viagemId);
    }

    public List<com.dias.navios.model.Tripulante> listarTripulantesDaViagem(int viagemId) throws Exception {
        return viagemDAO.listarTripulantesDaViagem(viagemId);
    }

    private void validarDadosBasicos(Viagem viagem) {
        if (viagem.getNavioId() <= 0) {
            throw new IllegalArgumentException("É necessário seleccionar um navio.");
        }
        if (viagem.getPortoOrigemId() <= 0) {
            throw new IllegalArgumentException("O porto de origem é obrigatório.");
        }
        if (viagem.getPortoDestinoId() <= 0) {
            throw new IllegalArgumentException("O porto de destino é obrigatório.");
        }
        if (viagem.getPortoOrigemId() == viagem.getPortoDestinoId()) {
            throw new IllegalArgumentException("O porto de origem não pode ser igual ao porto de destino.");
        }
        if (viagem.getDataPartida() == null) {
            throw new IllegalArgumentException("A data de partida é obrigatória.");
        }
        if (viagem.getDataChegadaPrevista() != null
                && viagem.getDataChegadaPrevista().isBefore(viagem.getDataPartida())) {
            throw new IllegalArgumentException("A data de chegada prevista não pode ser anterior à data de partida.");
        }
    }
}
