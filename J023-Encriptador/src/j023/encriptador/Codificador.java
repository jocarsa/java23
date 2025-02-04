package j023.encriptador;

// Autor Jose Vicente Carratalá Sanchis

public class Codificador {
    public String codifica(String entrada){
        String salida;                                                          // Defino la salida
        StringBuilder encadenador = new StringBuilder();                        // Creo un encadenador
        for (char c : entrada.toCharArray()) {                                  // Para cada caracter
            encadenador.append((char) (c + 1));                                 // Le subo un valor al ascii
        }
        salida = encadenador.toString();                                        // Lo convierto a cadena
        return salida;                                                          // Lo devuelvo
    }
    public String descodifica(String entrada){
        String salida;                                                          // Defino la salida
        StringBuilder encadenador = new StringBuilder();                        // Creo un encadenador
        for (char c : entrada.toCharArray()) {                                  // Para cada caracter
            encadenador.append((char) (c - 1));                                 // Le subo un valor al ascii
        }
        salida = encadenador.toString();                                        // Lo convierto a cadena
        return salida;  
    }   
}
