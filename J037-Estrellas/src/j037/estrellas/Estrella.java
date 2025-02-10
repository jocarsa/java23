package j037.estrellas;

// Autor Jose Vicente Carratalá Sanchis

public class Estrella {
    double x;
    double y;
    double angulo;
    double velocidad;
    int anchura;
    int altura;
    
    public  Estrella(int nuevax, int nuevay,int nuevoangulo ,int nuevavelocidad, int nuevaanchura, int nuevaaltura){
        x = nuevax;
        y = nuevay;
        angulo = nuevoangulo;
        velocidad = nuevavelocidad;
        anchura = nuevaanchura;
        altura = nuevaaltura;
    }
    public void actualizaPosicion(){
        x += Math.cos(angulo)*velocidad;
        y += Math.sin(angulo)*velocidad;
    }
    public void rebotaBordes(){
        if(
            x > anchura
            || x <0 
            || y > altura
            || y < 0
            ){
            angulo += 180;
        }
    }
    public double[] getPosicion() {
        return new double[] {x, y};
    }
}
