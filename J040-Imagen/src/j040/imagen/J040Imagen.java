package j040.imagen;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

// Autor: Jose Vicente Carratala
public class J040Imagen {
    public static void main(String[] args) {
        String inputFolder = "seleccion";  // Carpeta de imágenes de entrada
        String outputFolder = "seleccion2"; // Carpeta para guardar las imágenes invertidas

        File inputDir = new File(inputFolder);
        File outputDir = new File(outputFolder);

        // Crear la carpeta de salida si no existe
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Verificar si la carpeta de entrada existe y es un directorio
        if (inputDir.exists() && inputDir.isDirectory()) {
            File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
            
            if (files != null) {
                for (File file : files) {
                    processImage(file, outputFolder);
                }
                System.out.println("Procesamiento completado.");
            } else {
                System.err.println("No se encontraron imágenes en la carpeta.");
            }
        } else {
            System.err.println("La carpeta de entrada no existe o no es un directorio válido.");
        }
    }

    private static void processImage(File inputFile, String outputFolder) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) {
                System.err.println("No se pudo leer la imagen: " + inputFile.getName());
                return;
            }

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);

                    int alpha = (rgb >> 24) & 0xFF;
                    int red   = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue  = rgb & 0xFF;

                    red = 255 - red;
                    green = 255 - green;
                    blue = 255 - blue;

                    int invertedRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, invertedRGB);
                }
            }

            File outputFile = new File(outputFolder, inputFile.getName());
            ImageIO.write(image, getFileExtension(inputFile.getName()), outputFile);
            System.out.println("Imagen invertida guardada: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Error al procesar la imagen " + inputFile.getName() + ": " + e.getMessage());
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "jpg" : fileName.substring(lastDot + 1);
    }
}
