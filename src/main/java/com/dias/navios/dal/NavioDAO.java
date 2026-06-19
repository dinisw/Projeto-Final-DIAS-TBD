package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;

import java.util.List;

public class NavioDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    private static final String SELECT_BASE =
            "SELECT n.id, n.nome, n.codigoIMO, tn.nome AS tipoNavio, n.capacidadeMaxima, " +
            "n.numCompartimentos, n.bandeira, n.anoFabrico, n.estadoOperacional, n.portoAtualId " +
            "FROM Navio n JOIN TipoNavio tn ON tn.id = n.tipoNavioId";

    private final RowMapper<Navio> mapper = rs -> new Navio(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("codigoIMO"),
            TipoNavio.valueOf(rs.getString("tipoNavio")),
            rs.getDouble("capacidadeMaxima"),
            rs.getInt("numCompartimentos"),
            rs.getString("bandeira"),
            rs.getInt("anoFabrico"),
            EstadoNavio.valueOf(rs.getString("estadoOperacional")),
            rs.getInt("portoAtualId")
    );

    private int buscarIdTipoNavio(TipoNavio tipo) throws Exception {
        List<Integer> result = db.select(
                "SELECT id FROM TipoNavio WHERE nome=?",
                rs -> rs.getInt("id"),
                tipo.name());
        if (result.isEmpty()) throw new IllegalArgumentException("TipoNavio não encontrado: " + tipo);
        return result.get(0);
    }

    public int buscarMaxCargasDoTipo(int navioId) throws Exception {
        List<Integer> result = db.select(
                "SELECT tn.maxCargas FROM Navio n JOIN TipoNavio tn ON tn.id = n.tipoNavioId WHERE n.id=?",
                rs -> rs.getInt("maxCargas"),
                navioId);
        return result.isEmpty() ? 0 : result.get(0);
    }

    public void inserir(Navio navio) throws Exception {
        int tipoId = buscarIdTipoNavio(navio.getTipo());
        String sql = "INSERT INTO Navio (nome, codigoIMO, tipoNavioId, capacidadeMaxima, numCompartimentos, " +
                "bandeira, anoFabrico, estadoOperacional, portoAtualId) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                navio.getNome(),
                navio.getCodigoIMO(),
                tipoId,
                navio.getCapacidadeMaxima(),
                navio.getNumTanques(),
                navio.getBandeira(),
                navio.getAnoFabrico(),
                navio.getEstado() == null ? "ATIVO" : navio.getEstado().name(),
                navio.getPortoAtualId() == 0 ? null : navio.getPortoAtualId());
        if (id > 0) navio.setId(id);
    }

    public void atualizar(Navio navio) throws Exception {
        int tipoId = buscarIdTipoNavio(navio.getTipo());
        String sql = "UPDATE Navio SET nome=?, codigoIMO=?, tipoNavioId=?, capacidadeMaxima=?, " +
                "numCompartimentos=?, bandeira=?, anoFabrico=?, estadoOperacional=?, portoAtualId=? WHERE id=?";
        db.execute(sql,
                navio.getNome(),
                navio.getCodigoIMO(),
                tipoId,
                navio.getCapacidadeMaxima(),
                navio.getNumTanques(),
                navio.getBandeira(),
                navio.getAnoFabrico(),
                navio.getEstado() == null ? "ATIVO" : navio.getEstado().name(),
                navio.getPortoAtualId() == 0 ? null : navio.getPortoAtualId(),
                navio.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM Navio WHERE id=?", id);
    }

    public Navio buscarPorId(int id) throws Exception {
        List<Navio> resultado = db.select(SELECT_BASE + " WHERE n.id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Navio> listarTodos() throws Exception {
        return db.select(SELECT_BASE, mapper);
    }
}
