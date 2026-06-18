package com.dias.navios.dal;

import com.dias.navios.model.Porto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PortoDAO {

    public void inserir(Porto porto) throws Exception {
        String sql = "INSERT INTO portos (nome, pais, codigo) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, porto.getNome());
        ps.setString(2, porto.getPais());
        ps.setString(3, porto.getCodigo());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            porto.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void atualizar(Porto porto) throws Exception {
        String sql = "UPDATE portos SET nome=?, pais=?, codigo=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, porto.getNome());
        ps.setString(2, porto.getPais());
        ps.setString(3, porto.getCodigo());
        ps.setInt(4, porto.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        String sql = "DELETE FROM portos WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Porto buscarPorId(int id) throws Exception {
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
        List<Porto> lista = new ArrayList<>();
        String sql = "SELECT * FROM portos ORDER BY nome";
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

    Porto mapearResultSet(ResultSet rs) throws SQLException {
        Porto porto = new Porto();
        porto.setId(rs.getInt("id"));
        porto.setNome(rs.getString("nome"));
        porto.setPais(rs.getString("pais"));
        porto.setCodigo(rs.getString("codigo"));
        return porto;
    }
}
