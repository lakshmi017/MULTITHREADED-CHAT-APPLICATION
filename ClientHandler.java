// ClientHandler.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientName; // To identify the client

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        try {
            this.reader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.writer = new PrintWriter(this.clientSocket.getOutputStream(), true); // true for auto-flush
        } catch (IOException e) {
            System.err.println("Error setting up client handler: " + e.getMessage());
        }
    }

    public Socket getSocket() {
        return this.clientSocket;
    }

    public void sendMessage(String message) {
        this.writer.println(message);
    }

    @Override
    public void run() {
        try {
            // First message from client should be their name
            this.clientName = this.reader.readLine();
            System.out.println(this.clientName + " has joined the chat.");
            ChatServer.broadcastMessage(this.clientName + " has joined the chat.", this);

            String message;
            while ((message = this.reader.readLine()) != null) {
                System.out.println(this.clientName + ": " + message);
                ChatServer.broadcastMessage(this.clientName + ": " + message, this);
            }
        } catch (IOException e) {
            System.err.println("ClientHandler error for " + this.clientName + ": " + e.getMessage());
        } finally {
            try {
                ChatServer.broadcastMessage(this.clientName + " has left the chat.", this);
                ChatServer.removeClient(this);
                if (this.reader != null) this.reader.close();
                if (this.writer != null) this.writer.close();
                if (this.clientSocket != null) this.clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client resources: " + e.getMessage());
            }
        }
    }
}