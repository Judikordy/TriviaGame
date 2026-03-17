package server;

import java.io.*;
import java.net.Socket;

import models.GameRoom;
import models.Team;
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
        this.currentUser = null;
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
                if (parts.length == 0) continue;

                String command = parts[0].toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (parts.length < 3) {
                            out.println("Usage: LOGIN <username> <password>");
                            break;
                        }
                        String loginResult = auth.login(parts[1], parts[2]);
                        if (loginResult.equals("200 OK")) {
                            currentUser = auth.getUser(parts[1]);
                            out.println("Login successful. Welcome, " + currentUser.getName() + "!");
                        } else {
                            out.println("Login failed: " + loginResult);
                        }
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
                            out.println("Registration successful. Welcome, " + currentUser.getName() + "!");
                        } else {
                            out.println("Registration failed: " + registerResult);
                        }
                        break;

                    case "CREATE_ROOM":
                        if (currentUser == null) {
                            out.println("Please LOGIN or REGISTER before playing.");
                            break;
                        }
                        if (parts.length < 2) {
                            out.println("Usage: CREATE_ROOM <roomName>");
                            break;
                        }
                        String roomName = parts[1];
                        String result = gameManager.createRoom(roomName, true);

                        if (result.startsWith("Room created")) {
                            out.println("Room '" + roomName + "' created successfully!");
                        } else {
                            out.println("Failed to create room: " + result);
                        }
                        break;

                    case "JOIN_ROOM":
                        if (currentUser == null) {
                            out.println("Please LOGIN or REGISTER before playing.");
                            break;
                        }
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

                    case "START_GAME":
                        if (parts.length < 2) {
                            out.println("Usage: START_GAME <roomName>");
                            break;
                        }
                        String startRoom = parts[1];
                        String startResult = gameManager.startSession(startRoom);
                        out.println(startResult);
                        break;

                    case "ANSWER":
                        if (parts.length < 2) {
                            out.println("Usage: ANSWER <choice>");
                            break;
                        }
                        if (currentUser == null) {
                            out.println("Please login first.");
                            break;
                        }
                        String answer = parts[1];
                        GameSession session = gameManager.getSessionForUser(currentUser);
                        if (session == null) {
                            out.println("You are not in an active game.");
                            break;
                        }
                        String feedback = session.submitAnswer(currentUser, session.getQuestionName(), answer);
                        out.println(feedback);
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

    public void sendMessage(String message) {
        out.println(message);
    }
}