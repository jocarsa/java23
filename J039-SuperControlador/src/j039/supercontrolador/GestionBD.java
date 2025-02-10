package j039.supercontrolador;

// Autor Jose Vicente Carratalá Sanchis

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class GestionBD {
    static String url = "jdbc:mysql://localhost:3306/empresa"; 
    static String usuario = "empresa"; 
    static String contrasena = "empresa";
    static String persona = "";
    static int permisocrear;
    static int permisoleer;
    static int permisoactualizar;
    static int permisoeliminar;
    public GestionBD(String nuevousuario){
        persona = nuevousuario;
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
            String peticion = "SELECT \n" +
                "perfiles.crear,\n" +
                "perfiles.leer,\n" +
                "perfiles.actualizar,\n" +
                "perfiles.eliminar\n" +
                "FROM perfiles\n" +
                "LEFT JOIN usuariosgrupos \n" +
                "ON usuariosgrupos.grupo = perfiles.Identificador\n" +
                "LEFT JOIN usuarios\n" +
                "ON usuariosgrupos.usuario = usuarios.Identificador\n" +
                "WHERE usuarios.usuario = '"+persona+"'\n" +
                ";";        
            Statement llamada = conexion.createStatement();
            ResultSet resultados = llamada.executeQuery(peticion);
            while (resultados.next()) {
                permisocrear = resultados.getInt("crear");
                permisoleer = resultados.getInt("leer");
                permisoactualizar = resultados.getInt("actualizar");
                permisoeliminar = resultados.getInt("eliminar");
            }

            resultados.close();
            llamada.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void dameTablas(){
        try {
            Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
            String peticion = "SHOW TABLES;";        
            Statement llamada = conexion.createStatement();
            ResultSet resultados = llamada.executeQuery(peticion);
            while (resultados.next()) {
                String tabla = resultados.getString("tables_in_empresa");
                System.out.println("Tengo una tabla que se llama: "+tabla);
            }

            resultados.close();
            llamada.close();
            conexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void insertaDato(String tabla,String datos){
        if(permisocrear == 1){
            try {
                String peticion = "INSERT INTO "+tabla+" VALUES ("+datos+");";        
                Connection conexion = DriverManager.getConnection(url, usuario, contrasena);
                PreparedStatement preparedStatement = conexion.prepareStatement(peticion);

                int rowsAffected = preparedStatement.executeUpdate();
                System.out.println(rowsAffected + " row(s) inserted.");

                preparedStatement.close();
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }else{
            System.out.println("No tienes permisos");
        }
    }
}
