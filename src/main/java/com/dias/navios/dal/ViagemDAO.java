package com.dias.navios.dal;

import com.dias.navios.model.EstadoViagem;
import com.dias.navios.model.Viagem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViagemDAO {

    public void inserir(Viagem viagem) throws Exception {
        // TODO: implementar INSERT na tabela viagens
        String sql = "INSERT INTO viagens (porto_origem_id, porto_destino_id, data_partida, data_chegada_prevista, navio_id, estado) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        // TODO: preencher os parametros
        ps.close();
    }

    public void atualizarEstado(int id, EstadoViagem estado) throws Exception {
        // TODO: atualizar apenas o estado da viagem
        String sql = "UPDATE viagens SET estado=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, estado.name());
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        // TODO: implementar DELETE na tabela viagens
        String sql = "DELETE FROM viagens WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Viagem buscarPorId(int id) throws Exception {
        // TODO: implementar SELECT por id
        String sql = "SELECT * FROM viagens WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Viagem viagem = null;
        if (rs.next()) {
            viagem = mapearResultSet(rs);
        }
        rs.close();
        ps.close();
        return viagem;
    }

    public List<Viagem> listarTodos() throws Exception {
        // TODO: implementar SELECT de todas as viagens
        List<Viagem> lista = new ArrayList<>();
        String sql = "SELECT * FROM viagens";
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

    public boolean navioTemViagemAtiva(int navioId) throws Exception {
        // Regra de negocio: um navio so pode ter uma viagem ativa de cada vez
        String sql = "SELECT COUNT(*) FROM viagens WHERE navio_id=? AND estado='EM_CURSO'";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, navioId);
        ResultSet rs = ps.executeQuery();
        boolean temViagem = false;
        if (rs.next()) {
            temViagem = rs.getInt(1) > 0;
        }
        rs.close();
        ps.close();
        return temViagem;
    }

    private Viagem mapearResultSet(ResultSet rs) throws SQLException {
        // TODO: converter linha do ResultSet num objeto Viagem
        Viagem v = new Viagem();
        v.setId(rs.getInt("id"));
        v.setPortoOrigemId(rs.getInt("porto_origem_id"));
        v.setPortoDestinoId(rs.getInt("porto_destino_id"));
        v.setDataPartida(rs.getDate("data_partida").toLocalDate());
        v.setDataChegadaPrevista(rs.getDate("data_chegada_prevista").toLocalDate());
        v.setNavioId(rs.getInt("navio_id"));
        v.setEstado(EstadoViagem.valueOf(rs.getString("estado")));
        return v;
    }
}
