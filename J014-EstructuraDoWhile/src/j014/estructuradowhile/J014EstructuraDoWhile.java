package j014.estructuradowhile;

// Autor Jose Vicente Carratala
public class J014EstructuraDoWhile{
    public static void main(String[] args) {
        int dia = 35;
        do{
            System.out.println("Hoy es el dia "+dia+" del mes");
            dia++;
        }while(dia < 31);
    }
}
