package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.EstadoViagem;
import com.dias.navios.model.Viagem;

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
        db.execute("DELETE FROM viagens WHERE id=?", id);
    }

    public Viagem buscarPorId(int id) throws Exception {
        List<Viagem> resultado = db.select("SELECT * FROM viagens WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Viagem> listarTodos() throws Exception {
        return db.select("SELECT * FROM viagens", mapper);
    }

    /** Regra de negocio: um navio so pode ter uma viagem planeada ou em curso de cada vez. */
    public boolean navioTemViagemAtiva(int navioId) throws Exception {
        List<Integer> total = db.select(
                "SELECT COUNT(*) AS total FROM viagens WHERE navio_id=? AND estado IN ('PLANEADA','EM_CURSO')",
                rs -> rs.getInt("total"),
                navioId);
        return !total.isEmpty() && total.get(0) > 0;
    }
}
