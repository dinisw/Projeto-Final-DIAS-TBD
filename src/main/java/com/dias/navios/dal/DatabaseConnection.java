package com.dias.navios.dal;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {

    private static Connection connection = null;

    private DatabaseConnection() {}

    public static Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            // Carrega o ficheiro .env com as credenciais da base de dados
            props.load(new FileInputStream(".env"));

            String url  = props.getProperty("DB_URL");
            String user = props.getProperty("DB_USER");
            String pass = props.getProperty("DB_PASSWORD");

            // Regista o driver do SQL Server explicitamente
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }

    public static void closeConnection() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connection = null;
        }
    }
}
