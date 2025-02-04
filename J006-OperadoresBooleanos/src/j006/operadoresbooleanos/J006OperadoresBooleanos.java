package j006.operadoresbooleanos;

// Autor Jose Vicente Carratala
public class J006OperadoresBooleanos{
    public static void main(String[] args) {
        System.out.println(4 == 4 && 3 == 3 && 2 == 2);
        System.out.println(4 == 4 && 3 == 3 && 2 == 1);
        
        System.out.println(4 == 4 || 3 == 3 || 2 == 2);
        System.out.println(4 == 4 || 3 == 3 || 2 == 1);
        System.out.println(4 == 4 || 3 == 2 || 2 == 1);
        System.out.println(4 == 3 || 3 == 2 || 2 == 1);
    }
}
