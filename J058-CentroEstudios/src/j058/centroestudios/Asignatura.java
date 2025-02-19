package j058.centroestudios;

import java.util.ArrayList;
import java.util.List;

public class Asignatura {
    private String nombre;
    private Curso curso;  // The Curso to which this Asignatura belongs

    // Who is enrolled in this Asignatura?
    private List<Estudiante> estudiantesEnrolled = new ArrayList<>();
    private List<Profesor>   profesoresEnrolled  = new ArrayList<>();

    // The list of Temas for this Asignatura
    private List<Tema> temas = new ArrayList<>();

    public Asignatura(String nombre, Curso curso) {
        this.nombre = nombre;
        this.curso = curso;
    }

    public String getNombre() {
        return nombre;
    }

    public Curso getCurso() {
        return curso;
    }

    public List<Estudiante> getEstudiantesEnrolled() {
        return estudiantesEnrolled;
    }

    public List<Profesor> getProfesoresEnrolled() {
        return profesoresEnrolled;
    }

    public List<Tema> getTemas() {
        return temas;
    }
    
    public void addEstudiante(Estudiante e) {
        estudiantesEnrolled.add(e);
    }

    public void addProfesor(Profesor p) {
        profesoresEnrolled.add(p);
    }

    public void addTema(Tema t) {
        temas.add(t);
    }
}
