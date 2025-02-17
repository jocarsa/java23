package j048.redprocesamientotrabajos;

import java.io.*;
import java.net.*;

public class J048RedProcesamientoTrabajos {

   public static void main(String[] args) {
        String serverHost = "localhost";
        int dispatcherPort = 3000;
        
        // For this example, define a job that uses:
        // - totalIterations: total number of iterations in the Leibniz series
        // - numPackets: how many task packets to split the work into
        long totalIterations = 1000000;  // e.g., 1,000,000 iterations
        int numPackets = 20;
        
        try (Socket socket = new Socket(serverHost, dispatcherPort);
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
             
            System.out.println("Connected to Calculation Server as dispatcher.");
            
            // Send the job command. Format: JOB <totalIterations> <numPackets>
            String jobCommand = "JOB " + totalIterations + " " + numPackets;
            out.println(jobCommand);
            System.out.println("Sent job: " + jobCommand);
            
            // Wait for the job result from the server.
            String result = in.readLine();
            if (result != null) {
                System.out.println("Received from server: " + result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
