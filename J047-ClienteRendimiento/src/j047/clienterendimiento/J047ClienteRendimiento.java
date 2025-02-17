
package j047.clienterendimiento;

import java.io.*;
import java.net.*;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class J047ClienteRendimiento {

    public static void main(String[] args) {
        String host = "192.168.1.37"; // Change to your server's IP if needed.
        int port = 5000;            // Must match the server's port.
        
        try (Socket socket = new Socket(host, port);
             BufferedReader socketIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter socketOut = new PrintWriter(
                     socket.getOutputStream(), true)) {

            System.out.println("Connected to server on " + host + ":" + port);

            // Obtain the OS bean for system metrics.
            OperatingSystemMXBean osBean = (OperatingSystemMXBean)
                    ManagementFactory.getOperatingSystemMXBean();

            // For disk usage, adjust the partition if necessary.
            // On Windows, typically "C:\\"; on Linux/macOS, use "/" instead.
            File diskPartition = new File("C:\\");
            
            // Allow a brief delay so the OS bean can gather valid metrics.
            Thread.sleep(2000);

            while (true) {
                // --- CPU Usage ---
                // getSystemCpuLoad() returns a value between 0.0 and 1.0.
                double cpuLoad = osBean.getSystemCpuLoad();
                double cpuUsagePercent = (cpuLoad < 0 ? 0 : cpuLoad * 100);

                // --- RAM Usage ---
                long totalMemory = osBean.getTotalPhysicalMemorySize();
                long freeMemory = osBean.getFreePhysicalMemorySize();
                double ramUsagePercent = ((double) (totalMemory - freeMemory) / totalMemory) * 100;

                // --- Disk Usage ---
                long totalSpace = diskPartition.getTotalSpace();
                long freeSpace = diskPartition.getFreeSpace();
                double diskUsagePercent = totalSpace > 0
                        ? (1 - ((double) freeSpace / totalSpace)) * 100
                        : 0;

                // Format the message to send to the server.
                String message = String.format("CPU: %.2f%%, RAM: %.2f%%, Disk: %.2f%%",
                        cpuUsagePercent, ramUsagePercent, diskUsagePercent);
                socketOut.println(message);

                // Optionally, print the server's echo response.
                String response = socketIn.readLine();
                if (response != null) {
                    System.out.println("Server response: " + response);
                }

                // Wait for 1 second before sending the next update.
                Thread.sleep(1000);
            }
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
