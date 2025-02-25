package j058.centroestudios;

import java.io.*;
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import java.sql.*;
import java.util.*;

public class J058CentroEstudios {
    
    // --- SSL Configuration ---
    private static final int PORT = 8443; // SSL port
    private static final String KEYSTORE_PATH = "/home/keystore.jks";
    private static final String KEYSTORE_PASSWORD = "MasterMedia123$";
    
    // --- In-Memory Lists ---
    private static final List<Estudiante> estudiantes = new ArrayList<>();
    private static final List<Profesor>   profesores  = new ArrayList<>();
    private static final List<Curso>      cursos      = new ArrayList<>();
    private static final List<Asignatura> asignaturas = new ArrayList<>();
    
    // --- DB Configuration ---
    private static final String DB_URL      = "jdbc:mysql://localhost:3306/centroestudios";
    private static final String DB_USER     = "centroestudios";
    private static final String DB_PASSWORD = "Centroestudios123$";
    
    // ===============================
    // === ENTITY CLASSES ============
    // ===============================
    
    public static class Estudiante {
        private String nombre;
        private int edad;
        private String username;
        private String password;
        
        public Estudiante(String nombre, int edad, String username, String password) {
            this.nombre = nombre;
            this.edad = edad;
            this.username = username;
            this.password = password;
        }
        
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
    
    public static class Profesor {
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
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getEspecialidad() { return especialidad; }
        public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class Curso {
        private String nombre;
        private int duracionEnHoras;
        
        public Curso(String nombre, int duracionEnHoras) {
            this.nombre = nombre;
            this.duracionEnHoras = duracionEnHoras;
        }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public int getDuracionEnHoras() { return duracionEnHoras; }
        public void setDuracionEnHoras(int duracionEnHoras) { this.duracionEnHoras = duracionEnHoras; }
    }
    
    public static class Asignatura {
        private String nombre;
        private Curso curso;
        private List<Estudiante> estudiantes = new ArrayList<>();
        private List<Profesor> profesores = new ArrayList<>();
        private List<Tema> temas = new ArrayList<>();
        
        public Asignatura(String nombre, Curso curso) {
            this.nombre = nombre;
            this.curso = curso;
        }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public Curso getCurso() { return curso; }
        public void setCurso(Curso curso) { this.curso = curso; }
        public List<Estudiante> getEstudiantes() { return estudiantes; }
        public List<Profesor> getProfesores() { return profesores; }
        public List<Tema> getTemas() { return temas; }
        
        public void addEstudiante(Estudiante e) { estudiantes.add(e); }
        public void addProfesor(Profesor p) { profesores.add(p); }
        public void addTema(Tema t) { temas.add(t); }
    }
    
    public static class Tema {
        private String nombre;
        private List<Contenido> contenidos = new ArrayList<>();
        
        public Tema(String nombre) {
            this.nombre = nombre;
        }
        
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public List<Contenido> getContenidos() { return contenidos; }
        public void addContenido(Contenido c) { contenidos.add(c); }
    }
    
    public static class Contenido {
        private String titulo;
        private String texto;
        
        public Contenido(String titulo, String texto) {
            this.titulo = titulo;
            this.texto = texto;
        }
        
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getTexto() { return texto; }
        public void setTexto(String texto) { this.texto = texto; }
    }
    
    // ===============================
    // === MAIN & SERVER ============
    // ===============================
    
    public static void main(String[] args) {
        // Set SSL system properties
        System.setProperty("javax.net.ssl.keyStore", KEYSTORE_PATH);
        System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
        
        initDatabaseData();
        startDatabaseSyncThread();
        // Start only the secure WebSocket server (WSS)
        startSecureServer();
    }
    
