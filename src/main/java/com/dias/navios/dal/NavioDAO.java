package com.dias.navios.dal;

import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NavioDAO {

    public void inserir(Navio navio) throws Exception {
        // TODO: implementar INSERT na tabela navios
        String sql = "INSERT INTO navios (nome, codigo_imo, tipo, capacidade_maxima, num_tanques, bandeira, ano_fabrico, estado, porto_atual_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // TODO: preencher os parametros do PreparedStatement
        ps.close();
    }

    public void atualizar(Navio navio) throws Exception {
        // TODO: implementar UPDATE na tabela navios
        String sql = "UPDATE navios SET nome=?, codigo_imo=?, tipo=?, capacidade_maxima=?, num_tanques=?, bandeira=?, ano_fabrico=?, estado=?, porto_atual_id=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        // TODO: preencher os parametros do PreparedStatement
        ps.close();
    }

    public void apagar(int id) throws Exception {
        // TODO: implementar DELETE na tabela navios
        String sql = "DELETE FROM navios WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Navio buscarPorId(int id) throws Exception {
        // TODO: implementar SELECT por id
        String sql = "SELECT * FROM navios WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Navio navio = null;
        if (rs.next()) {
            navio = mapearResultSet(rs);
        }
        rs.close();
        ps.close();
        return navio;
    }

    public List<Navio> listarTodos() throws Exception {
        // TODO: implementar SELECT de todos os navios
        List<Navio> lista = new ArrayList<>();
        String sql = "SELECT * FROM navios";
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

    private Navio mapearResultSet(ResultSet rs) throws SQLException {
        // TODO: converter linha do ResultSet num objeto Navio
        Navio navio = new Navio();
        navio.setId(rs.getInt("id"));
        navio.setNome(rs.getString("nome"));
        navio.setCodigoIMO(rs.getString("codigo_imo"));
        navio.setTipo(TipoNavio.valueOf(rs.getString("tipo")));
        navio.setCapacidadeMaxima(rs.getDouble("capacidade_maxima"));
        navio.setNumTanques(rs.getInt("num_tanques"));
        navio.setBandeira(rs.getString("bandeira"));
        navio.setAnoFabrico(rs.getInt("ano_fabrico"));
        navio.setEstado(EstadoNavio.valueOf(rs.getString("estado")));
        navio.setPortoAtualId(rs.getInt("porto_atual_id"));
        return navio;
    }
}
