package j037.estrellas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.imageio.ImageIO;
// Autor Jose Vicente Carratala
public class J037Estrellas extends JFrame {
    private static final int width = 1920;
    private static final int height = 1080;
    private static final int numeroestrellas = 100;
    private Estrella[] estrellas;

    public J037Estrellas() {
        setTitle("Estrellas en Movimiento");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize stars with random positions, angles, and speeds
        estrellas = new Estrella[numeroestrellas];
        for (int i = 0; i < numeroestrellas; i++) {
            int x = (int) (Math.random() * width);
            int y = (int) (Math.random() * height);
            int angulo = (int) (Math.random() * 360);  // Angle between 0 and 360
            int velocidad = (int) (Math.random() * 5 + 1);  // Random speed between 1 and 5
            estrellas[i] = new Estrella(x, y, angulo, velocidad,width,height);
        }

        // Create a JPanel to handle the drawing
        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Cast Graphics to Graphics2D
                Graphics2D g2d = (Graphics2D) g;

                // Fill background with black
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, width, height);

                // Update and draw each star
                g2d.setColor(Color.WHITE);
                for (int i = 0; i < numeroestrellas; i++) {
                    // Update position
                    estrellas[i].rebotaBordes();
                    estrellas[i].actualizaPosicion();
  
                    double[] posicion = estrellas[i].getPosicion();
                    g2d.fillRect((int) posicion[0], (int) posicion[1], 2, 2);
                }
            }
        };

        add(panel);
        setVisible(true);

        // Start animation in a new thread
        new Thread(() -> {
            while (true) {
                panel.repaint();  // Trigger repaint
                try {
                    Thread.sleep(16);  // 60 FPS (approx.)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new J037Estrellas();
    }
}