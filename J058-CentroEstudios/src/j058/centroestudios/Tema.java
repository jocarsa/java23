package j058.centroestudios;

import java.util.ArrayList;
import java.util.List;

public class Tema {
    private String nombre;
    private List<Contenido> contenidos = new ArrayList<>();

    public Tema(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Contenido> getContenidos() {
        return contenidos;
    }

    // Optionally add a convenience method:
    public void addContenido(Contenido c) {
        this.contenidos.add(c);
    }
}
