package com.dias.navios.dal.db;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;
import java.util.ArrayList;

/**
 * Gestao da ligacao a base de dados (SQL Server / Azure SQL).
 *
 * Segue a mesma abordagem do projeto de referencia "Java With SQL":
 *  - as credenciais sao lidas do ficheiro .env (biblioteca dotenv);
 *  - a ligacao e feita por JDBC (DriverManager) com encriptacao TLS;
 *  - metodos utilitarios select() / create() / execute() para os DAO.
 *
 * A ligacao usa "encrypt=true" para garantir que o trafego com a base de
 * dados vai sempre cifrado (obrigatorio no Azure SQL).
 */
public class DatabaseConnection {

    private final String serverName;
    private final String databaseName;
    private final String username;
    private final String password;
    private Connection connection;

    public DatabaseConnection() {
        // As credenciais ficam no .env (em src/main/resources) — nao alterar aqui.
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        this.serverName   = dotenv.get("DB_SERVER");
        this.databaseName = dotenv.get("DB_DATABASE");
        this.username     = dotenv.get("DB_USER");
        this.password     = dotenv.get("DB_PASSWORD");
    }

    // O Azure SQL gratuito "adormece" quando esta inativo e demora alguns
    // segundos a acordar — por isso tentamos ligar varias vezes antes de desistir.
    private static final int MAX_TENTATIVAS = 5;
    private static final long ESPERA_ENTRE_TENTATIVAS_MS = 5000;

    /** Abre (ou reutiliza) a ligacao cifrada a base de dados, com retentativas. */
    private Connection connect() {
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                if (connection == null || connection.isClosed()) {
                    Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                    String url = "jdbc:sqlserver://" + serverName +
                            ";databaseName=" + databaseName +
                            ";encrypt=true;trustServerCertificate=true;loginTimeout=30";
                    connection = DriverManager.getConnection(url, username, password);
                }
                return connection;
            } catch (Exception ex) {
                System.err.println("Tentativa " + tentativa + "/" + MAX_TENTATIVAS
                        + " de ligacao falhou: " + ex.getMessage());
                if (tentativa < MAX_TENTATIVAS) {
                    try {
                        Thread.sleep(ESPERA_ENTRE_TENTATIVAS_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        System.err.println("Nao foi possivel ligar a base de dados apos " + MAX_TENTATIVAS + " tentativas.");
        return null;
    }

    /** Fecha a ligacao (chamada no fim de cada operacao). */
    private void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception ex) {
            System.err.println("Erro ao fechar a ligacao: " + ex.getMessage());
        }
    }

    // ── SELECT ────────────────────────────────────────────────────────────────
    public <T> ArrayList<T> select(String sql, RowMapper<T> mapper, Object... params) {
        ArrayList<T> results = new ArrayList<>();
        try {
            Connection conn = connect();
            if (conn == null) return results;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                bind(stmt, params);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        results.add(mapper.mapRow(rs));
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Erro ao executar SELECT: " + ex.getMessage());
        } finally {
            disconnect();
        }
        return results;
    }

    // ── INSERT (devolve a chave gerada) ───────────────────────────────────────
    public int create(String sql, Object... params) {
        int generatedId = 0;
        try {
            Connection conn = connect();
            if (conn == null) return 0;
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                bind(stmt, params);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) generatedId = keys.getInt(1);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erro ao executar INSERT (rollback efetuado): " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            System.err.println("Erro de ligacao: " + ex.getMessage());
        } finally {
            disconnect();
        }
        return generatedId;
    }

    // ── UPDATE / DELETE (devolve o nr de linhas afetadas) ─────────────────────
    public int execute(String sql, Object... params) {
        int rowsAffected = 0;
        try {
            Connection conn = connect();
            if (conn == null) return 0;
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                bind(stmt, params);
                rowsAffected = stmt.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Erro ao executar UPDATE/DELETE (rollback efetuado): " + e.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            System.err.println("Erro de ligacao: " + ex.getMessage());
        } finally {
            disconnect();
        }
        return rowsAffected;
    }

    /** Preenche os parametros (?) do PreparedStatement pela ordem dada. */
    private void bind(PreparedStatement stmt, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
