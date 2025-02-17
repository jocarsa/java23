
package j045.socketcliente;

import java.io.*;
import java.net.*;
public class J045SocketCliente {

    public static void main(String[] args) {
        String host = "192.168.1.37"; // Change to the server's IP address if needed
        int port = 5000; // Must match the server's port
        
        try (Socket socket = new Socket(host, port);
             BufferedReader socketIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter socketOut = new PrintWriter(
                     socket.getOutputStream(), true);
             // For reading input from the console
             BufferedReader consoleIn = new BufferedReader(
                     new InputStreamReader(System.in))) {
            
            System.out.println("Connected to server on " + host + ":" + port);
            String userInput;
            
            // Read user input from the console and send it to the server
            while ((userInput = consoleIn.readLine()) != null) {
                socketOut.println(userInput);
                // Read and display the server's response
                String response = socketIn.readLine();
                System.out.println("Server response: " + response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
