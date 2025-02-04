package j030.lectorcsv;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LectorCSV extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private File currentFile;

    public LectorCSV() {
        setTitle("Lector CSV");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set Nimbus look and feel (if available)
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Create table model and table
        tableModel = new DefaultTableModel();
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding around the table
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel with buttons for adding rows and columns
        JPanel bottomPanel = new JPanel();
        JButton addRowButton = new JButton("Agregar Fila");
        JButton addColumnButton = new JButton("Agregar Columna");

        addRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add an empty row with the same number of columns as the table
                int columnCount = tableModel.getColumnCount();
                Object[] emptyRow = new Object[columnCount];
                tableModel.addRow(emptyRow);
            }
        });

        addColumnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add a new column with a default name
                int newColumnIndex = tableModel.getColumnCount();
                String columnName = "Column " + (newColumnIndex + 1);
                tableModel.addColumn(columnName);
            }
        });

        bottomPanel.add(addRowButton);
        bottomPanel.add(addColumnButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Create menu bar and add menu items
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Archivo");

        // "Abrir" menu item to load a CSV file
        JMenuItem openItem = new JMenuItem("Abrir");
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openCSV();
            }
        });
        fileMenu.add(openItem);

        // "Guardar" menu item to save to the current file or ask for a file name if none exists
        JMenuItem saveItem = new JMenuItem("Guardar");
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentFile != null) {
                    saveCSV();
                } else {
                    saveCSVAs();
                }
            }
        });
        fileMenu.add(saveItem);

        // "Guardar Como" menu item to always ask for a file name
        JMenuItem saveAsItem = new JMenuItem("Guardar Como");
        saveAsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCSVAs();
            }
        });
        fileMenu.add(saveAsItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Opens a CSV file and loads its content into the table.
     */
    private void openCSV() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                String line;
                tableModel.setRowCount(0);
                tableModel.setColumnCount(0);
                
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    // Ensure the table has enough columns.
                    if (tableModel.getColumnCount() < data.length) {
                        for (int i = tableModel.getColumnCount(); i < data.length; i++) {
                            tableModel.addColumn("Column " + (i + 1));
                        }
                    }
                    tableModel.addRow(data);
                }
                JOptionPane.showMessageDialog(this, "Archivo cargado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error al abrir el archivo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Saves the current table content to the current file.
     */
    private void saveCSV() {
        if (currentFile == null) {
            saveCSVAs();
            return;
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
            int rowCount = tableModel.getRowCount();
            int columnCount = tableModel.getColumnCount();
            for (int i = 0; i < rowCount; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < columnCount; j++) {
                    Object cellObj = tableModel.getValueAt(i, j);
                    String cell = (cellObj != null) ? cellObj.toString() : "";
                    line.append(cell);
                    if (j < columnCount - 1) {
                        line.append(",");
                    }
                }
                bw.write(line.toString());
                bw.newLine();
            }
            JOptionPane.showMessageDialog(this, "Archivo guardado exitosamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el archivo", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Prompts the user to choose a file and then saves the current table content.
     */
    private void saveCSVAs() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveCSV();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LectorCSV().setVisible(true));
    }
}
