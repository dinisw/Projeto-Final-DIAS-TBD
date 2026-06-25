package com.dias.navios.dal;

import com.dias.navios.dal.db.DatabaseConnection;
import com.dias.navios.dal.db.RowMapper;
import com.dias.navios.model.Porto;

import java.util.List;

public class PortoDAO {

    private final DatabaseConnection db = DatabaseConnection.getInstance();

    private final RowMapper<Porto> mapper = rs -> new Porto(
            rs.getInt("id"),
            rs.getString("nome"),
            rs.getString("pais"),
            rs.getString("codigo")
    );

    public void inserir(Porto porto) throws Exception {
        String sql = "INSERT INTO Porto (nome, pais, codigo) VALUES (?, ?, ?)";
        int id = db.create(sql,
                porto.getNome(),
                porto.getPais(),
                porto.getCodigo());
        if (id > 0) porto.setId(id);
    }

    public void atualizar(Porto porto) throws Exception {
        db.execute("UPDATE Porto SET nome=?, pais=?, codigo=? WHERE id=?",
                porto.getNome(), porto.getPais(), porto.getCodigo(), porto.getId());
    }

    public void apagar(int id) throws Exception {
        db.execute("DELETE FROM Porto WHERE id=?", id);
    }

    public Porto buscarPorId(int id) throws Exception {
        List<Porto> resultado = db.select("SELECT * FROM Porto WHERE id=?", mapper, id);
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public List<Porto> listarTodos() throws Exception {
        return db.select("SELECT * FROM Porto", mapper);
    }
}
