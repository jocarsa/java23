package j054.servidorgol;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.*;
import java.util.Base64;

class Task {
    int taskId;
    int start;
    int end;
    String payload;

    public Task(int taskId, int start, int end) {
        this(taskId, start, end, null);
    }

    public Task(int taskId, int start, int end, String payload) {
        this.taskId = taskId;
        this.start = start;
        this.end = end;
        this.payload = payload;
    }
}

public class J054ServidorGOL {
    public static final int CLIENT_PORT = 5000;
    public static final int DISPATCHER_PORT = 3000;

    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();
    public static ConcurrentHashMap<Integer, Double> taskResults = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Integer, String> golTaskResults = new ConcurrentHashMap<>();
    public static AtomicInteger totalTasks = new AtomicInteger(0);
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static AtomicInteger clientCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        System.out.println("Calculation Server starting...");

        new Thread(new ClientAcceptor()).start();
        new Thread(new DispatcherAcceptor()).start();
        new Thread(new UsageMonitor()).start();
    }
}

class ClientAcceptor implements Runnable {
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(J054ServidorGOL.CLIENT_PORT)) {
            System.out.println("Client listener started on port " + J054ServidorGOL.CLIENT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = "Cliente" + J054ServidorGOL.clientCounter.getAndIncrement();
                ClientHandler handler = new ClientHandler(clientSocket, clientId);
                J054ServidorGOL.clients.put(clientId, handler);
                new Thread(handler).start();
                System.out.println("Connected: " + clientId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DispatcherAcceptor implements Runnable {
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(J054ServidorGOL.DISPATCHER_PORT)) {
            System.out.println("Dispatcher listener started on port " + J054ServidorGOL.DISPATCHER_PORT);
            while (true) {
                Socket dispatcherSocket = serverSocket.accept();
                new Thread(new DispatcherHandler(dispatcherSocket)).start();
                System.out.println("Dispatcher connected.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class DispatcherHandler implements Runnable {
    private Socket socket;

    public DispatcherHandler(Socket socket) {
        this.socket = socket;
    }

    private boolean[][] decodeBoard(String boardStr, int width, int height) {
        boolean[][] board = new boolean[height][width];
        String[] rows = boardStr.split(";");
        for (int y = 0; y < height && y < rows.length; y++) {
            String row = rows[y];
            for (int x = 0; x < width && x < row.length(); x++) {
                board[y][x] = (row.charAt(x) == '1');
            }
        }
        return board;
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

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String jobCommand = in.readLine();
            if (jobCommand != null && jobCommand.startsWith("JOB_GOL")) {
                String[] tokens = jobCommand.split("\\s+");
                if (tokens.length >= 4) {
                    int width = Integer.parseInt(tokens[1]);
                    int height = Integer.parseInt(tokens[2]);
                    int numSlices = Integer.parseInt(tokens[3]);

                    String boardEncoded = in.readLine();
                    boolean[][] board = decodeBoard(boardEncoded, width, height);

                    int sliceHeight = height / numSlices;
                    int taskId = 1;
                    for (int i = 0; i < numSlices; i++) {
                        int globalStart = i * sliceHeight;
                        int globalEnd = (i == numSlices - 1) ? height : (globalStart + sliceHeight);

                        int localStart = (globalStart > 0) ? globalStart - 1 : globalStart;
                        int localEnd = (globalEnd < height) ? globalEnd + 1 : globalEnd;
                        int numLocalRows = localEnd - localStart;

                        boolean[][] slice = new boolean[numLocalRows][width];
                        for (int r = localStart, rr = 0; r < localEnd; r++, rr++) {
                            System.arraycopy(board[r], 0, slice[rr], 0, width);
                        }
                        String payload = Base64.getEncoder().encodeToString(encodeBoard(slice).getBytes());
                        Task task = new Task(taskId++, globalStart, globalEnd, payload);
                        J054ServidorGOL.taskQueue.offer(task);
                    }
                    J054ServidorGOL.totalTasks.set(numSlices);
                    System.out.println("GOL Job received: board " + width + "x" + height + ", numSlices=" + numSlices);

                    while (J054ServidorGOL.golTaskResults.size() < numSlices) {
                        Thread.sleep(100);
                    }

                    String[] slices = new String[numSlices];
                    for (Map.Entry<Integer, String> entry : J054ServidorGOL.golTaskResults.entrySet()) {
                        int tId = entry.getKey();
                        slices[tId - 1] = new String(Base64.getDecoder().decode(entry.getValue()));
                    }

                    StringBuilder newBoardEncoded = new StringBuilder();
                    for (int i = 0; i < numSlices; i++) {
                        if (i > 0) newBoardEncoded.append(";");
                        newBoardEncoded.append(slices[i]);
                    }

                    out.println("JOB_GOL_DONE " + newBoardEncoded.toString());
                    System.out.println("GOL Job complete. New board state sent to dispatcher.");

                    J054ServidorGOL.golTaskResults.clear();
                } else {
                    out.println("Invalid JOB_GOL command. Format: JOB_GOL <width> <height> <numSlices> then boardState");
                }
            } else if (jobCommand != null && jobCommand.startsWith("JOB")) {
                // (Existing PI job handling would go here.)
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private String clientId;
    private String clientIp;
    private volatile double[] cpuUsages;

    public ClientHandler(Socket socket, String clientId) {
        this.socket = socket;
        this.clientId = clientId;
        this.clientIp = socket.getInetAddress().toString();
    }

    public String getClientIp() { return clientIp; }
    public double[] getCpuUsages() { return cpuUsages; }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String coresMessage = in.readLine();
            int tempCores = 1;
            if (coresMessage != null && coresMessage.startsWith("CORES")) {
                String[] tokens = coresMessage.split("\\s+");
                if (tokens.length >= 2) {
                    tempCores = Integer.parseInt(tokens[1]);
                    System.out.println(clientId + " has " + tempCores + " cores available.");
                }
            } else {
                out.println("ERROR: Expected CORES message.");
                socket.close();
                return;
            }
            final int availableCores = tempCores;
            AtomicInteger tasksInFlight = new AtomicInteger(0);

            Thread sender = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        if (tasksInFlight.get() < availableCores) {
                            Task task = J054ServidorGOL.taskQueue.poll(1, TimeUnit.SECONDS);
                            if (task != null) {
                                String taskMessage;
                                if (task.payload != null) {
                                    taskMessage = "TASK_GOL " + task.taskId + " " + task.start + " " + task.end + " " + task.payload;
                                } else {
                                    taskMessage = "TASK " + task.taskId + " " + task.start + " " + task.end;
                                }
                                out.println(taskMessage);
                                System.out.println("Sent " + taskMessage + " to " + clientId);
                                tasksInFlight.incrementAndGet();
                            }
                        } else {
                            Thread.sleep(100);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Sender thread for " + clientId + " encountered an error: " + e.getMessage());
                }
            });

            Thread receiver = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("CPU_USAGE")) {
                            String[] tokens = response.split("\\s+");
                            int numCores = tokens.length - 1;
                            double[] usages = new double[numCores];
                            for (int i = 0; i < numCores; i++) {
                                usages[i] = Double.parseDouble(tokens[i + 1]);
                            }
                            cpuUsages = usages;
                            continue;
                        }
                        if (response.startsWith("RESULT_GOL")) {
                            String[] tokens = response.split("\\s+", 5);
                            if (tokens.length >= 5) {
                                int resultTaskId = Integer.parseInt(tokens[1]);
                                String resultData = new String(Base64.getDecoder().decode(tokens[4]));
                                J054ServidorGOL.golTaskResults.put(resultTaskId, resultData);
                                System.out.println("Received GOL result for task " + resultTaskId + " from " + clientId);
                                tasksInFlight.decrementAndGet();
                            }
                        } else if (response.startsWith("RESULT")) {
                            String[] tokens = response.split("\\s+");
                            if (tokens.length >= 3) {
                                int resultTaskId = Integer.parseInt(tokens[1]);
                                double partialSum = Double.parseDouble(tokens[2]);
                                J054ServidorGOL.taskResults.put(resultTaskId, partialSum);
                                System.out.println("Received result for task " + resultTaskId + " from " + clientId);
                                tasksInFlight.decrementAndGet();
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Receiver thread for " + clientId + " encountered an error: " + e.getMessage());
                }
            });

            sender.start();
            receiver.start();
            sender.join();
            receiver.join();

        } catch (Exception e) {
            System.out.println("Client " + clientId + " disconnected: " + e.getMessage());
        } finally {
            J054ServidorGOL.clients.remove(clientId);
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

class UsageMonitor implements Runnable {
    @Override
    public void run() {
        while (true) {
            System.out.println("-------- CPU Usage for Connected Clients --------");
            for (Map.Entry<String, ClientHandler> entry : J054ServidorGOL.clients.entrySet()) {
                String clientId = entry.getKey();
                ClientHandler handler = entry.getValue();
                System.out.println(clientId + ": " + handler.getClientIp());
                double[] usages = handler.getCpuUsages();
                if (usages != null) {
                    for (int i = 0; i < usages.length; i++) {
                        int bars = (int)(usages[i] / 10);
                        StringBuilder bar = new StringBuilder();
                        for (int j = 0; j < bars; j++) { bar.append("#"); }
                        for (int j = bars; j < 10; j++) { bar.append("-"); }
                        System.out.println("Core " + (i + 1) + ": |" + bar + "| -> " + String.format("%.0f", usages[i]) + "%");
                    }
                } else {
                    System.out.println("No CPU usage data available yet.");
                }
                System.out.println();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }
}
