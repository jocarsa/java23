package j048.redprocesamientoservidor;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

// --- Data structure for a task (job packet) ---
class Task {
    int taskId;
    long start;
    long end;
    
    public Task(int taskId, long start, long end) {
        this.taskId = taskId;
        this.start = start;
        this.end = end;
    }
}

public class J048RedProcesamientoServidor {

    // Ports for the two kinds of connections.
    public static final int CLIENT_PORT = 5000;
    public static final int DISPATCHER_PORT = 3000;

    // Shared task queue. When a job comes in the tasks (packets) are created here.
    public static BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>();

    // Map to store the results coming from tasks (key = taskId, value = partial sum)
    public static ConcurrentHashMap<Integer, Double> taskResults = new ConcurrentHashMap<>();

    // Count of tasks expected in the current job.
    public static AtomicInteger totalTasks = new AtomicInteger(0);

    // Map to hold connected clients (for logging, etc.)
    public static ConcurrentHashMap<String, ClientHandler> clients = new ConcurrentHashMap<>();
    public static AtomicInteger clientCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        System.out.println("Calculation Server starting...");

        // Start thread to listen for client connections.
        new Thread(new ClientAcceptor()).start();

        // Start thread to listen for dispatcher connections.
        new Thread(new DispatcherAcceptor()).start();
    }
}

// --- Thread that accepts calculation client connections ---
class ClientAcceptor implements Runnable {
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(J048RedProcesamientoServidor.CLIENT_PORT)) {
            System.out.println("Client listener started on port " + J048RedProcesamientoServidor.CLIENT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = "Client" + J048RedProcesamientoServidor.clientCounter.getAndIncrement();
                ClientHandler handler = new ClientHandler(clientSocket, clientId);
                J048RedProcesamientoServidor.clients.put(clientId, handler);
                new Thread(handler).start();
                System.out.println("Connected: " + clientId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// --- Thread that accepts job dispatcher connections ---
class DispatcherAcceptor implements Runnable {
    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(J048RedProcesamientoServidor.DISPATCHER_PORT)) {
            System.out.println("Dispatcher listener started on port " + J048RedProcesamientoServidor.DISPATCHER_PORT);
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

// --- Handler for job dispatcher connections ---
class DispatcherHandler implements Runnable {
    private Socket socket;
    
    public DispatcherHandler(Socket socket) {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // The dispatcher is expected to send a job command of the format:
            // JOB <totalIterations> <numPackets>
            String jobCommand = in.readLine();
            if (jobCommand != null && jobCommand.startsWith("JOB")) {
                String[] tokens = jobCommand.split("\\s+");
                if (tokens.length >= 3) {
                    long totalIterations = Long.parseLong(tokens[1]);
                    int numPackets = Integer.parseInt(tokens[2]);
                    long iterationsPerTask = totalIterations / numPackets;
                    int taskId = 1;

                    // Create each task (packet) and add it to the shared queue.
                    for (int i = 0; i < numPackets; i++) {
                        long start = i * iterationsPerTask;
                        long end = (i == numPackets - 1) ? totalIterations : (i + 1) * iterationsPerTask;
                        Task task = new Task(taskId++, start, end);
                        J048RedProcesamientoServidor.taskQueue.offer(task);
                    }
                    J048RedProcesamientoServidor.totalTasks.set(numPackets);
                    System.out.println("Job received: totalIterations=" + totalIterations + ", numPackets=" + numPackets);
                    
                    // Wait until all task results have been gathered.
                    while (J048RedProcesamientoServidor.taskResults.size() < numPackets) {
                        Thread.sleep(100);
                    }
                    
                    // Aggregate the partial sums from each task.
                    double sum = 0.0;
                    for (Double partial : J048RedProcesamientoServidor.taskResults.values()) {
                        sum += partial;
                    }
                    double piApprox = 4 * sum; // Leibniz series: π = 4 * (sum of partials)
                    
                    out.println("JOB DONE: PI = " + piApprox);
                    System.out.println("Job complete: PI approximated as " + piApprox);
                    
                    // Clear the results for a possible future job.
                    J048RedProcesamientoServidor.taskResults.clear();
                } else {
                    out.println("Invalid JOB command. Format: JOB <totalIterations> <numPackets>");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

// --- Updated Handler for a connected calculation client (worker) ---
class ClientHandler implements Runnable {
    private Socket socket;
    private String clientId;
    
    public ClientHandler(Socket socket, String clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }
    
    @Override
    public void run() {
        try {
            // Prepare I/O for this client.
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
            // Expect the first message to be "CORES <n>"
            String coresMessage = in.readLine();
            int tempCores = 1; // default value
            if (coresMessage != null && coresMessage.startsWith("CORES")) {
                String[] tokens = coresMessage.split("\\s+");
                if (tokens.length >= 2) {
                    tempCores = Integer.parseInt(tokens[1]);
                    System.out.println("Client " + clientId + " has " + tempCores + " cores available.");
                }
            } else {
                out.println("ERROR: Expected CORES message.");
                socket.close();
                return;
            }
            final int availableCores = tempCores;
            
            // Shared counter for tasks currently being processed on this client.
            AtomicInteger tasksInFlight = new AtomicInteger(0);
            
            // Sender thread: dispatches tasks up to availableCores concurrently.
            Thread sender = new Thread(() -> {
                try {
                    while (!socket.isClosed()) {
                        if (tasksInFlight.get() < availableCores) {
                            // Try to get a task from the shared queue.
                            Task task = J048RedProcesamientoServidor.taskQueue.poll(1, TimeUnit.SECONDS);
                            if (task != null) {
                                String taskMessage = "TASK " + task.taskId + " " + task.start + " " + task.end;
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
            
            // Receiver thread: reads results from the client.
            Thread receiver = new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        if (response.startsWith("RESULT")) {
                            String[] tokens = response.split("\\s+");
                            if (tokens.length >= 3) {
                                int resultTaskId = Integer.parseInt(tokens[1]);
                                double partialSum = Double.parseDouble(tokens[2]);
                                long rangeStart = tokens.length >= 4 ? Long.parseLong(tokens[3]) : -1;
                                long rangeEnd = tokens.length >= 5 ? Long.parseLong(tokens[4]) : -1;
                                J048RedProcesamientoServidor.taskResults.put(resultTaskId, partialSum);
                                System.out.println("Received result for task " + resultTaskId + " from " + clientId +
                                        (rangeStart != -1 ? " with range [" + rangeStart + ", " + rangeEnd + ")" : ""));
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
            J048RedProcesamientoServidor.clients.remove(clientId);
            try { socket.close(); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}

