package com.dias.navios.dal;

import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripulanteDAO {

    public void inserir(Tripulante t) throws Exception {
        String sql = "INSERT INTO tripulantes (nome, numero_certificado, funcao, disponivel, nacionalidade) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, t.getNome());
        ps.setString(2, t.getNumeroCertificado());
        ps.setString(3, t.getFuncao().name());
        ps.setBoolean(4, t.isDisponivel());
        ps.setString(5, t.getNacionalidade());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            t.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void atualizar(Tripulante t) throws Exception {
        String sql = "UPDATE tripulantes SET nome=?, numero_certificado=?, funcao=?, disponivel=?, nacionalidade=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, t.getNome());
        ps.setString(2, t.getNumeroCertificado());
        ps.setString(3, t.getFuncao().name());
        ps.setBoolean(4, t.isDisponivel());
        ps.setString(5, t.getNacionalidade());
        ps.setInt(6, t.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        String sql = "DELETE FROM tripulantes WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Tripulante buscarPorId(int id) throws Exception {
        String sql = "SELECT * FROM tripulantes WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Tripulante t = null;
        if (rs.next()) {
            t = mapearResultSet(rs);
        }
        rs.close();
        ps.close();
        return t;
    }

    public List<Tripulante> listarTodos() throws Exception {
        List<Tripulante> lista = new ArrayList<>();
        String sql = "SELECT * FROM tripulantes ORDER BY nome";
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

    Tripulante mapearResultSet(ResultSet rs) throws SQLException {
        Tripulante t = new Tripulante();
        t.setId(rs.getInt("id"));
        t.setNome(rs.getString("nome"));
        t.setNumeroCertificado(rs.getString("numero_certificado"));
        t.setFuncao(FuncaoTripulante.valueOf(rs.getString("funcao")));
        t.setDisponivel(rs.getBoolean("disponivel"));
        t.setNacionalidade(rs.getString("nacionalidade"));
        return t;
    }
}
