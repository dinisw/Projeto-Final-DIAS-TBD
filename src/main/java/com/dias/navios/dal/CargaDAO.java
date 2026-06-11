package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;

import java.util.List;

public class CargaDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    private final RowMapper<Carga> mapper = rs -> new Carga(
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

    public void inserir(Carga carga) throws Exception {
        String sql = "INSERT INTO cargas (designacao, tipo, volume, peso, inflamavel, corrosiva, toxica, " +
                "porto_carregamento_id, porto_descarga_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                carga.getDesignacao(),
                carga.getTipo() == null ? null : carga.getTipo().name(),
                carga.getVolume(),
                carga.getPeso(),
                carga.isInflamavel(),
                carga.isCorrosiva(),
                carga.isToxica(),
                carga.getPortoCarregamentoId(),
                carga.getPortoDescargaId());
        if (id > 0) carga.setId(id);
    }

    public void atualizar(Carga carga) throws Exception {
        String sql = "UPDATE cargas SET designacao=?, tipo=?, volume=?, peso=?, inflamavel=?, corrosiva=?, " +
                "toxica=?, porto_carregamento_id=?, porto_descarga_id=? WHERE id=?";
        db.execute(sql,
                carga.getDesignacao(),
                carga.getTipo() == null ? null : carga.getTipo().name(),
                carga.getVolume(),
                carga.getPeso(),
                carga.isInflamavel(),
                carga.isCorrosiva(),
                carga.isToxica(),
                carga.getPortoCarregamentoId(),
                carga.getPortoDescargaId(),
                carga.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM cargas WHERE id=?", id);
    }

    public Carga buscarPorId(int id) throws Exception {
        List<Carga> resultado = db.select("SELECT * FROM cargas WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Carga> listarTodos() throws Exception {
        return db.select("SELECT * FROM cargas", mapper);
    }
}
