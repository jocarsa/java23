
package j046.servidorrendimiento;


import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class J046ServidorRendimiento {
 // Shared map holding the latest metrics from each connected client.
    public static ConcurrentHashMap<String, ClientData> clientsData = new ConcurrentHashMap<>();
    // A counter to assign a unique ID to each client.
    public static AtomicInteger clientCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        int port = 5000; // Server listening port

        // Start a thread to refresh and display clients' data.
        Thread displayThread = new Thread(new DisplayTask());
        displayThread.setDaemon(true); // Daemon thread exits when main thread exits.
        displayThread.start();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port + "...");
            while (true) {
                // Accept a new client.
                Socket clientSocket = serverSocket.accept();
                String clientId = "Client " + clientCounter.getAndIncrement();
                String clientIP = clientSocket.getInetAddress().toString();

                // Create a new record for this client.
                ClientData data = new ClientData(clientId, clientIP);
                clientsData.put(clientId, data);

                System.out.println("Connected: " + clientId + " (" + clientIP + ")");

                // Launch a new thread to handle communication with this client.
                new Thread(new ClientHandler(clientSocket, clientId)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

// Data holder for each client's metrics.
class ClientData {
    String clientId;
    String ip;
    double cpu;  // in percentage
    double ram;  // in percentage
    double disk; // in percentage

    public ClientData(String clientId, String ip) {
        this.clientId = clientId;
        this.ip = ip;
        this.cpu = 0;
        this.ram = 0;
        this.disk = 0;
    }
}

// Handles a single client's incoming messages.
class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientId;

    public ClientHandler(Socket socket, String clientId) {
        this.clientSocket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;
            // Continuously read lines from the client.
            while ((inputLine = in.readLine()) != null) {
                // Expecting message in format: 
                // "CPU: XX.XX%, RAM: YY.YY%, Disk: ZZ.ZZ%"
                String[] parts = inputLine.split(",");
                if (parts.length >= 3) {
                    try {
                        double cpu = Double.parseDouble(parts[0].replaceAll("[^0-9.]", ""));
                        double ram = Double.parseDouble(parts[1].replaceAll("[^0-9.]", ""));
                        double disk = Double.parseDouble(parts[2].replaceAll("[^0-9.]", ""));
                        ClientData data = J046ServidorRendimiento.clientsData.get(clientId);
                        if (data != null) {
                            data.cpu = cpu;
                            data.ram = ram;
                            data.disk = disk;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing data from " + clientId + ": " + inputLine);
                    }
                }
                // Optionally, echo the message back.
                out.println("Echo: " + inputLine);
            }
        } catch (IOException e) {
            System.out.println("Connection error with " + clientId);
            e.printStackTrace();
        } finally {
            // Remove client data when disconnected.
            J046ServidorRendimiento.clientsData.remove(clientId);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

// Periodically clears and redraws the console with clients' metrics.
class DisplayTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000); // Refresh every second.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Clear the console using ANSI escape codes.
            System.out.print("\033[H\033[2J");
            System.out.flush();

            // Display each client's data.
            for (ClientData data : J046ServidorRendimiento.clientsData.values()) {
                System.out.println(data.clientId + ": " + data.ip);
                System.out.println("CPU:  " + progressBar(data.cpu));
                System.out.println("RAM:  " + progressBar(data.ram));
                System.out.println("Disk: " + progressBar(data.disk));
                System.out.println(); // Blank line between clients.
            }
        }
    }

    // Returns a progress bar string based on the percentage (0 to 100).
    private String progressBar(double percentage) {
        final int totalBars = 10;
        int filledBars = (int) (percentage / (100.0 / totalBars));
        if (filledBars > totalBars) {
            filledBars = totalBars;
        }
        StringBuilder bar = new StringBuilder("|");
        for (int i = 0; i < filledBars; i++) {
            bar.append("#");
        }
        for (int i = filledBars; i < totalBars; i++) {
            bar.append("-");
        }
        bar.append("| -> ").append(String.format("%.2f", percentage)).append("%");
        return bar.toString();
    }
}

