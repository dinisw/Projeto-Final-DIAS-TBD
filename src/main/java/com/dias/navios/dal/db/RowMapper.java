package com.dias.navios.dal.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converte uma linha de um ResultSet num objeto do tipo T.
 * Usado pelo metodo select() do DatabaseConnection.
 */
@FunctionalInterface
public interface RowMapper<T> {
    T mapRow(ResultSet rs) throws SQLException;
}
