package j058.centroestudios;

import java.util.ArrayList;
import java.util.List;

public class Asignatura {
    private int id; // Nuevo campo
    private String nombre;
    private Curso curso;
    private List<Estudiante> estudiantesEnrolled = new ArrayList<>();
    private List<Profesor> profesoresEnrolled = new ArrayList<>();
    private List<Tema> temas = new ArrayList<>();

    public Asignatura(String nombre, Curso curso) {
        this.nombre = nombre;
        this.curso = curso; 
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Curso getCurso() { return curso; }
    public void setCurso(Curso curso) { this.curso = curso; }

    public List<Estudiante> getEstudiantesEnrolled() { return estudiantesEnrolled; }
    public List<Profesor> getProfesoresEnrolled() { return profesoresEnrolled; }
    public List<Tema> getTemas() { return temas; }

    public void addEstudiante(Estudiante e) { estudiantesEnrolled.add(e); }
    public void addProfesor(Profesor p) { profesoresEnrolled.add(p); }
    public void addTema(Tema t) { temas.add(t); }
}
