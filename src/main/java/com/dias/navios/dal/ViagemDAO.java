package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.*;

import java.sql.Date;
import java.util.List;

public class ViagemDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    private final RowMapper<Viagem> mapper = rs -> {
        Date partida = rs.getDate("data_partida");
        Date chegada = rs.getDate("data_chegada_prevista");
        return new Viagem(
                rs.getInt("id"),
                rs.getInt("porto_origem_id"),
                rs.getInt("porto_destino_id"),
                partida == null ? null : partida.toLocalDate(),
                chegada == null ? null : chegada.toLocalDate(),
                rs.getInt("navio_id"),
                EstadoViagem.valueOf(rs.getString("estado"))
        );
    };

    private final RowMapper<Carga> cargaMapper = rs -> new Carga(
            rs.getInt("id"),
            rs.getString("designacao"),
            TipoCarga.valueOf(rs.getString("tipo")),
            rs.getDouble("volume"),
            rs.getDouble("peso"),
            rs.getBoolean("inflamavel"),
            rs.getBoolean("corrosiva"),
            rs.getBoolean("toxica"),
            rs.getInt("porto_carregamento_id"),
            rs.getInt("porto_descarga_id")
    );

    private final RowMapper<Tripulante> tripulanteMapper = rs -> new Tripulante(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("numero_certificado"),
            FuncaoTripulante.valueOf(rs.getString("funcao")),
            rs.getBoolean("disponivel"),
            rs.getString("nacionalidade")
    );

    // ─── CRUD principal ───────────────────────────────────────────────────────

    public void inserir(Viagem viagem) throws Exception {
        String sql = "INSERT INTO viagens (porto_origem_id, porto_destino_id, data_partida, " +
                "data_chegada_prevista, navio_id, estado) VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                viagem.getPortoOrigemId(),
                viagem.getPortoDestinoId(),
                viagem.getDataPartida() == null ? null : Date.valueOf(viagem.getDataPartida()),
                viagem.getDataChegadaPrevista() == null ? null : Date.valueOf(viagem.getDataChegadaPrevista()),
                viagem.getNavioId(),
                viagem.getEstado() == null ? null : viagem.getEstado().name());
        if (id > 0) viagem.setId(id);
    }

    public void atualizar(Viagem viagem) throws Exception {
        String sql = "UPDATE viagens SET porto_origem_id=?, porto_destino_id=?, data_partida=?, " +
                "data_chegada_prevista=?, navio_id=?, estado=? WHERE id=?";
        db.execute(sql,
                viagem.getPortoOrigemId(),
                viagem.getPortoDestinoId(),
                viagem.getDataPartida() == null ? null : Date.valueOf(viagem.getDataPartida()),
                viagem.getDataChegadaPrevista() == null ? null : Date.valueOf(viagem.getDataChegadaPrevista()),
                viagem.getNavioId(),
                viagem.getEstado() == null ? null : viagem.getEstado().name(),
                viagem.getId());
    }

    public void atualizarEstado(int id, EstadoViagem estado) throws Exception {
        db.execute("UPDATE viagens SET estado=? WHERE id=?", estado.name(), id);
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM viagem_carga WHERE viagem_id=?", id);
        db.execute("DELETE FROM viagem_tripulante WHERE viagem_id=?", id);
        db.execute("DELETE FROM viagens WHERE id=?", id);
    }

    public Viagem buscarPorId(int id) throws Exception {
        List<Viagem> resultado = db.select("SELECT * FROM viagens WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Viagem> listarTodos() throws Exception {
        return db.select("SELECT * FROM viagens ORDER BY id DESC", mapper);
    }

    // ─── Regras de negocio sobre o navio ─────────────────────────────────────

    public boolean navioTemViagemAtiva(int navioId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM viagens WHERE navio_id=? AND estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                navioId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    // ─── Associações viagem ↔ carga ───────────────────────────────────────────

    public void adicionarCarga(int viagemId, int cargaId) throws Exception {
        db.execute("INSERT INTO viagem_carga (viagem_id, carga_id) VALUES (?, ?)", viagemId, cargaId);
    }

    public void removerCarga(int viagemId, int cargaId) throws Exception {
        db.execute("DELETE FROM viagem_carga WHERE viagem_id=? AND carga_id=?", viagemId, cargaId);
    }

    public List<Carga> listarCargasDaViagem(int viagemId) throws Exception {
        String sql = "SELECT c.* FROM cargas c " +
                "INNER JOIN viagem_carga vc ON c.id = vc.carga_id " +
                "WHERE vc.viagem_id = ?";
        return db.select(sql, cargaMapper, viagemId);
    }

    public boolean cargaEstaEmViagemAtiva(int cargaId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM viagem_carga vc " +
                "INNER JOIN viagens v ON v.id = vc.viagem_id " +
                "WHERE vc.carga_id=? AND v.estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                cargaId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    // ─── Associações viagem ↔ tripulante ──────────────────────────────────────

    public void adicionarTripulante(int viagemId, int tripulanteId) throws Exception {
        db.execute("INSERT INTO viagem_tripulante (viagem_id, tripulante_id) VALUES (?, ?)",
                viagemId, tripulanteId);
    }

    public void removerTripulante(int viagemId, int tripulanteId) throws Exception {
        db.execute("DELETE FROM viagem_tripulante WHERE viagem_id=? AND tripulante_id=?",
                viagemId, tripulanteId);
    }

    public List<Tripulante> listarTripulantesDaViagem(int viagemId) throws Exception {
        String sql = "SELECT t.* FROM tripulantes t " +
                "INNER JOIN viagem_tripulante vt ON t.id = vt.tripulante_id " +
                "WHERE vt.viagem_id = ?";
        return db.select(sql, tripulanteMapper, viagemId);
    }

    public boolean tripulanteTemViagemAtiva(int tripulanteId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM viagem_tripulante vt " +
                "INNER JOIN viagens v ON v.id = vt.viagem_id " +
                "WHERE vt.tripulante_id=? AND v.estado IN ('" +
                EstadoViagem.PLANEADA.name() + "','" + EstadoViagem.EM_CURSO.name() + "')",
                rs -> rs.getInt("total"),
                tripulanteId);
        return !total.isEmpty() && total.get(0) > 0;
    }

    public List<Viagem> listarViagensDeTripulante(int tripulanteId) throws Exception {
        String sql = "SELECT v.* FROM viagens v " +
                "INNER JOIN viagem_tripulante vt ON v.id = vt.viagem_id " +
                "WHERE vt.tripulante_id = ? ORDER BY v.data_partida DESC";
        return db.select(sql, mapper, tripulanteId);
    }
}
