package j041.imagenesswing;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

// Autor: Jose Vicente Carratala
public class J041ImagenesSwing extends JFrame {
     private JTextField inputFolderField;
    private JTextField outputFolderField;
    private JButton startButton;
    private JSlider brightnessSlider;
    private JSlider contrastSlider;
    private JCheckBox invertCheckBox;

    public J041ImagenesSwing() {
        setTitle("Image Processor");
        setSize(600, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 1));
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputFolderField = new JTextField();
        JButton inputButton = new JButton("Select Source Folder");
        inputButton.addActionListener(e -> chooseFolder(inputFolderField));
        inputPanel.add(inputFolderField, BorderLayout.CENTER);
        inputPanel.add(inputButton, BorderLayout.EAST);

        JPanel outputPanel = new JPanel(new BorderLayout());
        outputFolderField = new JTextField();
        JButton outputButton = new JButton("Select Destination Folder");
        outputButton.addActionListener(e -> chooseFolder(outputFolderField));
        outputPanel.add(outputFolderField, BorderLayout.CENTER);
        outputPanel.add(outputButton, BorderLayout.EAST);

        JPanel operationPanel = new JPanel(new GridLayout(3, 1));
        brightnessSlider = new JSlider(-100, 100, 0);
        brightnessSlider.setBorder(BorderFactory.createTitledBorder("Brightness"));
        contrastSlider = new JSlider(0, 200, 100);
        contrastSlider.setBorder(BorderFactory.createTitledBorder("Contrast"));
        invertCheckBox = new JCheckBox("Invert Colors");
        operationPanel.add(brightnessSlider);
        operationPanel.add(contrastSlider);
        operationPanel.add(invertCheckBox);

        startButton = new JButton("Process Images");
        startButton.addActionListener(e -> processImages());
        
        add(inputPanel);
        add(outputPanel);
        add(operationPanel);
        add(startButton);
        
        setVisible(true);
    }
    
    private void chooseFolder(JTextField textField) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void processImages() {
        String inputFolder = inputFolderField.getText();
        String outputFolder = outputFolderField.getText();
        
        if (inputFolder.isEmpty() || outputFolder.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select both folders.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File inputDir = new File(inputFolder);
        File outputDir = new File(outputFolder);

        if (!inputDir.exists() || !inputDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid source folder.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] files = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png"));
        if (files == null || files.length == 0) {
            JOptionPane.showMessageDialog(this, "No images found in the source folder.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (File file : files) {
            applyFilters(file, outputFolder);
        }

        JOptionPane.showMessageDialog(this, "Processing completed.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyFilters(File inputFile, String outputFolder) {
        try {
            BufferedImage image = ImageIO.read(inputFile);
            if (image == null) return;

            float brightness = brightnessSlider.getValue() / 100f;
            float contrast = contrastSlider.getValue() / 100f;
            boolean invert = invertCheckBox.isSelected();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    int alpha = (rgb >> 24) & 0xFF;
                    int red   = (rgb >> 16) & 0xFF;
                    int green = (rgb >> 8) & 0xFF;
                    int blue  = rgb & 0xFF;

                    if (invert) {
                        red = 255 - red;
                        green = 255 - green;
                        blue = 255 - blue;
                    }
                    
                    red = Math.min(255, Math.max(0, (int) ((red - 128) * contrast + 128 + brightness * 255)));
                    green = Math.min(255, Math.max(0, (int) ((green - 128) * contrast + 128 + brightness * 255)));
                    blue = Math.min(255, Math.max(0, (int) ((blue - 128) * contrast + 128 + brightness * 255)));

                    int newRGB = (alpha << 24) | (red << 16) | (green << 8) | blue;
                    image.setRGB(x, y, newRGB);
                }
            }

            File outputFile = new File(outputFolder, inputFile.getName());
            ImageIO.write(image, getFileExtension(inputFile.getName()), outputFile);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error processing image: " + inputFile.getName(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "jpg" : fileName.substring(lastDot + 1);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(J041ImagenesSwing::new);
    }
}
