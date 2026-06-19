package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;

import java.sql.Date;
import java.util.List;

public class TripulanteDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    static final RowMapper<Tripulante> MAPPER = rs -> {
        Date dn = rs.getDate("dataNascimento");
        return new Tripulante(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("numCertificado"),
                FuncaoTripulante.valueOf(rs.getString("funcao")),
                rs.getString("estadoDisponibilidade"),
                dn == null ? null : dn.toLocalDate(),
                rs.getString("email")
        );
    };

    public void inserir(Tripulante t) throws Exception {
        String sql = "INSERT INTO Tripulante (nome, dataNascimento, email, numCertificado, funcao, estadoDisponibilidade) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        int id = db.create(sql,
                t.getNome(),
                t.getDataNascimento() == null ? null : Date.valueOf(t.getDataNascimento()),
                t.getEmail(),
                t.getNumeroCertificado(),
                t.getFuncao() == null ? null : t.getFuncao().name(),
                t.getEstadoDisponibilidade() == null ? "DISPONIVEL" : t.getEstadoDisponibilidade());
        if (id > 0) t.setId(id);
    }

    public void atualizar(Tripulante t) throws Exception {
        String sql = "UPDATE Tripulante SET nome=?, dataNascimento=?, email=?, numCertificado=?, " +
                "funcao=?, estadoDisponibilidade=? WHERE id=?";
        db.execute(sql,
                t.getNome(),
                t.getDataNascimento() == null ? null : Date.valueOf(t.getDataNascimento()),
                t.getEmail(),
                t.getNumeroCertificado(),
                t.getFuncao() == null ? null : t.getFuncao().name(),
                t.getEstadoDisponibilidade() == null ? "DISPONIVEL" : t.getEstadoDisponibilidade(),
                t.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM Tripulante WHERE id=?", id);
    }

    public Tripulante buscarPorId(int id) throws Exception {
        List<Tripulante> resultado = db.select("SELECT * FROM Tripulante WHERE id=?", MAPPER, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Tripulante> listarTodos() throws Exception {
        return db.select("SELECT * FROM Tripulante", MAPPER);
    }
}
