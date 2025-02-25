package j058.centroestudios;

public class Profesor {
    private int id; // Nuevo campo
    private String nombre;
    private String especialidad;
    private String username;
    private String password;

    public Profesor(String nombre, String especialidad, String username, String password) {
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.username = username;
        this.password = password;
    }

    public Profesor(String nombre, String especialidad) {
        this(nombre, especialidad, null, null);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
