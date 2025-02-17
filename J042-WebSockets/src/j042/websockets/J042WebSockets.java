
package j042.websockets;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.concurrent.*;


import java.security.NoSuchAlgorithmException;

public class J042WebSockets {

    private static final int PORT = 3000;
    private static final String WEBSOCKET_GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final CopyOnWriteArraySet<ClientHandler> clients = new CopyOnWriteArraySet<>();
    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("WebSocket server started on ws://localhost:" + PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final InputStream input;
        private final OutputStream output;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        }

        @Override
        public void run() {
            try {
                // Perform WebSocket handshake
                handshake();

                clients.add(this);
                System.out.println("New client connected");

                // Continuously read and broadcast messages
                while (true) {
                    String message = readMessage();
                    if (message == null) break;
                    System.out.println("Received: " + message);
                    broadcast(message);
                }
            } catch (IOException e) {
                System.err.println("Connection error: " + e.getMessage());
            } finally {
                clients.remove(this);
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }

        private void handshake() throws IOException {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                String webSocketKey = null;

                // Read WebSocket key from headers
                while (!(line = reader.readLine()).isEmpty()) {
                    if (line.startsWith("Sec-WebSocket-Key: ")) {
                        webSocketKey = line.split(": ")[1].trim();
                    }
                }

                // Correct SHA-1 algorithm usage
                MessageDigest md = MessageDigest.getInstance("SHA-1"); // Correct name
                byte[] digest = md.digest((webSocketKey + WEBSOCKET_GUID).getBytes());
                String acceptKey = Base64.getEncoder().encodeToString(digest);

                // Send WebSocket handshake response
                String response = "HTTP/1.1 101 Switching Protocols\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Sec-WebSocket-Accept: " + acceptKey + "\r\n\r\n";
                output.write(response.getBytes());
                output.flush();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("SHA-1 algorithm not available", e);
            }
        }

        private String readMessage() throws IOException {
            byte[] header = input.readNBytes(2);
            if (header.length < 2) return null;

            int payloadLength = header[1] & 0x7F;
            if (payloadLength == 126) {
                payloadLength = ((input.read() & 0xFF) << 8) | (input.read() & 0xFF);
            } else if (payloadLength == 127) {
                // For simplicity, we are not handling extended payload (64-bit length)
                throw new IOException("Extended payloads not supported");
            }

            byte[] maskingKey = input.readNBytes(4);
            byte[] payload = input.readNBytes(payloadLength);

            for (int i = 0; i < payloadLength; i++) {
                payload[i] ^= maskingKey[i % 4];
            }

            return new String(payload);
        }

        private void sendMessage(String message) throws IOException {
            byte[] messageBytes = message.getBytes();
            ByteBuffer buffer = ByteBuffer.allocate(messageBytes.length + 2);
            buffer.put((byte) 0x81); // 0x81 = text frame
            buffer.put((byte) messageBytes.length);
            buffer.put(messageBytes);
            output.write(buffer.array());
            output.flush();
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                try {
                    client.sendMessage(message);
                } catch (IOException e) {
                    System.err.println("Failed to send message: " + e.getMessage());
                }
            }
        }
    }
    
}
