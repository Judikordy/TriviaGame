package client;

import java.io.BufferedReader;
import java.io.IOException;

public class ServerListener implements Runnable {

    private BufferedReader in;

    public ServerListener(BufferedReader in) {
        this.in = in;
    }

    @Override
    public void run() {

        try {
            String message;

            while ((message = in.readLine()) != null) {
                System.out.println("\n[SERVER] " + message);
                System.out.print("> ");
            }

        } catch (IOException e) {
            System.out.println("Disconnected from server.");
        }
    }
}