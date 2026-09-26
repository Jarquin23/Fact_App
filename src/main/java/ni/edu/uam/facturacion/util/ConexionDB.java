package ni.edu.uam.facturacion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionDB {

    private static final String URL  = "jdbc:postgresql://localhost:5432/facturacion_db";
    private static final String USER = "postgres";
    private static final String PASS = "1234";

    private ConexionDB() { }

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}