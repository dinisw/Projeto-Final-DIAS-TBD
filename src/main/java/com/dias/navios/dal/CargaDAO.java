package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;

import java.util.List;

public class CargaDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    static final String SELECT_BASE =
            "SELECT c.id, c.designacao, tc.nome AS tipoCarga, c.volume, c.peso, " +
            "tc.inflamavel, tc.corrosiva, tc.toxica, c.portoCargoId, c.portoDescargoId " +
            "FROM Carga c JOIN TipoCarga tc ON tc.id = c.tipoCargaId";

    static final RowMapper<Carga> MAPPER = rs -> new Carga(
            rs.getInt("id"),
            rs.getString("designacao"),
            TipoCarga.valueOf(rs.getString("tipoCarga")),
            rs.getDouble("volume"),
            rs.getDouble("peso"),
            rs.getBoolean("inflamavel"),
            rs.getBoolean("corrosiva"),
            rs.getBoolean("toxica"),
            rs.getInt("portoCargoId"),
            rs.getInt("portoDescargoId")
    );

    private int buscarIdTipoCarga(TipoCarga tipo) throws Exception {
        List<Integer> result = db.select(
                "SELECT id FROM TipoCarga WHERE nome=?",
                rs -> rs.getInt("id"),
                tipo.name());
        if (result.isEmpty()) throw new IllegalArgumentException("TipoCarga não encontrado: " + tipo);
        return result.get(0);
    }

    public void inserir(Carga carga) throws Exception {
        int tipoId = buscarIdTipoCarga(carga.getTipo());
        String sql = "INSERT INTO Carga (designacao, tipoCargaId, volume, peso, portoCargoId, portoDescargoId) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                carga.getDesignacao(),
                tipoId,
                carga.getVolume(),
                carga.getPeso(),
                carga.getPortoCarregamentoId(),
                carga.getPortoDescargaId());
        if (id > 0) carga.setId(id);
    }

    public void atualizar(Carga carga) throws Exception {
        int tipoId = buscarIdTipoCarga(carga.getTipo());
        String sql = "UPDATE Carga SET designacao=?, tipoCargaId=?, volume=?, peso=?, " +
                "portoCargoId=?, portoDescargoId=? WHERE id=?";
        db.execute(sql,
                carga.getDesignacao(),
                tipoId,
                carga.getVolume(),
                carga.getPeso(),
                carga.getPortoCarregamentoId(),
                carga.getPortoDescargaId(),
                carga.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM Carga WHERE id=?", id);
    }

    public Carga buscarPorId(int id) throws Exception {
        List<Carga> resultado = db.select(SELECT_BASE + " WHERE c.id=?", MAPPER, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Carga> listarTodos() throws Exception {
        return db.select(SELECT_BASE, MAPPER);
    }
}
