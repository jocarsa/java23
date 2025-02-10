package j036.imagen;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop;
import javax.imageio.ImageIO;
// Autor Jose Vicente Carratala
public class J036Imagen{
    public static void main(String[] args) {
        int width = 400;
        int height = 400;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = image.createGraphics();
        
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width, height);
        
        g2d.setColor(Color.RED);
        g2d.fillRect(50, 50, 300, 300);
        
        g2d.dispose();

        File outputFile = new File("output_image.png");
        try {
            ImageIO.write(image, "PNG", outputFile);
            System.out.println("Image saved as output_image.png");

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputFile);
            } else {
                System.out.println("Desktop is not supported. Unable to open the image.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
