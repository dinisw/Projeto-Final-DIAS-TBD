package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.FuncaoTripulante;
import com.dias.navios.model.Tripulante;

import java.util.List;

public class TripulanteDAO {

    private final DatabaseConnection db = new DatabaseConnection();

    private final RowMapper<Tripulante> mapper = rs -> new Tripulante(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("numero_certificado"),
            FuncaoTripulante.valueOf(rs.getString("funcao")),
            rs.getBoolean("disponivel"),
            rs.getString("nacionalidade")
    );

    public void inserir(Tripulante t) throws Exception {
        String sql = "INSERT INTO tripulantes (nome, numero_certificado, funcao, disponivel, nacionalidade) " +
                "VALUES (?, ?, ?, ?, ?)";
        int id = db.create(sql,
                t.getNome(),
                t.getNumeroCertificado(),
                t.getFuncao() == null ? null : t.getFuncao().name(),
                t.isDisponivel(),
                t.getNacionalidade());
        if (id > 0) t.setId(id);
    }

    public void atualizar(Tripulante t) throws Exception {
        String sql = "UPDATE tripulantes SET nome=?, numero_certificado=?, funcao=?, disponivel=?, " +
                "nacionalidade=? WHERE id=?";
        db.execute(sql,
                t.getNome(),
                t.getNumeroCertificado(),
                t.getFuncao() == null ? null : t.getFuncao().name(),
                t.isDisponivel(),
                t.getNacionalidade(),
                t.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM tripulantes WHERE id=?", id);
    }

    public Tripulante buscarPorId(int id) throws Exception {
        List<Tripulante> resultado = db.select("SELECT * FROM tripulantes WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Tripulante> listarTodos() throws Exception {
        return db.select("SELECT * FROM tripulantes", mapper);
    }
}
