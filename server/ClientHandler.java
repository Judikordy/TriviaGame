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

    private void askGameMode() throws IOException {
        out.println("Choose game mode: 1 for Single Player, 2 for Multiplayer");
        String mode = in.readLine();

        if (mode == null) {
            out.println("No input received. Defaulting to Single Player.");
            mode = "1";
        }

        switch (mode) {
            case "1":
                out.println("You chose Single Player mode.");
                out.println("Enter the desired question category (Math, Geography, History, Science): ");
                String category = in.readLine();

                if (category == null) {
                    out.println("No input received. Defaulting to Math.");
                    category = "Math";
                }

                out.println("Enter the desired question difficulty (Easy, Medium, Hard): ");
                String difficulty = in.readLine();

                if (difficulty == null) {
                    out.println("No input received. Defaulting to Easy.");
                    difficulty = "Easy";
                }

                out.println("Enter the maximum number of questions: ");
                String maxQuestions = in.readLine();

                if (maxQuestions == null) {
                    out.println("No input received. Defaulting to 5.");
                    maxQuestions = "5";
                }

                startSinglePlayer();
                break;
            case "2":
                out.println("You chose Multiplayer mode.");
                out.println("Choose 1 to create a room or 2 to join an existing room.");

                String choice = in.readLine();

                if (choice == null) {
                    out.println("No input received. Defaulting to creating a room.");
                    choice = "1";
                }
                else if (choice.equals("1")) {
                        out.println("Enter the name of the room: ");
                        String roomName = in.readLine();
                        gameManager.createRoom(roomName, true);
                    }
                    else {
                        if (choice.equals("2")) {
                            out.println("Enter the name of the room: ");
                            String roomName = in.readLine();
                            out.println("Enter the name of the team: ");
                            String teamName = in.readLine();
                            gameManager.joinRoom(roomName, teamName, currentUser);
                        }
                    }
                // Multiplayer flow can continue as before (create/join room)
                break;
            default:
                out.println("Invalid choice. Please choose 1 or 2.");
                askGameMode();
        }
    }

    private void startSinglePlayer() {
        out.println("Starting Single Player game...");
        // Implement single-player game logic here
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
                            
                            askGameMode();
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
                            
                            askGameMode();
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
                        gameManager.saveRoom(gameManager.getRoom(roomName));

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