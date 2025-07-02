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