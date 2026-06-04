package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripulanteDAO {

    public void inserir(Tripulante t) throws Exception {
        // TODO: implementar INSERT na tabela tripulantes
        String sql = "INSERT INTO tripulantes (nome, numero_certificado, funcao, disponivel, nacionalidade) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // TODO: preencher os parametros
        ps.close();
    }

    public void atualizar(Tripulante t) throws Exception {
        // TODO: implementar UPDATE na tabela tripulantes
    }

    public void apagar(int id) throws Exception {
        // TODO: implementar DELETE na tabela tripulantes
        String sql = "DELETE FROM tripulantes WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Tripulante buscarPorId(int id) throws Exception {
        // TODO: implementar SELECT por id
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
        // TODO: implementar SELECT de todos os tripulantes
        List<Tripulante> lista = new ArrayList<>();
        String sql = "SELECT * FROM tripulantes";
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

    private Tripulante mapearResultSet(ResultSet rs) throws SQLException {
        // TODO: converter linha do ResultSet num objeto Tripulante
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
