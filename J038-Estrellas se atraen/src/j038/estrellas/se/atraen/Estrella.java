package j038.estrellas.se.atraen;

// Autor Jose Vicente Carratalá Sanchis

public class Estrella {
    double x;
    double y;
    double angulo;
    double velocidad;
    double masa;
    int anchura;
    int altura;
    double lastX;
    double lastY;
    
    public Estrella(int nuevax, int nuevay, int nuevoangulo, int nuevavelocidad, int nuevaanchura, int nuevaaltura, double masa) {
        x = nuevax;
        y = nuevay;
        angulo = nuevoangulo;
        velocidad = nuevavelocidad / 10.0;
        anchura = nuevaanchura;
        altura = nuevaaltura;
        this.masa = masa * 100;
        lastX = x;
        lastY = y;
    }

    public void actualizaPosicion(double fuerzaX, double fuerzaY) {
        lastX = x;
        lastY = y;
        
        velocidad += Math.sqrt(fuerzaX * fuerzaX + fuerzaY * fuerzaY) / 100;
        angulo = Math.atan2(fuerzaY, fuerzaX);
        x += Math.cos(angulo) * velocidad;
        y += Math.sin(angulo) * velocidad;
    }
    
    public void calculaFuerza(Estrella otraEstrella, double[] fuerzas) {
        double G = 6.67430e-10;
        double dx = otraEstrella.x - this.x;
        double dy = otraEstrella.y - this.y;
        double distancia = Math.sqrt(dx * dx + dy * dy);
        
        if (distancia > 10) {
            double fuerza = G * this.masa * otraEstrella.masa / (distancia * distancia);
            double fuerzaX = fuerza * dx / distancia;
            double fuerzaY = fuerza * dy / distancia;
            
            fuerzas[0] += fuerzaX;
            fuerzas[1] += fuerzaY;
        }
    }

    public void rebotaBordes() {
        if (x > anchura) x = 0;
        if (x < 0) x = anchura;
        if (y > altura) y = 0;
        if (y < 0) y = altura;
    }

    public double[] getPosicion() {
        return new double[] { x, y };
    }
    
    public double[] getPosicionAnterior() {
        return new double[] { lastX, lastY };
    }
}
