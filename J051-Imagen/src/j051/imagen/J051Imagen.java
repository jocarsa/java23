package j051.imagen;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class J051Imagen {

    /**
     * Adjusts the contrast and brightness of an image.
     *
     * @param src        The source image.
     * @param contrast   Contrast factor (e.g., 1.2 increases contrast by 20%).
     * @param brightness Brightness offset (e.g., 10 increases brightness by 10 units).
     * @return A new BufferedImage with the applied adjustments.
     */
    public static BufferedImage adjustContrastBrightness(BufferedImage src, float contrast, float brightness) {
        int width = src.getWidth();
        int height = src.getHeight();
        // Create a new image with the same dimensions and type.
        BufferedImage result = new BufferedImage(width, height, src.getType());

        // Process each pixel.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Get the original pixel color.
                int rgb = src.getRGB(x, y);
                Color originalColor = new Color(rgb);

                // Adjust each color channel.
                int r = (int) (contrast * (originalColor.getRed() - 128) + 128 + brightness);
                int g = (int) (contrast * (originalColor.getGreen() - 128) + 128 + brightness);
                int b = (int) (contrast * (originalColor.getBlue() - 128) + 128 + brightness);

                // Ensure the new values are within 0-255.
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                // Set the new pixel color.
                Color newColor = new Color(r, g, b);
                result.setRGB(x, y, newColor.getRGB());
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // Record the start time of the program.
        long startTime = System.currentTimeMillis();
        
        try {
            // Load the image from a file (ensure the path is correct).
            BufferedImage image = ImageIO.read(new File("josevicente.jpg"));
            
            // Set desired contrast and brightness values.
            // For example, 1.2 increases contrast by 20% and 10 increases brightness.
            float contrast = 1.2f;
            float brightness = 10.0f;
            
            // Process the image.
            BufferedImage processedImage = adjustContrastBrightness(image, contrast, brightness);
            
            // Save the processed image as "josevicente2.jpg".
            ImageIO.write(processedImage, "jpg", new File("josevicente2.jpg"));

            // Display the processed image in a JFrame.
            JFrame frame = new JFrame("Processed Image");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new JLabel(new ImageIcon(processedImage)));
            frame.pack();
            frame.setLocationRelativeTo(null); // Center the frame on screen.
            frame.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Record the end time of the program.
        long endTime = System.currentTimeMillis();
        // Calculate and print the elapsed time in milliseconds.
        System.out.println("Execution time: " + (endTime - startTime) + " milliseconds.");
    }
}
