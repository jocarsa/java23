package j058.centroestudios;

public class Curso {
    private int id; // Nuevo campo
    private String nombre;
    private int duracionEnHoras;

    public Curso(String nombre, int duracionEnHoras) {
        this.nombre = nombre;
        this.duracionEnHoras = duracionEnHoras;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getDuracionEnHoras() { return duracionEnHoras; }
    public void setDuracionEnHoras(int duracionEnHoras) { this.duracionEnHoras = duracionEnHoras; }
}