    private static void startSecureServer() {
        try {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            try (SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(PORT)) {
                System.out.println("Secure WebSocket Server running on wss://localhost:" + PORT);
                while (true) {
                    SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void handleClient(SSLSocket socket) {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            // 1) Read HTTP handshake
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String key = null;
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                if (line.toLowerCase().startsWith("sec-websocket-key:")) {
                    key = line.split(":")[1].trim();
                }
            }
            if (key == null) {
                socket.close();
                return;
            }

            // 2) Send handshake response
            String acceptKey = createAcceptKey(key);
            String resp = "HTTP/1.1 101 Switching Protocols\r\n" +
                          "Upgrade: websocket\r\n" +
                          "Connection: Upgrade\r\n" +
                          "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
            out.write(resp.getBytes("UTF-8"));
            out.flush();

            // 3) Read and process frames
            while (!socket.isClosed()) {
                String msg = readTextFrame(in);
                if (msg == null) {
                    break;
                }
                System.out.println("Client says: " + msg);
                String response = handleCommand(msg);
                writeTextFrame(response, out);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException ignore) {}
        }
    }
    
    // ===============================
    // === COMMAND DISPATCHER ========
    // ===============================
    
    private static String handleCommand(String msg) {
        // NEW: LMS Commands
        if (msg.startsWith("loginStudent:"))        return handleLoginStudent(msg);
        if (msg.startsWith("loginTeacher:"))        return handleLoginTeacher(msg);
        if (msg.startsWith("myCoursesStudent:"))     return handleMyCoursesStudent(msg);
        if (msg.startsWith("myAsignaturasStudent:")) return handleMyAsignaturasStudent(msg);
        if (msg.startsWith("myAsignaturasTeacher:")) return handleMyAsignaturasTeacher(msg);

        // Existing commands
        if (msg.startsWith("createStudent:"))    return handleCreateStudent(msg);
        if (msg.equals("listStudents"))          return listEstudiantes();
        if (msg.startsWith("createProfesor:"))   return handleCreateProfesor(msg);
        if (msg.equals("listProfesores"))        return listProfesores();
        if (msg.startsWith("createCurso:"))      return handleCreateCurso(msg);
        if (msg.equals("listCursos"))            return listCursos();
        if (msg.startsWith("createAsignatura:")) return handleCreateAsignatura(msg);
        if (msg.equals("listAsignaturas"))       return listAsignaturas();
        if (msg.startsWith("enrollEstudiante:")) return handleEnrollEstudiante(msg);
        if (msg.startsWith("enrollProfesor:"))   return handleEnrollProfesor(msg);
        if (msg.startsWith("createTema:"))       return handleCreateTema(msg);
        if (msg.startsWith("listTemas:"))        return handleListTemas(msg);
        if (msg.startsWith("createContenido:"))  return handleCreateContenido(msg);
        if (msg.startsWith("listContenidos:"))   return handleListContenidos(msg);

        // Update / Delete
        if (msg.startsWith("updateEstudiante:")) return handleUpdateEstudiante(msg);
        if (msg.startsWith("deleteEstudiante:")) return handleDeleteEstudiante(msg);
        if (msg.startsWith("updateProfesor:"))   return handleUpdateProfesor(msg);
        if (msg.startsWith("deleteProfesor:"))   return handleDeleteProfesor(msg);
        if (msg.startsWith("updateCurso:"))      return handleUpdateCurso(msg);
        if (msg.startsWith("deleteCurso:"))      return handleDeleteCurso(msg);
        if (msg.startsWith("updateAsignatura:")) return handleUpdateAsignatura(msg);
        if (msg.startsWith("deleteAsignatura:")) return handleDeleteAsignatura(msg);
        if (msg.startsWith("updateTema:"))       return handleUpdateTema(msg);
        if (msg.startsWith("deleteTema:"))       return handleDeleteTema(msg);
        if (msg.startsWith("updateContenido:"))  return handleUpdateContenido(msg);
        if (msg.startsWith("deleteContenido:"))  return handleDeleteContenido(msg);
        // Admin login
        if (msg.startsWith("loginAdmin:")) {
            return handleLoginAdmin(msg);
        }
        return "ERROR: Unknown command";
    }
    
    // --- LOGIN & "MY" QUERIES ---
    
    private static String handleLoginAdmin(String msg) {
        String data = msg.substring("loginAdmin:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 2) {
            return "ERROR: Use: loginAdmin: username,password";
        }
        String username = parts[0].trim();
        String password = parts[1].trim();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            try (PreparedStatement ps = conn.prepareStatement(
                 "SELECT nombre FROM administradores WHERE username = ? AND password = ?")) {
                ps.setString(1, username);
                ps.setString(2, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return "OK: Admin " + rs.getString("nombre");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "ERROR: Invalid admin credentials.";
    }
    
    private static String handleLoginStudent(String msg) {
        String data = msg.substring("loginStudent:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 2) {
            return "ERROR: Use: loginStudent: username,password";
        }
        String username = parts[0].trim();
        String password = parts[1].trim();

        for (Estudiante e : estudiantes) {
            if (e.getUsername() != null &&
                e.getUsername().equalsIgnoreCase(username) &&
                e.getPassword() != null &&
                e.getPassword().equals(password)) {
                return "OK: Student " + e.getNombre();
            }
        }
        return "ERROR: Invalid student credentials.";
    }
    
    private static String handleLoginTeacher(String msg) {
        String data = msg.substring("loginTeacher:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 2) {
            return "ERROR: Use: loginTeacher: username,password";
        }
        String username = parts[0].trim();
        String password = parts[1].trim();

        for (Profesor p : profesores) {
            if (p.getUsername() != null &&
                p.getUsername().equalsIgnoreCase(username) &&
                p.getPassword() != null &&
                p.getPassword().equals(password)) {
                return "OK: Teacher " + p.getNombre();
            }
        }
        return "ERROR: Invalid teacher credentials.";
    }
    
    private static String handleMyCoursesStudent(String msg) {
        String username = msg.substring("myCoursesStudent:".length()).trim();
        Estudiante stu = findStudentByUsername(username);
        if (stu == null) {
            return "ERROR: Student with username='" + username + "' not found.";
        }
        Set<Curso> courseSet = new HashSet<>();
        for (Asignatura a : asignaturas) {
            if (a.getEstudiantes().contains(stu)) {
                courseSet.add(a.getCurso());
            }
        }
        if (courseSet.isEmpty()) {
            return "No courses found for student username='" + username + "'";
        }
        StringBuilder sb = new StringBuilder("MyCourses:\n");
        for (Curso c : courseSet) {
            sb.append(" - ").append(c.getNombre())
              .append(" (Dur: ").append(c.getDuracionEnHoras()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleMyAsignaturasStudent(String msg) {
        String username = msg.substring("myAsignaturasStudent:".length()).trim();
        Estudiante stu = findStudentByUsername(username);
        if (stu == null) {
            return "ERROR: Student with username='" + username + "' not found.";
        }
        List<Asignatura> result = new ArrayList<>();
        for (Asignatura a : asignaturas) {
            if (a.getEstudiantes().contains(stu)) {
                result.add(a);
            }
        }
        if (result.isEmpty()) {
            return "No asignaturas found for student username='" + username + "'";
        }
        StringBuilder sb = new StringBuilder("MyAsignaturas (Student):\n");
        for (Asignatura a : result) {
            sb.append(" - ").append(a.getNombre())
              .append(" (Curso: ").append(a.getCurso().getNombre()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleMyAsignaturasTeacher(String msg) {
        String username = msg.substring("myAsignaturasTeacher:".length()).trim();
        Profesor prof = findTeacherByUsername(username);
        if (prof == null) {
            return "ERROR: Teacher with username='" + username + "' not found.";
        }
        List<Asignatura> result = new ArrayList<>();
        for (Asignatura a : asignaturas) {
            if (a.getProfesores().contains(prof)) {
                result.add(a);
            }
        }
        if (result.isEmpty()) {
            return "No asignaturas found for teacher username='" + username + "'";
        }
        StringBuilder sb = new StringBuilder("MyAsignaturas (Teacher):\n");
        for (Asignatura a : result) {
            sb.append(" - ").append(a.getNombre())
              .append(" (Curso: ").append(a.getCurso().getNombre()).append(")\n");
        }
        return sb.toString();
    }
    
    // --- CREATE / LIST COMMANDS ---
    
    private static String handleCreateStudent(String msg) {
        String data = msg.substring("createStudent:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            try {
                String name = parts[0].trim();
                int age = Integer.parseInt(parts[1].trim());
                estudiantes.add(new Estudiante(name, age));
                return "Estudiante created: " + name;
            } catch (NumberFormatException e) {
                return "ERROR: Age must be integer.";
            }
        } else if (parts.length == 4) {
            try {
                String name = parts[0].trim();
                int age = Integer.parseInt(parts[1].trim());
                String user = parts[2].trim();
                String pass = parts[3].trim();
                estudiantes.add(new Estudiante(name, age, user, pass));
                return "Estudiante created with username='" + user + "'";
            } catch (NumberFormatException e) {
                return "ERROR: Age must be integer.";
            }
        }
        return "ERROR: Use createStudent: name,age [or name,age,username,password]";
    }
    
    private static String listEstudiantes() {
        if (estudiantes.isEmpty()) return "No estudiantes found.";
        StringBuilder sb = new StringBuilder("Estudiantes:\n");
        for (Estudiante e : estudiantes) {
            sb.append(" - ").append(e.getNombre())
              .append(" (Edad: ").append(e.getEdad()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleCreateProfesor(String msg) {
        String data = msg.substring("createProfesor:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String name = parts[0].trim();
            String esp = parts[1].trim();
            profesores.add(new Profesor(name, esp));
            return "Profesor created: " + name;
        } else if (parts.length == 4) {
            String name = parts[0].trim();
            String esp = parts[1].trim();
            String user = parts[2].trim();
            String pass = parts[3].trim();
            profesores.add(new Profesor(name, esp, user, pass));
            return "Profesor created with username='" + user + "'";
        }
        return "ERROR: Use createProfesor: name,especialidad[,username,password]";
    }
    
    private static String listProfesores() {
        if (profesores.isEmpty()) return "No profesores found.";
        StringBuilder sb = new StringBuilder("Profesores:\n");
        for (Profesor p : profesores) {
            sb.append(" - ").append(p.getNombre())
              .append(" (Especialidad: ").append(p.getEspecialidad()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleCreateCurso(String msg) {
        String data = msg.substring("createCurso:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            try {
                String name = parts[0].trim();
                int dur = Integer.parseInt(parts[1].trim());
                cursos.add(new Curso(name, dur));
                return "Curso created: " + name;
            } catch (NumberFormatException e) {
                return "ERROR: Duracion must be integer.";
            }
        }
        return "ERROR: Use: createCurso: name,duracion";
    }
    
    private static String listCursos() {
        if (cursos.isEmpty()) return "No cursos found.";
        StringBuilder sb = new StringBuilder("Cursos:\n");
        for (Curso c : cursos) {
            sb.append(" - ").append(c.getNombre())
              .append(" (Duración: ").append(c.getDuracionEnHoras()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleCreateAsignatura(String msg) {
        String data = msg.substring("createAsignatura:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String asigName = parts[0].trim();
            String cursoName = parts[1].trim();
            Curso c = findCursoByName(cursoName);
            if (c == null) {
                return "ERROR: Curso not found: " + cursoName;
            }
            asignaturas.add(new Asignatura(asigName, c));
            return "Asignatura created: " + asigName;
        }
        return "ERROR: Use: createAsignatura: asigName,cursoName";
    }
    
    private static String listAsignaturas() {
        if (asignaturas.isEmpty()) return "No asignaturas found.";
        StringBuilder sb = new StringBuilder("Asignaturas:\n");
        for (Asignatura a : asignaturas) {
            sb.append(" - ").append(a.getNombre())
              .append(" (Curso: ").append(a.getCurso().getNombre()).append(")\n");
        }
        return sb.toString();
    }
    
    private static String handleEnrollEstudiante(String msg) {
        String data = msg.substring("enrollEstudiante:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String asigN = parts[0].trim();
            String estN  = parts[1].trim();
            Asignatura a = findAsignaturaByName(asigN);
            if (a == null) return "ERROR: Asignatura not found: " + asigN;
            Estudiante e = findEstudianteByName(estN);
            if (e == null) return "ERROR: Estudiante not found: " + estN;
            a.addEstudiante(e);
            return "Estudiante " + estN + " enrolled in " + asigN;
        }
        return "ERROR: Use: enrollEstudiante: asigName,estName";
    }
    
    private static String handleEnrollProfesor(String msg) {
        String data = msg.substring("enrollProfesor:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String asigN = parts[0].trim();
            String profN = parts[1].trim();
            Asignatura a = findAsignaturaByName(asigN);
            if (a == null) return "ERROR: Asignatura not found: " + asigN;
            Profesor p = findProfesorByName(profN);
            if (p == null) return "ERROR: Profesor not found: " + profN;
            a.addProfesor(p);
            return "Profesor " + profN + " enrolled in " + asigN;
        }
        return "ERROR: Use: enrollProfesor: asigName,profName";
    }
    
    private static String handleCreateTema(String msg) {
        String data = msg.substring("createTema:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length == 2) {
            String asigN = parts[0].trim();
            String temaN = parts[1].trim();
            Asignatura asig = findAsignaturaByName(asigN);
            if (asig == null) return "ERROR: Asignatura not found: " + asigN;
            Tema t = new Tema(temaN);
            asig.addTema(t);
            return "Tema created: " + temaN + " in Asignatura: " + asigN;
        }
        return "ERROR: Use: createTema: asigName,temaName";
    }
    
    private static String handleListTemas(String msg) {
        String asigN = msg.substring("listTemas:".length()).trim();
        if (asigN.isEmpty()) return "ERROR: Must specify Asignatura name";
        Asignatura a = findAsignaturaByName(asigN);
        if (a == null) return "ERROR: Asignatura not found: " + asigN;
        if (a.getTemas().isEmpty()) return "No temas found for Asignatura '" + asigN + "'";
        StringBuilder sb = new StringBuilder("Temas in Asignatura '").append(asigN).append("':\n");
        for (Tema t : a.getTemas()) {
            sb.append(" - ").append(t.getNombre()).append("\n");
        }
        return sb.toString();
    }
    
    private static String handleCreateContenido(String msg) {
        String data = msg.substring("createContenido:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length < 4) {
            return "ERROR: Use: createContenido: asigName,temaName,titulo,texto";
        }
        String asigN = parts[0].trim();
        String temaN = parts[1].trim();
        String titulo = parts[2].trim();
        StringBuilder sb = new StringBuilder();
        if (parts.length > 4) {
            sb.append(parts[3].trim());
            for (int i = 4; i < parts.length; i++) {
                sb.append(",").append(parts[i].trim());
            }
        } else {
            sb.append(parts[3].trim());
        }
        String texto = sb.toString();
        Asignatura asig = findAsignaturaByName(asigN);
        if (asig == null) return "ERROR: Asignatura not found: " + asigN;
        Tema t = findTemaByName(asig, temaN);
        if (t == null) return "ERROR: Tema not found: " + temaN;
        t.getContenidos().add(new Contenido(titulo, texto));
        return "Contenido created: " + titulo;
    }
    
    private static String handleListContenidos(String msg) {
        String data = msg.substring("listContenidos:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 2) return "ERROR: Use: listContenidos: asigName,temaName";
        String asigN = parts[0].trim();
        String temaN = parts[1].trim();
        Asignatura a = findAsignaturaByName(asigN);
        if (a == null) return "ERROR: Asignatura not found: " + asigN;
        Tema t = findTemaByName(a, temaN);
        if (t == null) return "ERROR: Tema not found: " + temaN;
        if (t.getContenidos().isEmpty()) return "No contenidos found for Tema '" + temaN + "'";
        StringBuilder sb = new StringBuilder("Contenidos in Tema '")
            .append(temaN).append("' of Asignatura '")
            .append(asigN).append("':\n");
        for (Contenido c : t.getContenidos()) {
            sb.append(" - [").append(c.getTitulo()).append("] ")
              .append(c.getTexto()).append("\n");
        }
        return sb.toString();
    }
    
    // --- UPDATE & DELETE COMMANDS ---
    
    private static String handleUpdateEstudiante(String msg) {
        String data = msg.substring("updateEstudiante:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) return "ERROR: Use: updateEstudiante: oldName,newName,newAge";
        String oldName = parts[0].trim();
        String newName = parts[1].trim();
        int newAge;
        try { newAge = Integer.parseInt(parts[2].trim()); }
        catch (NumberFormatException e) { return "ERROR: Age must be integer."; }
        Estudiante e = findEstudianteByName(oldName);
        if (e == null) return "ERROR: Estudiante not found: " + oldName;
        e.setNombre(newName);
        e.setEdad(newAge);
        return "Estudiante updated: " + oldName + " -> " + newName;
    }
    
    private static String handleDeleteEstudiante(String msg) {
        String name = msg.substring("deleteEstudiante:".length()).trim();
        Estudiante e = findEstudianteByName(name);
        if (e == null) return "ERROR: Estudiante not found: " + name;
        estudiantes.remove(e);
        return "Estudiante deleted: " + name;
    }
    
    private static String handleUpdateProfesor(String msg) {
        String data = msg.substring("updateProfesor:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) return "ERROR: Use: updateProfesor: oldName,newName,newEsp";
        String oldName = parts[0].trim();
        String newName = parts[1].trim();
        String newEsp = parts[2].trim();
        Profesor p = findProfesorByName(oldName);
        if (p == null) return "ERROR: Profesor not found: " + oldName;
        p.setNombre(newName);
        p.setEspecialidad(newEsp);
        return "Profesor updated: " + oldName + " -> " + newName;
    }
    
    private static String handleDeleteProfesor(String msg) {
        String name = msg.substring("deleteProfesor:".length()).trim();
        Profesor p = findProfesorByName(name);
        if (p == null) return "ERROR: Profesor not found: " + name;
        profesores.remove(p);
        return "Profesor deleted: " + name;
    }
    
    private static String handleUpdateCurso(String msg) {
        String data = msg.substring("updateCurso:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) return "ERROR: Use: updateCurso: oldName,newName,newDur";
        String oldName = parts[0].trim();
        String newName = parts[1].trim();
        int newDur;
        try { newDur = Integer.parseInt(parts[2].trim()); }
        catch (NumberFormatException e) { return "ERROR: Dur must be integer."; }
        Curso c = findCursoByName(oldName);
        if (c == null) return "ERROR: Curso not found: " + oldName;
        c.setNombre(newName);
        c.setDuracionEnHoras(newDur);
        return "Curso updated: " + oldName + " -> " + newName;
    }
    
    private static String handleDeleteCurso(String msg) {
        String name = msg.substring("deleteCurso:".length()).trim();
        Curso c = findCursoByName(name);
        if (c == null) return "ERROR: Curso not found: " + name;
        cursos.remove(c);
        return "Curso deleted: " + name;
    }
    
    private static String handleUpdateAsignatura(String msg) {
        String data = msg.substring("updateAsignatura:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) return "ERROR: Use: updateAsignatura: oldAsig,newAsig,newCurso";
        String oldAsig = parts[0].trim();
        String newAsig = parts[1].trim();
        String newCurso = parts[2].trim();
        Asignatura a = findAsignaturaByName(oldAsig);
        if (a == null) return "ERROR: Asignatura not found: " + oldAsig;
        Curso c = findCursoByName(newCurso);
        if (c == null) return "ERROR: Curso not found: " + newCurso;
        a.setNombre(newAsig);
        a.setCurso(c);
        return "Asignatura updated: " + oldAsig + " -> " + newAsig;
    }
    
    private static String handleDeleteAsignatura(String msg) {
        String name = msg.substring("deleteAsignatura:".length()).trim();
        Asignatura a = findAsignaturaByName(name);
        if (a == null) return "ERROR: Asignatura not found: " + name;
        asignaturas.remove(a);
        return "Asignatura deleted: " + name;
    }
    
    private static String handleUpdateTema(String msg) {
        String data = msg.substring("updateTema:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) return "ERROR: Use: updateTema: asigName,oldTema,newTema";
        String asigName = parts[0].trim();
        String oldTema = parts[1].trim();
        String newTema = parts[2].trim();
        Asignatura a = findAsignaturaByName(asigName);
        if (a == null) return "ERROR: Asignatura not found: " + asigName;
        Tema t = findTemaByName(a, oldTema);
        if (t == null) return "ERROR: Tema not found: " + oldTema;
        t.setNombre(newTema);
        return "Tema updated: " + oldTema + " -> " + newTema;
    }
    
    private static String handleDeleteTema(String msg) {
        String data = msg.substring("deleteTema:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 2) return "ERROR: Use: deleteTema: asigName,temaName";
        String asigName = parts[0].trim();
        String temaName = parts[1].trim();
        Asignatura a = findAsignaturaByName(asigName);
        if (a == null) return "ERROR: Asignatura not found: " + asigName;
        Tema t = findTemaByName(a, temaName);
        if (t == null) return "ERROR: Tema not found: " + temaName;
        a.getTemas().remove(t);
        return "Tema deleted: " + temaName;
    }
    
    private static String handleUpdateContenido(String msg) {
        String data = msg.substring("updateContenido:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length < 5) {
            return "ERROR: Use: updateContenido: asigName,temaName,oldTitulo,newTitulo,newTexto";
        }
        String asigN = parts[0].trim();
        String temaN = parts[1].trim();
        String oldTit = parts[2].trim();
        String newTit = parts[3].trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 4; i < parts.length; i++) {
            if (sb.length() > 0) sb.append(",");
            sb.append(parts[i].trim());
        }
        String newTexto = sb.toString();
        Asignatura a = findAsignaturaByName(asigN);
        if (a == null) return "ERROR: Asignatura not found: " + asigN;
        Tema t = findTemaByName(a, temaN);
        if (t == null) return "ERROR: Tema not found: " + temaN;
        Contenido c = findContenidoByTitulo(t, oldTit);
        if (c == null) return "ERROR: Contenido not found: " + oldTit;
        c.setTitulo(newTit);
        c.setTexto(newTexto);
        return "Contenido updated: " + oldTit + " -> " + newTit;
    }
    
    private static String handleDeleteContenido(String msg) {
        String data = msg.substring("deleteContenido:".length()).trim();
        String[] parts = data.split(",");
        if (parts.length != 3) {
            return "ERROR: Use: deleteContenido: asigName,temaName,titulo";
        }
        String asigN = parts[0].trim();
        String temaN = parts[1].trim();
        String titulo = parts[2].trim();
        Asignatura a = findAsignaturaByName(asigN);
        if (a == null) return "ERROR: Asignatura not found: " + asigN;
        Tema t = findTemaByName(a, temaN);
        if (t == null) return "ERROR: Tema not found: " + temaN;
        Contenido c = findContenidoByTitulo(t, titulo);
        if (c == null) return "ERROR: Contenido not found: " + titulo;
        t.getContenidos().remove(c);
        return "Contenido deleted: " + titulo;
    }
    
    // --- HELPER FINDER METHODS ---
    
    private static Estudiante findStudentByUsername(String username) {
        for (Estudiante e : estudiantes) {
            if (e.getUsername() != null && e.getUsername().equalsIgnoreCase(username)) {
                return e;
            }
        }
        return null;
    }
    
    private static Profesor findTeacherByUsername(String username) {
        for (Profesor p : profesores) {
            if (p.getUsername() != null && p.getUsername().equalsIgnoreCase(username)) {
                return p;
            }
        }
        return null;
    }
    
    private static Estudiante findEstudianteByName(String name) {
        for (Estudiante e : estudiantes) {
            if (e.getNombre().equalsIgnoreCase(name)) return e;
        }
        return null;
    }
    
    private static Profesor findProfesorByName(String name) {
        for (Profesor p : profesores) {
            if (p.getNombre().equalsIgnoreCase(name)) return p;
        }
        return null;
    }
    
    private static Curso findCursoByName(String name) {
        for (Curso c : cursos) {
            if (c.getNombre().equalsIgnoreCase(name)) return c;
        }
        return null;
    }
    
    private static Asignatura findAsignaturaByName(String name) {
        for (Asignatura a : asignaturas) {
            if (a.getNombre().equalsIgnoreCase(name)) return a;
        }
        return null;
    }
    
    private static Tema findTemaByName(Asignatura asig, String temaName) {
        for (Tema t : asig.getTemas()) {
            if (t.getNombre().equalsIgnoreCase(temaName)) return t;
        }
        return null;
    }
    
    private static Contenido findContenidoByTitulo(Tema t, String titulo) {
        for (Contenido c : t.getContenidos()) {
            if (c.getTitulo().equalsIgnoreCase(titulo)) return c;
        }
        return null;
    }
    
    // --- WEBSOCKET FRAMING METHODS ---
    
    private static String createAcceptKey(String clientKey) {
        try {
            String magicGUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            sha1.update((clientKey + magicGUID).getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(sha1.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String readTextFrame(InputStream in) throws IOException {
        int b1 = in.read();
        if (b1 == -1) return null;
        int b2 = in.read();
        if (b2 == -1) return null;
        boolean masked = (b2 & 0x80) != 0;
        long payloadLen = (b2 & 0x7F);
        if (payloadLen == 126) {
            byte[] extended = new byte[2];
            if (in.read(extended) < 2) return null;
            payloadLen = ((extended[0] & 0xFF) << 8) | (extended[1] & 0xFF);
        } else if (payloadLen == 127) {
            byte[] extended = new byte[8];
            if (in.read(extended) < 8) return null;
            payloadLen = 0;
            for (int i = 0; i < 8; i++) {
                payloadLen = (payloadLen << 8) | (extended[i] & 0xFF);
            }
        }
        byte[] maskKey = new byte[4];
        if (masked) {
            if (in.read(maskKey) < 4) return null;
        }
        byte[] payloadData = new byte[(int) payloadLen];
        int totalRead = 0;
        while (totalRead < payloadLen) {
            int r = in.read(payloadData, totalRead, (int)(payloadLen - totalRead));
            if (r == -1) return null;
            totalRead += r;
        }
        if (masked) {
            for (int i = 0; i < payloadLen; i++) {
                payloadData[i] = (byte) (payloadData[i] ^ maskKey[i % 4]);
            }
        }
        int opcode = (b1 & 0x0F);
        if (opcode == 8) {
            return null; // close frame
        } else if (opcode == 1) {
            return new String(payloadData, "UTF-8");
        }
        return null;
    }
    
    private static void writeTextFrame(String message, OutputStream out) throws IOException {
        byte[] payload = message.getBytes("UTF-8");
        out.write((byte)(0x80 | 0x1)); // FIN + text opcode
        if (payload.length <= 125) {
            out.write(payload.length);
        } else if (payload.length <= 65535) {
            out.write(126);
            out.write((payload.length >> 8) & 0xFF);
            out.write(payload.length & 0xFF);
        } else {
            out.write(127);
            long len = payload.length;
            out.write((int)((len >> 56) & 0xFF));
            out.write((int)((len >> 48) & 0xFF));
            out.write((int)((len >> 40) & 0xFF));
            out.write((int)((len >> 32) & 0xFF));
            out.write((int)((len >> 24) & 0xFF));
            out.write((int)((len >> 16) & 0xFF));
            out.write((int)((len >> 8) & 0xFF));
            out.write((int)(len & 0xFF));
        }
        out.write(payload);
        out.flush();
    }
    
    // --- DATABASE INITIALIZATION & SYNC ---
    
    private static void initDatabaseData() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            createTablesIfNeeded(conn);
            // Load Estudiantes
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre, edad, username, password FROM estudiantes")) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    int edad = rs.getInt("edad");
                    String user = rs.getString("username");
                    String pass = rs.getString("password");
                    estudiantes.add(new Estudiante(nombre, edad, user, pass));
                }
            }
            // Load Profesores
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre, especialidad, username, password FROM profesores")) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    String esp = rs.getString("especialidad");
                    String user = rs.getString("username");
                    String pass = rs.getString("password");
                    profesores.add(new Profesor(nombre, esp, user, pass));
                }
            }
            // Load Cursos
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT nombre, duracion FROM cursos")) {
                while (rs.next()) {
                    String cName = rs.getString("nombre");
                    int dur = rs.getInt("duracion");
                    cursos.add(new Curso(cName, dur));
                }
            }
            System.out.println("Loaded from DB: " 
                + estudiantes.size() + " estudiantes, "
                + profesores.size()  + " profesores, "
                + cursos.size()      + " cursos.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void startDatabaseSyncThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    syncDatabase();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
    
    private static void syncDatabase() {
        System.out.println("Syncing data to DB...");
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            dropTables(conn);
            createTablesIfNeeded(conn);
            insertAllData(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private static void createTablesIfNeeded(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS estudiantes ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100), "
                           + "edad INT, "
                           + "username VARCHAR(100) UNIQUE, "
                           + "password VARCHAR(100))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS profesores ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100), "
                           + "especialidad VARCHAR(100), "
                           + "username VARCHAR(100) UNIQUE, "
                           + "password VARCHAR(100))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS cursos ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100) UNIQUE, "
                           + "duracion INT)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS asignaturas ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100) UNIQUE, "
                           + "curso_id INT, "
                           + "FOREIGN KEY (curso_id) REFERENCES cursos(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS temas ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100), "
                           + "asignatura_id INT, "
                           + "FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS contenidos ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "titulo VARCHAR(100), "
                           + "texto TEXT, "
                           + "tema_id INT, "
                           + "FOREIGN KEY (tema_id) REFERENCES temas(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS estudiante_asignatura ("
                           + "estudiante_id INT, "
                           + "asignatura_id INT, "
                           + "PRIMARY KEY (estudiante_id, asignatura_id), "
                           + "FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id), "
                           + "FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS profesor_asignatura ("
                           + "profesor_id INT, "
                           + "asignatura_id INT, "
                           + "PRIMARY KEY (profesor_id, asignatura_id), "
                           + "FOREIGN KEY (profesor_id) REFERENCES profesores(id), "
                           + "FOREIGN KEY (asignatura_id) REFERENCES asignaturas(id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS administradores ("
                           + "id INT AUTO_INCREMENT PRIMARY KEY, "
                           + "nombre VARCHAR(100), "
                           + "email VARCHAR(100), "
                           + "username VARCHAR(100) UNIQUE, "
                           + "password VARCHAR(100))");
            try (PreparedStatement checkPs = conn.prepareStatement(
                 "SELECT COUNT(*) FROM administradores WHERE username = ?")) {
                checkPs.setString(1, "jocarsa");
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO administradores (nombre, email, username, password) VALUES (?,?,?,?)")) {
                            ps.setString(1, "Jose Vicente Carratala");
                            ps.setString(2, "info@josevicentecarratala");
                            ps.setString(3, "jocarsa");
                            ps.setString(4, "jocarsa");
                            ps.executeUpdate();
                        }
                    }
                }
            }
        }
    }
    
    private static void dropTables(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("DROP TABLE IF EXISTS estudiante_asignatura");
            st.executeUpdate("DROP TABLE IF EXISTS profesor_asignatura");
            st.executeUpdate("DROP TABLE IF EXISTS contenidos");
            st.executeUpdate("DROP TABLE IF EXISTS temas");
            st.executeUpdate("DROP TABLE IF EXISTS asignaturas");
            st.executeUpdate("DROP TABLE IF EXISTS estudiantes");
            st.executeUpdate("DROP TABLE IF EXISTS profesores");
            st.executeUpdate("DROP TABLE IF EXISTS cursos");
        }
    }
    
    private static void insertAllData(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO estudiantes (nombre, edad, username, password) VALUES (?,?,?,?)")) {
            for (Estudiante e : estudiantes) {
                ps.setString(1, e.getNombre());
                ps.setInt(2, e.getEdad());
                ps.setString(3, e.getUsername());
                ps.setString(4, e.getPassword());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO profesores (nombre, especialidad, username, password) VALUES (?,?,?,?)")) {
            for (Profesor p : profesores) {
                ps.setString(1, p.getNombre());
                ps.setString(2, p.getEspecialidad());
                ps.setString(3, p.getUsername());
                ps.setString(4, p.getPassword());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO cursos (nombre, duracion) VALUES (?,?)")) {
            for (Curso c : cursos) {
                ps.setString(1, c.getNombre());
                ps.setInt(2, c.getDuracionEnHoras());
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO asignaturas (nombre, curso_id) VALUES (?,?)")) {
            for (Asignatura a : asignaturas) {
                ps.setString(1, a.getNombre());
                // Note: Here you would normally lookup the actual course id.
                ps.setInt(2, 0); // Placeholder
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO temas (nombre, asignatura_id) VALUES (?,?)")) {
            for (Asignatura a : asignaturas) {
                for (Tema t : a.getTemas()) {
                    ps.setString(1, t.getNombre());
                    ps.setInt(2, 0); // Placeholder
                    ps.executeUpdate();
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO contenidos (titulo, texto, tema_id) VALUES (?,?,?)")) {
            for (Asignatura a : asignaturas) {
                for (Tema t : a.getTemas()) {
                    for (Contenido c : t.getContenidos()) {
                        ps.setString(1, c.getTitulo());
                        ps.setString(2, c.getTexto());
                        ps.setInt(3, 0); // Placeholder
                        ps.executeUpdate();
                    }
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO estudiante_asignatura (estudiante_id, asignatura_id) VALUES (?,?)")) {
            for (Asignatura a : asignaturas) {
                for (Estudiante e : a.getEstudiantes()) {
                    ps.setInt(1, 0); // Placeholder
                    ps.setInt(2, 0); // Placeholder
                    ps.executeUpdate();
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(
             "INSERT INTO profesor_asignatura (profesor_id, asignatura_id) VALUES (?,?)")) {
            for (Asignatura a : asignaturas) {
                for (Profesor p : a.getProfesores()) {
                    ps.setInt(1, 0); // Placeholder
                    ps.setInt(2, 0); // Placeholder
                    ps.executeUpdate();
                }
            }
        }
    }
}
