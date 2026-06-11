package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.EstadoNavio;
import com.dias.navios.model.Navio;
import com.dias.navios.model.TipoNavio;

import java.util.List;

public class NavioDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    // Converte uma linha da tabela "navios" num objeto Navio
    private final RowMapper<Navio> mapper = rs -> new Navio(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("codigo_imo"),
            TipoNavio.valueOf(rs.getString("tipo")),
            rs.getDouble("capacidade_maxima"),
            rs.getInt("num_tanques"),
            rs.getString("bandeira"),
            rs.getInt("ano_fabrico"),
            EstadoNavio.valueOf(rs.getString("estado")),
            rs.getInt("porto_atual_id")
    );

    public void inserir(Navio navio) throws Exception {
        String sql = "INSERT INTO navios (nome, codigo_imo, tipo, capacidade_maxima, num_tanques, " +
                "bandeira, ano_fabrico, estado, porto_atual_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                navio.getNome(),
                navio.getCodigoIMO(),
                navio.getTipo() == null ? null : navio.getTipo().name(),
                navio.getCapacidadeMaxima(),
                navio.getNumTanques(),
                navio.getBandeira(),
                navio.getAnoFabrico(),
                navio.getEstado() == null ? null : navio.getEstado().name(),
                navio.getPortoAtualId());
        if (id > 0) navio.setId(id);
    }

    public void atualizar(Navio navio) throws Exception {
        String sql = "UPDATE navios SET nome=?, codigo_imo=?, tipo=?, capacidade_maxima=?, num_tanques=?, " +
                "bandeira=?, ano_fabrico=?, estado=?, porto_atual_id=? WHERE id=?";
        db.execute(sql,
                navio.getNome(),
                navio.getCodigoIMO(),
                navio.getTipo() == null ? null : navio.getTipo().name(),
                navio.getCapacidadeMaxima(),
                navio.getNumTanques(),
                navio.getBandeira(),
                navio.getAnoFabrico(),
                navio.getEstado() == null ? null : navio.getEstado().name(),
                navio.getPortoAtualId(),
                navio.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM navios WHERE id=?", id);
    }

    public Navio buscarPorId(int id) throws Exception {
        List<Navio> resultado = db.select("SELECT * FROM navios WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Navio> listarTodos() throws Exception {
        return db.select("SELECT * FROM navios", mapper);
    }
}
