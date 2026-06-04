package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.model.Carga;
import com.dias.navios.model.TipoCarga;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CargaDAO {

    public void inserir(Carga carga) throws Exception {
        // TODO: implementar INSERT na tabela cargas
        String sql = "INSERT INTO cargas (designacao, tipo, volume, peso, inflamavel, corrosiva, toxica, porto_carregamento_id, porto_descarga_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // TODO: preencher os parametros
        ps.close();
    }

    public void atualizar(Carga carga) throws Exception {
        // TODO: implementar UPDATE na tabela cargas
    }

    public void apagar(int id) throws Exception {
        // TODO: implementar DELETE na tabela cargas
        String sql = "DELETE FROM cargas WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Carga buscarPorId(int id) throws Exception {
        // TODO: implementar SELECT por id
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
        // TODO: implementar SELECT de todas as cargas
        List<Carga> lista = new ArrayList<>();
        String sql = "SELECT * FROM cargas";
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

    private Carga mapearResultSet(ResultSet rs) throws SQLException {
        // TODO: converter linha do ResultSet num objeto Carga
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
