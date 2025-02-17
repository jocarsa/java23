
package j044.sockets;
import java.io.*;
import java.net.*;

public class J044Sockets {

   
   public static void main(String[] args) {
        int port = 5000; // Server will listen on this port
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server listening on port " + port + "...");
            
            // Wait for a client connection
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            
            // Set up input and output streams for the socket
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            
            String inputLine;
            // Read lines from the client until the connection is closed
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                // Echo the received message back to the client
                out.println("Echo: " + inputLine);
            }
            
            System.out.println("Client disconnected.");
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
