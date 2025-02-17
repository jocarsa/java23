package j056.dispatchergol;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.zip.*;
import java.util.Base64;

public class J056DispatcherGOL extends JPanel {
    private static final int WIDTH = 2048;
    private static final int HEIGHT = 2048;
    private boolean[][] board = new boolean[HEIGHT][WIDTH];

    public J056DispatcherGOL() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        initBoard();
    }

    private void initBoard() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[y][x] = Math.random() < 0.2;
            }
        }
    }

    private String encodeBoard(boolean[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                sb.append(board[y][x] ? "1" : "0");
            }
            if (y < board.length - 1) sb.append(";");
        }
        return sb.toString();
    }

    private boolean[][] decodeBoard(String boardStr) {
        String[] rows = boardStr.split(";");
        int h = rows.length;
        int w = rows[0].length();
        boolean[][] newBoard = new boolean[h][w];
        for (int y = 0; y < h; y++) {
            String row = rows[y];
            for (int x = 0; x < w; x++) {
                newBoard[y][x] = (row.charAt(x) == '1');
            }
        }
        return newBoard;
    }

    public void updateBoardDistributed() {
        String serverHost = "localhost";
        int dispatcherPort = 3000;
        int numSlices = 8;
        try (Socket socket = new Socket(serverHost, dispatcherPort);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("JOB_GOL " + WIDTH + " " + HEIGHT + " " + numSlices);
            out.println(Base64.getEncoder().encodeToString(encodeBoard(board).getBytes()));
            System.out.println("DistributedGameOfLife: Sent JOB_GOL to server.");

            String response = in.readLine();
            if (response != null && response.startsWith("JOB_GOL_DONE")) {
                String newBoardEncoded = new String(Base64.getDecoder().decode(response.substring("JOB_GOL_DONE".length()).trim()));
                board = decodeBoard(newBoardEncoded);
                repaint();
                System.out.println("DistributedGameOfLife: Updated board received.");
            } else {
                System.out.println("DistributedGameOfLife: Unexpected response: " + response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                g.setColor(board[y][x] ? Color.BLACK : Color.WHITE);
                g.fillRect(x, y, 1, 1);
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Game of Life (Distributed)");
        J056DispatcherGOL gol = new J056DispatcherGOL();
        frame.add(gol);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(1000, e -> gol.updateBoardDistributed());
        timer.start();
    }
}
