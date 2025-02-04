package j023.encriptador;

// Autor Jose Vicente Carratala
public class J023Encriptador{
    
    public static void main(String[] args) {
        Codificador codifica = new Codificador();
        if(null == args[0]){
            System.out.println("operación no permitida");
        }else switch (args[0]) {
            case "codifica":
                System.out.println(codifica.codifica(args[1]));
                break;
            case "descodifica":
                System.out.println(codifica.descodifica(args[1]));
                break;
            default:
                System.out.println("operación no permitida");
                break;
        }
        
    }
}
