// ChatServer.java
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static ExecutorService pool = Executors.newFixedThreadPool(10); // Adjust pool size as needed

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                pool.execute(clientHandler); // Execute client handler in a thread from the pool
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    // Method to broadcast messages to all connected clients
    public static void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) { // Don't send the message back to the sender
                client.sendMessage(message);
            }
        }
    }

    // Method to remove a disconnected client
    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client disconnected: " + client.getSocket());
    }
}
// ChatClient.java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_IP = "127.0.0.1"; // Loopback address for local testing
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
             BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true); // auto-flush
             Scanner userInput = new Scanner(System.in)) {

            System.out.print("Enter your name: ");
            String userName = userInput.nextLine();
            serverWriter.println(userName); // Send name to server

            // Thread to continuously read messages from the server
            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = serverReader.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            });
            readerThread.start();

            // Main thread to continuously send messages to the server
            System.out.println("You can now start typing messages. Type 'exit' to quit.");
            String userMessage;
            while (true) {
                userMessage = userInput.nextLine();
                if (userMessage.equalsIgnoreCase("exit")) {
                    break;
                }
                serverWriter.println(userMessage);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        } finally {
            System.out.println("Disconnected from chat server.");
        }
    }
}
