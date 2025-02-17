/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package j050.video;

import org.jcodec.api.FrameGrab;
import org.jcodec.api.JCodecException;
import org.jcodec.api.SequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.Rational;
import org.jcodec.scale.AWTUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class J050Video {

    public static void main(String[] args) {
        File inputFile = new File("input.mp4");
        File outputFile = new File("output.mp4");
        
        try {
            // Open the input video for frame grabbing.
            FrameGrab grab = FrameGrab.createFrameGrab(NIOUtils.readableChannel(inputFile));
            
            // Set up the encoder (here assuming 25 fps, adjust as needed).
            SequenceEncoder encoder = new SequenceEncoder(
                    NIOUtils.writableChannel(outputFile), Rational.R(25, 1));
            
            Picture picture;
            while ((picture = grab.getNativeFrame()) != null) {
                // Convert the native frame to a BufferedImage.
                BufferedImage frame = AWTUtil.toBufferedImage(picture);
                
                // Process the frame to get the negative.
                BufferedImage negativeFrame = createNegative(frame);
                
                // Convert the processed BufferedImage back to a Picture.
                Picture negativePic = AWTUtil.fromBufferedImage(negativeFrame, ColorSpace.RGB);
                
                // Encode the processed frame.
                encoder.encodeNativeFrame(negativePic);
            }
            
            // Finalize the video file.
            encoder.finish();
            System.out.println("Video processing complete.");
            
        } catch (IOException | JCodecException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a negative of the given BufferedImage.
     *
     * @param original the original image
     * @return a new BufferedImage with inverted colors
     */
    private static BufferedImage createNegative(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage negative = new BufferedImage(width, height, original.getType());
        
        // Process each pixel.
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgba = original.getRGB(x, y);
                int a = (rgba >> 24) & 0xff;
                int r = (rgba >> 16) & 0xff;
                int g = (rgba >> 8) & 0xff;
                int b = rgba & 0xff;
                
                // Invert the color components.
                int negR = 255 - r;
                int negG = 255 - g;
                int negB = 255 - b;
                
                int negativePixel = (a << 24) | (negR << 16) | (negG << 8) | negB;
                negative.setRGB(x, y, negativePixel);
            }
        }
        return negative;
    }
}
