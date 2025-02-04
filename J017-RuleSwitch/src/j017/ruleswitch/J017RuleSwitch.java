package j017.ruleswitch;

// Autor Jose Vicente Carratala
public class J017RuleSwitch{
    public static void main(String[] args) {
        String diadelasemana = "lunes";
        switch(diadelasemana){
            case "lunes" -> System.out.println("Hoy es el peor día de la semana");
            case "martes" -> System.out.println("Hoy es el segundo peor día de la semana");
            case "miercoles" -> System.out.println("Ya estamos a mitad de semana");
            case "jueves" -> System.out.println("Ya es casi viernes");
            case "viernes" -> System.out.println("Ya casi es fin de semana");
            case "sabado" -> System.out.println("Hoy es el mejor día de la semana");
            case "domingo" -> System.out.println("Parece mentira que mañana ya sea lunes");
        }
    }
}
