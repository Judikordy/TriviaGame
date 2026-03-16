package server;

import java.io.*;
import java.net.Socket;
import models.User;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private AuthManager auth;
    private static GameManager gameManager = new GameManager(); // shared across all clients
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.auth = new AuthManager();
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Connected to Trivia Game Server. Use LOGIN or REGISTER.");

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(" ");
                String command = parts[0].toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (parts.length < 3) {
                            out.println("Usage: LOGIN <username> <password>");
                            break;
                        }
                        String loginResult = auth.login(parts[1], parts[2]);
                        if (loginResult.equals("200 OK")) {
                            currentUser = new User("?", parts[1], parts[2]); // name not needed here
                        }
                        out.println("Result: " + loginResult);
                        break;

                    case "REGISTER":
                        if (parts.length < 4) {
                            out.println("Usage: REGISTER <name> <username> <password>");
                            break;
                        }
                        String registerResult = auth.register(parts[1], parts[2], parts[3]);
                        if (registerResult.equals("200 OK")) {
                            currentUser = new User(parts[1], parts[2], parts[3]);
                            auth.saveUser(currentUser);
                        }
                        out.println("Result: " + registerResult);
                        break;

                    case "CREATE_ROOM":
                        if (parts.length < 2) {
                            out.println("Usage: CREATE_ROOM <roomName>");
                            break;
                        }
                        String result = gameManager.createRoom(parts[1], true); // multiplayer by default
                        out.println(result);
                        break;

                    case "JOIN_ROOM":
                        if (parts.length < 3) {
                            out.println("Usage: JOIN_ROOM <roomName> <teamName>");
                            break;
                        }
                        GameRoom room = gameManager.getRoom(parts[1]);
                        if (room == null) {
                            out.println("Room not found.");
                            break;
                        }
                        Team team = new Team(parts[2]);
                        boolean added = room.addTeam(team);
                        if (!added) {
                            out.println("Team name already exists.");
                            break;
                        }
                        if (currentUser != null) {
                            team.addPlayer(currentUser);
                        }
                        out.println("Joined room " + parts[1] + " as team " + parts[2]);
                        break;

                    case "EXIT":
                        out.println("Goodbye!");
                        socket.close();
                        return;

                    default:
                        out.println("Unknown command.");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
        }
    }
}