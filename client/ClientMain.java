package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {
        final String HOST = "localhost";
        final int PORT = 5000;

        try (Socket socket = new Socket(HOST, PORT);
             BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out    = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner    = new Scanner(System.in)) {

            // ServerListener runs on a separate thread — receives all server messages
            // (questions, timer broadcasts, results) without blocking user input
            Thread listenerThread = new Thread(new ServerListener(in));
            listenerThread.setDaemon(true);
            listenerThread.start();

            // Main thread handles user input and sends over TCP socket
            while (listenerThread.isAlive()) {
                if (scanner.hasNextLine()) {
                    String input = scanner.nextLine().trim();
                    if (input.isEmpty()) continue;
                    out.println(input);
                    if (input.equalsIgnoreCase("EXIT") || input.equals("-")) break;
                }
            }

        } catch (IOException e) {
            System.out.println("Could not connect to server at " + HOST + ":" + PORT);
            System.out.println("Make sure the server is running.");
        }
    }
}
