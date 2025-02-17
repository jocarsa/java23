package j049.redprocesamientocliente;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class J049RedProcesamientoCliente {

    // Adjust this constant to simulate heavy computations (lasting seconds to minutes)
    private static final long WORKLOAD_ITERATIONS = 500000;

    public static void main(String[] args) {
        String serverHost = "localhost";
        int serverPort = 5000;
        
        try (Socket socket = new Socket(serverHost, serverPort);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             
            // Determine available cores and inform the server.
            int availableCores = Runtime.getRuntime().availableProcessors();
            System.out.println("Connected to Calculation Server as client with " + availableCores + " cores.");
            out.println("CORES " + availableCores);
            
            // Create a thread pool equal to the number of available cores.
            ExecutorService executor = Executors.newFixedThreadPool(availableCores);
            
            // Continuously read tasks from the server.
            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("TASK")) {
                    // Expected format: TASK <taskId> <start> <end>
                    String[] tokens = message.split("\\s+");
                    if (tokens.length >= 4) {
                        int taskId = Integer.parseInt(tokens[1]);
                        long start = Long.parseLong(tokens[2]);
                        long end = Long.parseLong(tokens[3]);
                        System.out.println("Received task " + taskId + ": compute range [" + start + ", " + end + ")");
                        
                        // Submit the computation to the thread pool.
                        executor.submit(() -> {
                            double partialSum = 0.0;
                            for (long k = start; k < end; k++) {
                                // Heavy inner loop to simulate load.
                                for (long i = 0; i < WORKLOAD_ITERATIONS; i++) {
                                    double dummy = Math.sqrt((i + k) % 1000 + 1);
                                }
                                partialSum += (k % 2 == 0 ? 1 : -1) / (2.0 * k + 1);
                            }
                            
                            // Send the result back (synchronized to avoid interleaved writes).
                            synchronized(out) {
                                String resultMessage = "RESULT " + taskId + " " + partialSum + " " + start + " " + end;
                                out.println(resultMessage);
                            }
                            System.out.println("Sent result for task " + taskId + " with range [" + start + ", " + end + ")");
                        });
                    }
                } else {
                    // If the message is not a task, pause briefly.
                    Thread.sleep(100);
                }
            }
            
            // Shutdown the executor gracefully.
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
