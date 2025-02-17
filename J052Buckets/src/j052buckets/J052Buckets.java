package j052buckets;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class J052Buckets {

    /**
     * Adjusts the contrast and brightness of an image using parallel processing.
     * Instead of launching thousands of tasks, the work is split among a fixed number
     * of threads equal to the number of available processors. Each thread processes
     * a subset of buckets.
     *
     * @param src        The source image.
     * @param contrast   Contrast factor (e.g., 1.2 increases contrast by 20%).
     * @param brightness Brightness offset (e.g., 10 increases brightness by 10 units).
     * @return A new BufferedImage with the applied adjustments.
     */
    public static BufferedImage adjustContrastBrightnessParallel(BufferedImage src, float contrast, float brightness) {
        int width = src.getWidth();
        int height = src.getHeight();
        // Create a new image with the same dimensions and type.
        BufferedImage result = new BufferedImage(width, height, src.getType());

        final int bucketSize = 4;  // Using a small bucket size for finer partitioning.
        int bucketsX = (width + bucketSize - 1) / bucketSize;
        int bucketsY = (height + bucketSize - 1) / bucketSize;
        int numCores = Runtime.getRuntime().availableProcessors();

        // Create an array of lists. Each list will hold the bucket tasks for one thread.
        @SuppressWarnings("unchecked")
        List<Runnable>[] tasksPerThread = new ArrayList[numCores];
        for (int i = 0; i < numCores; i++) {
            tasksPerThread[i] = new ArrayList<>();
        }

        // Partition the buckets among the available threads.
        for (int bx = 0; bx < bucketsX; bx++) {
            for (int by = 0; by < bucketsY; by++) {
                final int bucketX = bx;
                final int bucketY = by;
                final int startX = bucketX * bucketSize;
                final int startY = bucketY * bucketSize;
                final int endX = Math.min(startX + bucketSize, width);
                final int endY = Math.min(startY + bucketSize, height);

                Runnable bucketTask = () -> {
                    // Log which thread is processing which bucket.
                    System.err.println("Thread " + Thread.currentThread().getName() +
                            " processing bucket (" + bucketX + ", " + bucketY + ") with pixels x: " +
                            startX + "-" + (endX - 1) + " and y: " + startY + "-" + (endY - 1));
                    System.err.flush();

                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            int rgb = src.getRGB(x, y);
                            Color originalColor = new Color(rgb);

                            // Adjust each color channel.
                            int r = (int) (contrast * (originalColor.getRed() - 128) + 128 + brightness);
                            int g = (int) (contrast * (originalColor.getGreen() - 128) + 128 + brightness);
                            int b = (int) (contrast * (originalColor.getBlue() - 128) + 128 + brightness);

                            // Clamp values to the range 0-255.
                            r = Math.min(255, Math.max(0, r));
                            g = Math.min(255, Math.max(0, g));
                            b = Math.min(255, Math.max(0, b));

                            Color newColor = new Color(r, g, b);
                            result.setRGB(x, y, newColor.getRGB());
                        }
                    }
                };

                // Assign the bucket to one of the threads using a round-robin strategy.
                int threadIndex = (bx + by * bucketsX) % numCores;
                tasksPerThread[threadIndex].add(bucketTask);
            }
        }

        // Create and start threads. Each thread processes its assigned bucket tasks sequentially.
        Thread[] threads = new Thread[numCores];
        for (int i = 0; i < numCores; i++) {
            final List<Runnable> tasksForThread = tasksPerThread[i];
            threads[i] = new Thread(() -> {
                for (Runnable task : tasksForThread) {
                    task.run();
                }
            }, "Worker-" + i);
            threads[i].start();
        }

        // Wait for all threads to finish.
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            float contrast = 1.2f;
            float brightness = 10.0f;

            // Process the image in parallel.
            BufferedImage processedImage = adjustContrastBrightnessParallel(image, contrast, brightness);

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
