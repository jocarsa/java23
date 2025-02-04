package j028.agendaconclases;

// Autor Jose Vicente Carratalá Sanchis

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

public class ConexionDB {
    String url = "jdbc:sqlite:empresa.db";
    public void comprueba(Connection conn) throws SQLException{
        // Crear la tabla clientes
        String sqlCreate = "CREATE TABLE IF NOT EXISTS clientes ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "nombre TEXT NOT NULL,"
                + "apellidos TEXT NOT NULL,"
                + "email TEXT NOT NULL UNIQUE);";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreate);
            System.out.println("Tabla clientes creada o ya existente.");
        }
    }
    public void inserta(Connection conn,String nombre, String apellidos, String email) throws SQLException{
        // Insertar datos en la tabla clientes
        String sqlInsert = "INSERT INTO clientes (nombre, apellidos, email) VALUES"
                + "('"+nombre+"', '"+apellidos+"', '"+email+"')";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sqlInsert);
            System.out.println("Datos insertados en la tabla clientes.");
        }
    }
    public void conecta(String nombre, String apellidos, String email){
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Me he conectado.");    
                comprueba(conn);
                inserta(conn,nombre,apellidos,email);   
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
