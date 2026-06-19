package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.*;

import java.sql.Date;
import java.util.List;

public class ViagemDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    private final RowMapper<Viagem> mapper = rs -> {
        Date partida = rs.getDate("dataPartida");
        Date chegada = rs.getDate("dataChegadaPrevista");
        return new Viagem(
                rs.getInt("id"),
                rs.getInt("portoOrigemId"),
                rs.getInt("portoDestinoId"),
                partida == null ? null : partida.toLocalDate(),
                chegada == null ? null : chegada.toLocalDate(),
                rs.getInt("navioId"),
                EstadoViagem.valueOf(rs.getString("estado"))
        );
    };

    // ─── CRUD principal ───────────────────────────────────────────────────────

    public void inserir(Viagem viagem) throws Exception {
        String sql = "INSERT INTO Viagem (navioId, portoOrigemId, portoDestinoId, dataPartida, " +
                "dataChegadaPrevista, estado) VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                viagem.getNavioId(),
                viagem.getPortoOrigemId(),
                viagem.getPortoDestinoId(),
                viagem.getDataPartida() == null ? null : Date.valueOf(viagem.getDataPartida()),
                viagem.getDataChegadaPrevista() == null ? null : Date.valueOf(viagem.getDataChegadaPrevista()),
                viagem.getEstado() == null ? "PLANEADA" : viagem.getEstado().name());
        if (id > 0) viagem.setId(id);
    }

    public void atualizar(Viagem viagem) throws Exception {
        String sql = "UPDATE Viagem SET navioId=?, portoOrigemId=?, portoDestinoId=?, dataPartida=?, " +
                "dataChegadaPrevista=?, estado=? WHERE id=?";
        db.execute(sql,
                viagem.getNavioId(),
                viagem.getPortoOrigemId(),
                viagem.getPortoDestinoId(),
                viagem.getDataPartida() == null ? null : Date.valueOf(viagem.getDataPartida()),
                viagem.getDataChegadaPrevista() == null ? null : Date.valueOf(viagem.getDataChegadaPrevista()),
                viagem.getEstado() == null ? "PLANEADA" : viagem.getEstado().name(),
                viagem.getId());
    }

    public void atualizarEstado(int id, EstadoViagem estado) throws Exception {
        db.execute("UPDATE Viagem SET estado=? WHERE id=?", estado.name(), id);
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM ViagemCarga WHERE viagemId=?", id);
        db.execute("DELETE FROM ViagemTripulante WHERE viagemId=?", id);
        db.execute("DELETE FROM Viagem WHERE id=?", id);
    }

    public Viagem buscarPorId(int id) throws Exception {
        List<Viagem> resultado = db.select("SELECT * FROM Viagem WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Viagem> listarTodos() throws Exception {
        return db.select("SELECT * FROM Viagem ORDER BY id DESC", mapper);
    }

    // ─── Regras de negócio sobre o navio ─────────────────────────────────────

    public boolean navioTemViagemAtiva(int navioId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM Viagem WHERE navioId=? AND estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                navioId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    // ─── Associações viagem ↔ carga ───────────────────────────────────────────

    public void adicionarCarga(int viagemId, int cargaId) throws Exception {
        db.execute("INSERT INTO ViagemCarga (viagemId, cargaId) VALUES (?, ?)", viagemId, cargaId);
    }

    public void removerCarga(int viagemId, int cargaId) throws Exception {
        db.execute("DELETE FROM ViagemCarga WHERE viagemId=? AND cargaId=?", viagemId, cargaId);
    }

    public List<Carga> listarCargasDaViagem(int viagemId) throws Exception {
        String sql = CargaDAO.SELECT_BASE +
                " INNER JOIN ViagemCarga vc ON c.id = vc.cargaId WHERE vc.viagemId=?";
        return db.select(sql, CargaDAO.MAPPER, viagemId);
    }

    public boolean cargaJaAssociada(int viagemId, int cargaId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemCarga WHERE viagemId=? AND cargaId=?",
                rs -> rs.getInt("total"),
                viagemId, cargaId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    public int contarCargasDaViagem(int viagemId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemCarga WHERE viagemId=?",
                rs -> rs.getInt("total"),
                viagemId);
        return total.isEmpty() ? 0 : total.get(0);
    }

    public boolean cargaEstaEmViagemAtiva(int cargaId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemCarga vc " +
                "INNER JOIN Viagem v ON v.id = vc.viagemId " +
                "WHERE vc.cargaId=? AND v.estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                cargaId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    // ─── Associações viagem ↔ tripulante ──────────────────────────────────────

    public void adicionarTripulante(int viagemId, int tripulanteId, String funcaoNaViagem) throws Exception {
        db.execute("INSERT INTO ViagemTripulante (viagemId, tripulanteId, funcaoNaViagem) VALUES (?, ?, ?)",
                viagemId, tripulanteId, funcaoNaViagem);
    }

    public void removerTripulante(int viagemId, int tripulanteId) throws Exception {
        db.execute("DELETE FROM ViagemTripulante WHERE viagemId=? AND tripulanteId=?",
                viagemId, tripulanteId);
    }

    public List<Tripulante> listarTripulantesDaViagem(int viagemId) throws Exception {
        String sql = "SELECT t.* FROM Tripulante t " +
                "INNER JOIN ViagemTripulante vt ON t.id = vt.tripulanteId " +
                "WHERE vt.viagemId=?";
        return db.select(sql, TripulanteDAO.MAPPER, viagemId);
    }

    public boolean tripulanteJaAssociado(int viagemId, int tripulanteId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemTripulante WHERE viagemId=? AND tripulanteId=?",
                rs -> rs.getInt("total"),
                viagemId, tripulanteId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    public boolean tripulanteTemViagemAtiva(int tripulanteId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemTripulante vt " +
                "INNER JOIN Viagem v ON v.id = vt.viagemId " +
                "WHERE vt.tripulanteId=? AND v.estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                tripulanteId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    public List<Viagem> listarViagensDeTripulante(int tripulanteId) throws Exception {
        String sql = "SELECT v.* FROM Viagem v " +
                "INNER JOIN ViagemTripulante vt ON v.id = vt.viagemId " +
                "WHERE vt.tripulanteId=? ORDER BY v.dataPartida DESC";
        return db.select(sql, mapper, tripulanteId);
    }

    public void libertarTripulantes(int viagemId) throws Exception {
        db.execute(
                "UPDATE Tripulante SET estadoDisponibilidade='DISPONIVEL' " +
                "WHERE id IN (SELECT tripulanteId FROM ViagemTripulante WHERE viagemId=?)",
                viagemId);
    }

    public boolean viagemTemCapitao(int viagemId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM ViagemTripulante WHERE viagemId=? AND funcaoNaViagem='CAPITAO'",
                rs -> rs.getInt("total"),
                viagemId);
        return !total.isEmpty() && total.get(0) > 0;
    }
}
