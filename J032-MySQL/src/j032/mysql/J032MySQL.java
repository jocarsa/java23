package j032.mysql;

// Autor Jose Vicente Carratala

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class J032MySQL{
    public static void main(String[] args) {
        // CREATE DATABASE empresa;
        // CREATE USER 'empresa'@'localhost' IDENTIFIED VIA mysql_native_password USING '***';
        // GRANT USAGE ON *.* TO 'empresa'@'localhost' REQUIRE NONE WITH MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0 MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;
        // GRANT ALL PRIVILEGES ON `empresa`.* TO 'empresa'@'localhost';
        // CREATE TABLE `empresa`.`clientes` (`Identificador` INT(255) NOT NULL AUTO_INCREMENT , `nombre` VARCHAR(255) NOT NULL , `apellidos` VARCHAR(255) NOT NULL , `email` VARCHAR(255) NOT NULL , PRIMARY KEY (`Identificador`)) ENGINE = InnoDB;
        String url = "jdbc:mysql://localhost:3306/empresa"; 
        String usuario = "empresa"; 
        String contrasena = "empresa"; 
        
        // CREATE
        try {
            String peticion = "INSERT INTO clientes (nombre, apellidos,email) VALUES (?, ?, ?)";
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
            PreparedStatement preparedStatement = conexion.prepareStatement(peticion);
            
            preparedStatement.setString(1, "Jose Vicente");
            preparedStatement.setString(2, "Carratalá");
            preparedStatement.setString(3, "info@jocarsa.com");

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted.");

            preparedStatement.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // READ
        try {
            String peticion = "SELECT * FROM clientes";
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);          
            Statement llamada = conexion.createStatement();

            ResultSet resultados = llamada.executeQuery(peticion);

            while (resultados.next()) {
                String nombre = resultados.getString("nombre");
                String apellidos = resultados.getString("apellidos");
                String email = resultados.getString("email");

                System.out.println(nombre+apellidos+email);
            }

            resultados.close();
            llamada.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // UPDATE
        try {
            String peticion = "UPDATE clientes SET nombre = 'Juan';";
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
            PreparedStatement preparedStatement = conexion.prepareStatement(peticion);
            
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted.");

            preparedStatement.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // DELETE
        try {
            String peticion = "DELETE FROM clientes WHERE Identificador = 1;";
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
            PreparedStatement preparedStatement = conexion.prepareStatement(peticion);
            
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println(rowsAffected + " row(s) inserted.");

            preparedStatement.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
