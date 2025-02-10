package j039.supercontrolador;

// Autor Jose Vicente Carratala
public class J039SuperControlador{
    public static void main(String[] args) {
        String usuario = "pedro";
      
        GestionBD conexion = new GestionBD(usuario);
        conexion.insertaDato("clientes","NULL,'aaa','bbb','ccc'");
       
    }
}
