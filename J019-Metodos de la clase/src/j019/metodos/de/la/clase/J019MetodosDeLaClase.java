package j019.metodos.de.la.clase;

// Autor Jose Vicente Carratala
public class J019MetodosDeLaClase{
    static private int edad = 46;
    
    static public int dameEdad(){
        return edad;
    }
    static public int dobleEdad(){
        int doble = edad*2;
        return doble;
    }
    
    public static void main(String[] args) {
        System.out.println("Mi edad es de: "+dameEdad()+" años");
        System.out.println("El doble de mi edad es de: "+dobleEdad()+" años");
    }
}
