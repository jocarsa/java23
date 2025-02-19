package j058.centroestudios;

public class Estudiante {
    private String nombre;
    private int edad;
    private String username; // NEW
    private String password; // NEW

    public Estudiante(String nombre, int edad, String username, String password) {
        this.nombre = nombre;
        this.edad = edad;
        this.username = username;
        this.password = password;
    }

    // For backward compatibility if you want:
    public Estudiante(String nombre, int edad) {
        this(nombre, edad, null, null);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
