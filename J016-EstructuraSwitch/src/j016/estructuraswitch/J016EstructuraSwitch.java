package j016.estructuraswitch;

// Autor Jose Vicente Carratala
public class J016EstructuraSwitch{
    public static void main(String[] args) {
        String diadelasemana = "lunes";
        switch(diadelasemana){
            case "lunes":
                System.out.println("Hoy es el peor día de la semana");
                break;
            case "martes":
                System.out.println("Hoy es el segundo peor día de la semana");
                break;
            case "miercoles":
                System.out.println("Ya estamos a mitad de semana");
                break;
            case "jueves":
                System.out.println("Ya es casi viernes");
                break;
            case "viernes":
                System.out.println("Ya casi es fin de semana");
                break;
            case "sabado":
                System.out.println("Hoy es el mejor día de la semana");
                break;
            case "domingo":
                System.out.println("Parece mentira que mañana ya sea lunes");
                break;
            
        }
    }
}
