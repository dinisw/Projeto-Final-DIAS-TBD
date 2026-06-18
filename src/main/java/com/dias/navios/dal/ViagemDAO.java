package com.dias.navios.dal;

import com.dias.navios.model.Carga;
import com.dias.navios.model.EstadoViagem;
import com.dias.navios.model.Tripulante;
import com.dias.navios.model.Viagem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ViagemDAO {

    public void inserir(Viagem viagem) throws Exception {
        String sql = "INSERT INTO viagens (porto_origem_id, porto_destino_id, data_partida, data_chegada_prevista, navio_id, estado) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, viagem.getPortoOrigemId());
        ps.setInt(2, viagem.getPortoDestinoId());
        ps.setDate(3, Date.valueOf(viagem.getDataPartida()));
        ps.setDate(4, viagem.getDataChegadaPrevista() != null ? Date.valueOf(viagem.getDataChegadaPrevista()) : null);
        ps.setInt(5, viagem.getNavioId());
        ps.setString(6, viagem.getEstado().name());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) {
            viagem.setId(keys.getInt(1));
        }
        keys.close();
        ps.close();
    }

    public void atualizar(Viagem viagem) throws Exception {
        String sql = "UPDATE viagens SET porto_origem_id=?, porto_destino_id=?, data_partida=?, data_chegada_prevista=?, navio_id=?, estado=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagem.getPortoOrigemId());
        ps.setInt(2, viagem.getPortoDestinoId());
        ps.setDate(3, Date.valueOf(viagem.getDataPartida()));
        ps.setDate(4, viagem.getDataChegadaPrevista() != null ? Date.valueOf(viagem.getDataChegadaPrevista()) : null);
        ps.setInt(5, viagem.getNavioId());
        ps.setString(6, viagem.getEstado().name());
        ps.setInt(7, viagem.getId());
        ps.executeUpdate();
        ps.close();
    }

    public void atualizarEstado(int id, EstadoViagem estado) throws Exception {
        String sql = "UPDATE viagens SET estado=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, estado.name());
        ps.setInt(2, id);
        ps.executeUpdate();
        ps.close();
    }

    public void apagar(int id) throws Exception {
        removerTodasCargas(id);
        removerTodosTripulantes(id);
        String sql = "DELETE FROM viagens WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    public Viagem buscarPorId(int id) throws Exception {
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
        if (viagem != null) {
            viagem.setCargasIds(listarIdsCargasDaViagem(id));
            viagem.setTripulantesIds(listarIdsTripulantesDaViagem(id));
        }
        return viagem;
    }

    public List<Viagem> listarTodos() throws Exception {
        List<Viagem> lista = new ArrayList<>();
        String sql = "SELECT * FROM viagens ORDER BY data_partida DESC";
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
        String sql = "SELECT COUNT(*) FROM viagens WHERE navio_id=? AND estado IN ('EM_CURSO','PLANEADA')";
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

    public boolean cargaEstaEmViagemAtiva(int cargaId) throws Exception {
        String sql = "SELECT COUNT(*) FROM viagem_carga vc INNER JOIN viagens v ON vc.viagem_id=v.id WHERE vc.carga_id=? AND v.estado IN ('PLANEADA','EM_CURSO')";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, cargaId);
        ResultSet rs = ps.executeQuery();
        boolean resultado = false;
        if (rs.next()) {
            resultado = rs.getInt(1) > 0;
        }
        rs.close();
        ps.close();
        return resultado;
    }

    public boolean tripulanteTemViagemAtiva(int tripulanteId) throws Exception {
        String sql = "SELECT COUNT(*) FROM viagem_tripulante vt INNER JOIN viagens v ON vt.viagem_id=v.id WHERE vt.tripulante_id=? AND v.estado IN ('PLANEADA','EM_CURSO')";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, tripulanteId);
        ResultSet rs = ps.executeQuery();
        boolean resultado = false;
        if (rs.next()) {
            resultado = rs.getInt(1) > 0;
        }
        rs.close();
        ps.close();
        return resultado;
    }

    // --- Associações Carga ---

    public void adicionarCarga(int viagemId, int cargaId) throws Exception {
        String sql = "INSERT INTO viagem_carga (viagem_id, carga_id) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.setInt(2, cargaId);
        ps.executeUpdate();
        ps.close();
    }

    public void removerCarga(int viagemId, int cargaId) throws Exception {
        String sql = "DELETE FROM viagem_carga WHERE viagem_id=? AND carga_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.setInt(2, cargaId);
        ps.executeUpdate();
        ps.close();
    }

    public void removerTodasCargas(int viagemId) throws Exception {
        String sql = "DELETE FROM viagem_carga WHERE viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.executeUpdate();
        ps.close();
    }

    public List<Carga> listarCargasDaViagem(int viagemId) throws Exception {
        List<Carga> lista = new ArrayList<>();
        String sql = "SELECT c.* FROM cargas c INNER JOIN viagem_carga vc ON c.id=vc.carga_id WHERE vc.viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ResultSet rs = ps.executeQuery();
        CargaDAO cargaDAO = new CargaDAO();
        while (rs.next()) {
            lista.add(cargaDAO.mapearResultSet(rs));
        }
        rs.close();
        ps.close();
        return lista;
    }

    // --- Associações Tripulante ---

    public void adicionarTripulante(int viagemId, int tripulanteId) throws Exception {
        String sql = "INSERT INTO viagem_tripulante (viagem_id, tripulante_id) VALUES (?, ?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.setInt(2, tripulanteId);
        ps.executeUpdate();
        ps.close();
    }

    public void removerTripulante(int viagemId, int tripulanteId) throws Exception {
        String sql = "DELETE FROM viagem_tripulante WHERE viagem_id=? AND tripulante_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.setInt(2, tripulanteId);
        ps.executeUpdate();
        ps.close();
    }

    public void removerTodosTripulantes(int viagemId) throws Exception {
        String sql = "DELETE FROM viagem_tripulante WHERE viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ps.executeUpdate();
        ps.close();
    }

    public List<Tripulante> listarTripulantesDaViagem(int viagemId) throws Exception {
        List<Tripulante> lista = new ArrayList<>();
        String sql = "SELECT t.* FROM tripulantes t INNER JOIN viagem_tripulante vt ON t.id=vt.tripulante_id WHERE vt.viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ResultSet rs = ps.executeQuery();
        TripulanteDAO tripulanteDAO = new TripulanteDAO();
        while (rs.next()) {
            lista.add(tripulanteDAO.mapearResultSet(rs));
        }
        rs.close();
        ps.close();
        return lista;
    }

    public List<Viagem> listarViagensDeTripulante(int tripulanteId) throws Exception {
        List<Viagem> lista = new ArrayList<>();
        String sql = "SELECT v.* FROM viagens v INNER JOIN viagem_tripulante vt ON v.id=vt.viagem_id WHERE vt.tripulante_id=? ORDER BY v.data_partida DESC";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, tripulanteId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            lista.add(mapearResultSet(rs));
        }
        rs.close();
        ps.close();
        return lista;
    }

    // --- Auxiliares ---

    private List<Integer> listarIdsCargasDaViagem(int viagemId) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT carga_id FROM viagem_carga WHERE viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        rs.close();
        ps.close();
        return ids;
    }

    private List<Integer> listarIdsTripulantesDaViagem(int viagemId) throws Exception {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT tripulante_id FROM viagem_tripulante WHERE viagem_id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, viagemId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            ids.add(rs.getInt(1));
        }
        rs.close();
        ps.close();
        return ids;
    }

    private Viagem mapearResultSet(ResultSet rs) throws SQLException {
        Viagem v = new Viagem();
        v.setId(rs.getInt("id"));
        v.setPortoOrigemId(rs.getInt("porto_origem_id"));
        v.setPortoDestinoId(rs.getInt("porto_destino_id"));
        v.setDataPartida(rs.getDate("data_partida").toLocalDate());
        Date chegada = rs.getDate("data_chegada_prevista");
        if (chegada != null) {
            v.setDataChegadaPrevista(chegada.toLocalDate());
        }
        v.setNavioId(rs.getInt("navio_id"));
        v.setEstado(EstadoViagem.valueOf(rs.getString("estado")));
        return v;
    }
}
