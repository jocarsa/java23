package j024.sqlite;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;

// Autor Jose Vicente Carratala
public class J024SQLite{
    public static void main(String[] args) {
        String url = "jdbc:sqlite:empresa.db";

        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Me he conectado.");
                
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
                
                // Insertar datos en la tabla clientes
                String sqlInsert = "INSERT INTO clientes (nombre, apellidos, email) VALUES"
                        + "('Juan', 'Perez', 'juan.perez@example.com'),"
                        + "('Maria', 'Gonzalez', 'maria.gonzalez@example.com');";
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sqlInsert);
                    System.out.println("Datos insertados en la tabla clientes.");
                }
                
                // Seleccionar todos los clientes
                String sqlSelect = "SELECT * FROM clientes";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sqlSelect)) {
                    while (rs.next()) {
                        System.out.println("ID: " + rs.getInt("id") + ", Nombre: " + rs.getString("nombre") +
                                ", Apellidos: " + rs.getString("apellidos") + ", Email: " + rs.getString("email"));
                    }
                }
                
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
