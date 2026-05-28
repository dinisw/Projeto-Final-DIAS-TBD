package com.dias.navios.dal;

import com.dias.navios.model.Porto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortoDAO {

    public void inserir(Porto porto) throws Exception {
        // TODO: implementar INSERT na tabela portos
        String sql = "INSERT INTO portos (nome, pais, codigo) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // TODO: preencher os parametros
        ps.close();
    }

    public Porto buscarPorId(int id) throws Exception {
        // TODO: implementar SELECT por id
        String sql = "SELECT * FROM portos WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Porto porto = null;
        if (rs.next()) {
            porto = mapearResultSet(rs);
        }
        rs.close();
        ps.close();
        return porto;
    }

    public List<Porto> listarTodos() throws Exception {
        // TODO: implementar SELECT de todos os portos
        List<Porto> lista = new ArrayList<>();
        String sql = "SELECT * FROM portos";
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

    private Porto mapearResultSet(ResultSet rs) throws SQLException {
        Porto porto = new Porto();
        porto.setId(rs.getInt("id"));
        porto.setNome(rs.getString("nome"));
        porto.setPais(rs.getString("pais"));
        porto.setCodigo(rs.getString("codigo"));
        return porto;
    }
}
