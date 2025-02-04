/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package j003.entradas;

import java.util.Scanner;

/**
 *
 * @author Admin
 */
public class J003Entradas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Scanner nombre = new Scanner(System.in); 
        System.out.println("Introduce el nombre:");
        
        String usuario = nombre.nextLine();  
        System.out.println("El nombre de usuario : " + usuario);
    }
    
}
