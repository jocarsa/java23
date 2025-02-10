package j031.escribirleer;

// Autor Jose Vicente Carratala

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileWriter;
import java.util.Scanner;

public class J031EscribirLeer{
    public static void main(String[] args) {
        // Creo el archivo
        try {
            File archivo = new File("agenda.txt");
            if (archivo.createNewFile()) {
              System.out.println("El archivo que he creado es: " + archivo.getName());
            } else {
              System.out.println("El archivo ya existe");
            }
            
          } catch (IOException e) {
            System.out.println("Ha ocurrido un error");
            e.printStackTrace();
          }
        // Ahora escribimos contenido
        try {
            FileWriter archivoescribir = new FileWriter("agenda.txt");
            archivoescribir.write("Este es un texto que escribo");
            archivoescribir.close();
            System.out.println("Todo ha ido correctamente");
          } catch (IOException e) {
            System.out.println("Ha ocurrido algún error");
            e.printStackTrace();
          }
        // Ahora vamos a leer contenido de un archivo
        try {
            File archivoleer = new File("agenda.txt");
            Scanner lector = new Scanner(archivoleer);
            while (lector.hasNextLine()) {
              String datos = lector.nextLine();
              System.out.println(datos);
            }
            lector.close();
          } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
          }
        // Ahora por último eliminamos un archivo
        File archivoeliminar = new File("agenda.txt"); 
        if (archivoeliminar.delete()) { 
          System.out.println("He podido eliminar el archivo: " + archivoeliminar.getName());
        } else {
          System.out.println("No he podido eliminarlo.");
        } 
    }
}
