package j038.estrellas.se.atraen;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.*;
import j038.estrellas.se.atraen.Estrella;
import java.awt.image.BufferedImage;

// Autor Jose Vicente Carratala
public class J038EstrellasSeAtraen extends JFrame {
    private static final int width = 1920;
    private static final int height = 1080;
    private static final int numeroestrellas = 50000; // Reduce number for better performance in cluster simulation
    private Estrella[] estrellas;
    private Image buffer;
    private Graphics2D bufferGraphics;

    public J038EstrellasSeAtraen() {
        setTitle("Estrellas en Movimiento");
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        estrellas = new Estrella[numeroestrellas];
        for (int i = 0; i < numeroestrellas; i++) {
            int x = (int) (Math.random() * width * 0.8 + width * 0.1); // Cluster stars in a central region
            int y = (int) (Math.random() * height * 0.8 + height * 0.1);
            int angulo = (int) (Math.random() * 360);
            int velocidad = (int) (Math.random() * 4 + 1); // Lower initial velocity for cluster effect
            double masa = Math.random() * 500 + 100; // Increase mass for stronger gravity
            estrellas[i] = new Estrella(x, y, angulo, velocidad, width, height, masa);
        }

        buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferGraphics = (Graphics2D) buffer.getGraphics();

        JPanel panel = new JPanel() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                // Apply a semi-transparent black overlay for motion blur
                bufferGraphics.setColor(new Color(0, 0, 0, 10)); // Lower opacity for better trails
                bufferGraphics.fillRect(0, 0, width, height);
                
                bufferGraphics.setColor(Color.WHITE);
                for (int i = 0; i < numeroestrellas; i++) {
                    double[] lastPos = estrellas[i].getPosicionAnterior();
                    double[] currentPos = estrellas[i].getPosicion();
                    
                    // Draw a trail line from last known position to current position
                    bufferGraphics.drawLine((int) lastPos[0], (int) lastPos[1], (int) currentPos[0], (int) currentPos[1]);
                    
                    estrellas[i].rebotaBordes();
                    bufferGraphics.fillRect((int) currentPos[0], (int) currentPos[1], 2, 2);
                }
                
                g.drawImage(buffer, 0, 0, null);
            }
        };

        add(panel);
        setVisible(true);

        new Thread(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            while (true) {
                for (int i = 0; i < numeroestrellas; i++) {
                    final int index = i;
                    executor.submit(() -> {
                        double[] fuerzas = {0, 0};
                        for (int j = 0; j < numeroestrellas; j++) {
                            if (index == j) continue;
                            estrellas[index].calculaFuerza(estrellas[j], fuerzas);
                        }
                        estrellas[index].actualizaPosicion(fuerzas[0], fuerzas[1]);
                    });
                }

                try {
                    executor.shutdown();
                    executor.awaitTermination(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                panel.repaint();
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            }
        }).start();
    }

    public static void main(String[] args) {
        new J038EstrellasSeAtraen();
    }
}
