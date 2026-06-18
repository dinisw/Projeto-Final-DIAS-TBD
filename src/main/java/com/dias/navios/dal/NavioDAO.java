package com.dias.navios.dal;

import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NavioDAO {

    public void inserir(Navio navio) throws Exception {
        String sql = "INSERT INTO navios (nome, codigo_imo, tipo, capacidade_maxima, num_tanques, bandeira, ano_fabrico, estado, porto_atual_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, navio.getNome());
        ps.setString(2, navio.getCodigoIMO());
        ps.setString(3, navio.getTipo().name());
        ps.setDouble(4, navio.getCapacidadeMaxima());
        ps.setInt(5, navio.getNumTanques());
        ps.setString(6, navio.getBandeira());
        ps.setInt(7, navio.getAnoFabrico());
        ps.setString(8, navio.getEstado().name());
        if (navio.getPortoAtualId() > 0) {
            ps.setInt(9, navio.getPortoAtualId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            navio.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void atualizar(Navio navio) throws Exception {
        String sql = "UPDATE navios SET nome=?, codigo_imo=?, tipo=?, capacidade_maxima=?, num_tanques=?, bandeira=?, ano_fabrico=?, estado=?, porto_atual_id=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, navio.getNome());
        ps.setString(2, navio.getCodigoIMO());
        ps.setString(3, navio.getTipo().name());
        ps.setDouble(4, navio.getCapacidadeMaxima());
        ps.setInt(5, navio.getNumTanques());
        ps.setString(6, navio.getBandeira());
        ps.setInt(7, navio.getAnoFabrico());
        ps.setString(8, navio.getEstado().name());
        if (navio.getPortoAtualId() > 0) {
            ps.setInt(9, navio.getPortoAtualId());
        } else {
            ps.setNull(9, Types.INTEGER);
        }
        ps.setInt(10, navio.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        String sql = "DELETE FROM navios WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Navio buscarPorId(int id) throws Exception {
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
        List<Navio> lista = new ArrayList<>();
        String sql = "SELECT * FROM navios ORDER BY nome";
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

    public List<Navio> listarPorEstado(EstadoNavio estado) throws Exception {
        List<Navio> lista = new ArrayList<>();
        String sql = "SELECT * FROM navios WHERE estado=? ORDER BY nome";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, estado.name());
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lista.add(mapearResultSet(rs));
        }
        rs.close();
        ps.close();
        return lista;
    }

    Navio mapearResultSet(ResultSet rs) throws SQLException {
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
