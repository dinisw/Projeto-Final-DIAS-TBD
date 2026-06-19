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

    /** Abre (ou reutiliza) a ligacao cifrada a base de dados.
     *  Retenta automaticamente se o Azure SQL estiver a acordar (erro 40613). */
    private Connection connect() {
        int maxAttempts = 6;
        int waitSeconds = 10;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                if (connection != null && !connection.isClosed()) {
                    return connection;
                }
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                String url = "jdbc:sqlserver://" + serverName +
                        ";databaseName=" + databaseName +
                        ";encrypt=true;trustServerCertificate=true;loginTimeout=60";
                connection = DriverManager.getConnection(url, username, password);
                return connection;
            } catch (SQLException ex) {
                boolean isAutoPause = ex.getErrorCode() == 40613
                        || (ex.getMessage() != null && ex.getMessage().contains("not currently available"));
                if (isAutoPause && attempt < maxAttempts) {
                    System.out.println("BD a acordar, a aguardar " + waitSeconds
                            + "s (tentativa " + attempt + "/" + maxAttempts + ")...");
                    try { Thread.sleep(waitSeconds * 1000L); }
                    catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                } else {
                    System.err.println("Ligacao a base de dados falhou: " + ex.getMessage());
                    return null;
                }
            } catch (Exception ex) {
                System.err.println("Ligacao a base de dados falhou: " + ex.getMessage());
                return null;
            }
        }
        return null;
    }

    /** Faz um SELECT 1 para acordar a BD sem bloquear a UI. */
    public void warmUp() {
        try {
            select("SELECT 1", rs -> rs.getInt(1));
            System.out.println("BD pronta.");
        } catch (Exception ex) {
            // falha silenciosa — o primeiro acesso real vai retentar
        }
    }

    /** Fecha a ligacao. Chamar quando a aplicacao termina. */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception ex) {
            System.err.println("Erro ao fechar a ligacao: " + ex.getMessage());
        }
    }

    // ── SELECT ────────────────────────────────────────────────────────────────
    public <T> ArrayList<T> select(String sql, RowMapper<T> mapper, Object... params) throws Exception {
        ArrayList<T> results = new ArrayList<>();
        Connection conn = connect();
        if (conn == null) throw new Exception("Não foi possível ligar à base de dados.");
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.mapRow(rs));
                }
            }
        }
        return results;
    }

    // ── INSERT (devolve a chave gerada) ───────────────────────────────────────
    public int create(String sql, Object... params) throws Exception {
        Connection conn = connect();
        if (conn == null) throw new Exception("Não foi possível ligar à base de dados.");
        conn.setAutoCommit(false);
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(stmt, params);
            stmt.executeUpdate();
            int generatedId = 0;
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) generatedId = keys.getInt(1);
            }
            conn.commit();
            return generatedId;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── UPDATE / DELETE (devolve o nr de linhas afetadas) ─────────────────────
    public int execute(String sql, Object... params) throws Exception {
        Connection conn = connect();
        if (conn == null) throw new Exception("Não foi possível ligar à base de dados.");
        conn.setAutoCommit(false);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            bind(stmt, params);
            int rowsAffected = stmt.executeUpdate();
            conn.commit();
            return rowsAffected;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    /** Preenche os parametros (?) do PreparedStatement pela ordem dada. */
    private void bind(PreparedStatement stmt, Object... params) throws SQLException {
        if (params == null) return;
        for (int i = 0; i < params.length; i++) {
            stmt.setObject(i + 1, params[i]);
        }
    }
}
