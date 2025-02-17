package j053.juegodelavida;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class J053Juegodelavida extends JPanel {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 1024;
    private boolean[][] board = new boolean[HEIGHT][WIDTH];
    private boolean[][] nextBoard = new boolean[HEIGHT][WIDTH];
    private final int cores;
    private final ExecutorService executor;
    private int frameCounter = 0;

    public J053Juegodelavida() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        cores = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cores);
        initBoard();
    }

    private void initBoard() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[y][x] = Math.random() < 0.2;
            }
        }
    }

    public void updateBoard() {
        CountDownLatch latch = new CountDownLatch(cores);
        int sliceHeight = HEIGHT / cores;

        for (int i = 0; i < cores; i++) {
            final int startY = i * sliceHeight;
            final int endY = (i == cores - 1) ? HEIGHT : startY + sliceHeight;

            executor.submit(() -> {
                for (int y = startY; y < endY; y++) {
                    for (int x = 0; x < WIDTH; x++) {
                        int neighbors = countNeighbors(x, y);
                        nextBoard[y][x] = (board[y][x] && (neighbors == 2 || neighbors == 3)) || (!board[y][x] && neighbors == 3);
                    }
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean[][] temp = board;
        board = nextBoard;
        nextBoard = temp;

        frameCounter++;
        if (frameCounter % 100 == 0) {
            repaint();
        }
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int j = -1; j <= 1; j++) {
            int ny = y + j;
            if (ny < 0 || ny >= HEIGHT) continue;
            for (int i = -1; i <= 1; i++) {
                int nx = x + i;
                if (nx < 0 || nx >= WIDTH) continue;
                if (i == 0 && j == 0) continue;
                if (board[ny][nx]) count++;
            }
        }
        return count;
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
        JFrame frame = new JFrame("Game of Life (Multicore with Swing)");
        J053Juegodelavida gol = new J053Juegodelavida();
        frame.add(gol);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        Timer timer = new Timer(1, e -> gol.updateBoard());
        timer.start();
    }
}
