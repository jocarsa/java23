
package j058.centroestudios;

public class Curso {
    private String nombre;
    private int duracionEnHoras;

    public Curso() {
    }

    public Curso(String nombre, int duracionEnHoras) {
        this.nombre = nombre;
        this.duracionEnHoras = duracionEnHoras;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getDuracionEnHoras() {
        return duracionEnHoras;
    }

    public void setDuracionEnHoras(int duracionEnHoras) {
        this.duracionEnHoras = duracionEnHoras;
    }
}
