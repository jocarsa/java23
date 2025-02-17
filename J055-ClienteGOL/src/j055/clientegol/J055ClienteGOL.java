package j055.clientegol;

import java.io.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.net.*;
import java.util.concurrent.*;
import java.util.zip.*;
import java.util.Base64;

public class J055ClienteGOL {

    private static final long WORKLOAD_ITERATIONS = 500000;

    private static boolean[][] decodeBoard(String boardStr, int width) {
        String[] rows = boardStr.split(";");
        int height = rows.length;
        boolean[][] board = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            String row = rows[y];
            for (int x = 0; x < width && x < row.length(); x++) {
                board[y][x] = (row.charAt(x) == '1');
            }
        }
        return board;
    }

    private static String encodeBoard(boolean[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                sb.append(board[y][x] ? "1" : "0");
            }
            if (y < board.length - 1) {
                sb.append(";");
            }
        }
        return sb.toString();
    }

    private static boolean[][] computeGOL(boolean[][] slice, int startRowInclusive, int endRowExclusive) {
        int height = slice.length;
        int width = slice[0].length;
        int numRows = endRowExclusive - startRowInclusive;
        boolean[][] result = new boolean[numRows][width];

        for (int y = startRowInclusive; y < endRowExclusive; y++) {
            for (int x = 0; x < width; x++) {
                int count = 0;
                for (int j = -1; j <= 1; j++) {
                    int ny = y + j;
                    if (ny < 0 || ny >= height)
                        continue;
                    for (int i = -1; i <= 1; i++) {
                        int nx = x + i;
                        if (nx < 0 || nx >= width)
                            continue;
                        if (i == 0 && j == 0)
                            continue;
                        if (slice[ny][nx])
                            count++;
                    }
                }
                boolean current = slice[y][x];
                result[y - startRowInclusive][x] = (current && (count == 2 || count == 3)) || (!current && count == 3);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 5000;

        try (Socket socket = new Socket(serverHost, serverPort);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            int availableCores = Runtime.getRuntime().availableProcessors();
            System.out.println("Connected to Calculation Server as client with " + availableCores + " cores.");
            out.println("CORES " + availableCores);

            new Thread(() -> {
                OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                        ManagementFactory.getOperatingSystemMXBean();
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double load = osBean.getSystemCpuLoad();
                    int loadPercentage = (load < 0 ? 0 : (int) (load * 100));
                    synchronized (out) {
                        out.println("CPU_USAGE " + loadPercentage);
                    }
                    System.out.println("Reported overall CPU load: " + loadPercentage + "%");
                }
            }).start();

            ExecutorService executor = Executors.newFixedThreadPool(availableCores);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("TASK_GOL")) {
                    String[] tokens = message.split("\\s+", 5);
                    if (tokens.length >= 5) {
                        int taskId = Integer.parseInt(tokens[1]);
                        int globalStart = Integer.parseInt(tokens[2]);
                        int globalEnd = Integer.parseInt(tokens[3]);
                        String payload = new String(Base64.getDecoder().decode(tokens[4]));

                        String firstRow = payload.split(";")[0];
                        int width = firstRow.length();
                        boolean[][] slice = decodeBoard(payload, width);

                        int localStart = (globalStart > 0) ? 1 : 0;
                        int localEnd = (globalEnd < globalStart + slice.length - localStart) ? slice.length - 1 : slice.length;

                        final int finalLocalStart = localStart;
                        final int finalLocalEnd = localEnd;
                        final int taskIdFinal = taskId;
                        final int finalGlobalStart = globalStart;
                        final int finalGlobalEnd = globalEnd;

                        executor.submit(() -> {
                            boolean[][] updatedSlice = computeGOL(slice, finalLocalStart, finalLocalEnd);
                            String encodedResult = Base64.getEncoder().encodeToString(encodeBoard(updatedSlice).getBytes());
                            synchronized (out) {
                                String resultMessage = "RESULT_GOL " + taskIdFinal + " " +
                                        finalGlobalStart + " " + finalGlobalEnd + " " + encodedResult;
                                out.println(resultMessage);
                            }
                            System.out.println("Sent GOL result for task " + taskIdFinal);
                        });
                    }
                } else if (message.startsWith("TASK")) {
                    String[] tokens = message.split("\\s+");
                    if (tokens.length >= 4) {
                        int taskId = Integer.parseInt(tokens[1]);
                        long start = Long.parseLong(tokens[2]);
                        long end = Long.parseLong(tokens[3]);
                        System.out.println("Received PI task " + taskId + ": compute range [" + start + ", " + end + ")");

                        executor.submit(() -> {
                            double partialSum = 0.0;
                            for (long k = start; k < end; k++) {
                                for (long i = 0; i < WORKLOAD_ITERATIONS; i++) {
                                    double dummy = Math.sqrt((i + k) % 1000 + 1);
                                }
                                partialSum += (k % 2 == 0 ? 1 : -1) / (2.0 * k + 1);
                            }
                            synchronized (out) {
                                String resultMessage = "RESULT " + taskId + " " + partialSum;
                                out.println(resultMessage);
                            }
                            System.out.println("Sent result for PI task " + taskId);
                        });
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
