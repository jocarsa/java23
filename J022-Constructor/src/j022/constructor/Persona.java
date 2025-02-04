package j022.constructor;

// Autor Jose Vicente Carratalá Sanchis

public class Persona {
    private int edad;
    
    public Persona(int nuevaedad){
        edad = nuevaedad;
    }
    
    public int getEdad(){
        return edad;
    }
    public void setEdad(int nuevaedad){
        edad = nuevaedad;
    }
}