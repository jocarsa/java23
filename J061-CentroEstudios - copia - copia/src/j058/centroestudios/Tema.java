package j058.centroestudios;

import java.util.ArrayList;
import java.util.List;

public class Tema {
    private int id; // Nuevo campo
    private String nombre;
    private List<Contenido> contenidos = new ArrayList<>();

    public Tema(String nombre) {
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Contenido> getContenidos() { return contenidos; }

    public void addContenido(Contenido c) { this.contenidos.add(c); }
}
