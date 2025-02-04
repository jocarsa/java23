package j015.estructura.pkgif;

// Autor Jose Vicente Carratala
public class J015EstructuraIf{
    public static void main(String[] args) {
        int edad = 36;
        if(edad < 10){
            System.out.println("Eres un niño");
        }else if(edad >= 10 && edad < 20){
            System.out.println("Eres un adolescente");
        }else if(edad >= 20 && edad < 30){
            System.out.println("Eres un joven");
        }else{
            System.out.println("Ya no eres un joven");
        }
    }
}
