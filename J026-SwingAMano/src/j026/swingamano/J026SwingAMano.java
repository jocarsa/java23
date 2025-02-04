package j026.swingamano;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Autor Jose Vicente Carratala
public class J026SwingAMano{
    public static void main(String[] args) {
        /*
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            Class: javax.swing.plaf.metal.MetalLookAndFeel
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");

        */
        
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JFrame frame = new JFrame("Programa de Jose Vicente");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLayout(new FlowLayout());
        
         // Create label and text field
        JLabel label = new JLabel("Enter your name:");
        JTextField textField = new JTextField(15);

        // Create button
        JButton button = new JButton("Enviar");

        // Add action listener to button
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = textField.getText();
                JOptionPane.showMessageDialog(frame, "Hola, " + name + "!");
            }
        });

        // Add components to frame
        frame.add(label);
        frame.add(textField);
        frame.add(button);

        // Set visibility
        frame.setVisible(true);
    }
}
