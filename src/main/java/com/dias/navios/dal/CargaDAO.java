package com.dias.navios.dal;

import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CargaDAO {

    public void inserir(Carga carga) throws Exception {
        String sql = "INSERT INTO cargas (designacao, tipo, volume, peso, inflamavel, corrosiva, toxica, porto_carregamento_id, porto_descarga_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, carga.getDesignacao());
        ps.setString(2, carga.getTipo().name());
        ps.setDouble(3, carga.getVolume());
        ps.setDouble(4, carga.getPeso());
        ps.setBoolean(5, carga.isInflamavel());
        ps.setBoolean(6, carga.isCorrosiva());
        ps.setBoolean(7, carga.isToxica());
        if (carga.getPortoCarregamentoId() > 0) {
            ps.setInt(8, carga.getPortoCarregamentoId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
        if (carga.getPortoDescargaId() > 0) {
            ps.setInt(9, carga.getPortoDescargaId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            carga.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void atualizar(Carga carga) throws Exception {
        String sql = "UPDATE cargas SET designacao=?, tipo=?, volume=?, peso=?, inflamavel=?, corrosiva=?, toxica=?, porto_carregamento_id=?, porto_descarga_id=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, carga.getDesignacao());
        ps.setString(2, carga.getTipo().name());
        ps.setDouble(3, carga.getVolume());
        ps.setDouble(4, carga.getPeso());
        ps.setBoolean(5, carga.isInflamavel());
        ps.setBoolean(6, carga.isCorrosiva());
        ps.setBoolean(7, carga.isToxica());
        if (carga.getPortoCarregamentoId() > 0) {
            ps.setInt(8, carga.getPortoCarregamentoId());
        } else {
            ps.setNull(8, Types.INTEGER);
        }
        if (carga.getPortoDescargaId() > 0) {
            ps.setInt(9, carga.getPortoDescargaId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        ps.setInt(10, carga.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        String sql = "DELETE FROM cargas WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Carga buscarPorId(int id) throws Exception {
        String sql = "SELECT * FROM cargas WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Carga carga = null;
        if (rs.next()) {
            carga = mapearResultSet(rs);
        }
        rs.close();
        ps.close();
        return carga;
    }

    public List<Carga> listarTodos() throws Exception {
        List<Carga> lista = new ArrayList<>();
        String sql = "SELECT * FROM cargas ORDER BY designacao";
        Connection conn = DatabaseConnection.getConnection();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            lista.add(mapearResultSet(rs));
        }
        rs.close();
        st.close();
        return lista;
    }

    Carga mapearResultSet(ResultSet rs) throws SQLException {
        Carga carga = new Carga();
        carga.setId(rs.getInt("id"));
        carga.setDesignacao(rs.getString("designacao"));
        carga.setTipo(TipoCarga.valueOf(rs.getString("tipo")));
        carga.setVolume(rs.getDouble("volume"));
        carga.setPeso(rs.getDouble("peso"));
        carga.setInflamavel(rs.getBoolean("inflamavel"));
        carga.setCorrosiva(rs.getBoolean("corrosiva"));
        carga.setToxica(rs.getBoolean("toxica"));
        carga.setPortoCarregamentoId(rs.getInt("porto_carregamento_id"));
        carga.setPortoDescargaId(rs.getInt("porto_descarga_id"));
        return carga;
    }
}
