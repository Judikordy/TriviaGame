package server;

import client.MenuHandler;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import models.Team;
import models.User;

public class ClientHandler implements Runnable {

    private final Socket      socket;
    private BufferedReader    in;
    private PrintWriter       out;
    private final AuthManager auth;
    private final GameManager gm = GameManager.instance();
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.auth   = new AuthManager();
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            handleAuth();
        } catch (IOException e) {
            System.out.println("Client disconnected: " + socket);
        } finally {
            if (currentUser != null) gm.removePlayer(currentUser);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleAuth() throws IOException {
        while (currentUser == null) {
            out.println(MenuHandler.authMenu());
            String c = readLine();
            if (c == null || c.equals("0")) { out.println("Goodbye!"); return; }
            switch (c) {
                case "1": doLogin();    break;
                case "2": doRegister(); break;
                default:  out.println("Invalid choice. Enter 1 or 2.");
            }
        }
        handleGameMode();
    }

    private void doLogin() throws IOException {
        out.println("Enter username:"); String u = readLine();
        out.println("Enter password:"); String p = readLine();
        if (u == null || p == null) return;
        String r = auth.login(u, p);
        if (r.equals("200 OK")) {
            currentUser = auth.getUser(u);
            out.println("Login successful. Welcome, " + currentUser.getName() + "!");
        } else {
            out.println("Login failed: " + r);
        }
    }

    private void doRegister() throws IOException {
        out.println("Enter your full name:"); String name = readLine();
        out.println("Enter username:");       String u    = readLine();
        out.println("Enter password:");       String p    = readLine();
        if (name == null || u == null || p == null) return;
        String r = auth.register(name, u, p);
        if (r.equals("200 OK")) {
            currentUser = new User(name, u, p);
            auth.saveUser(currentUser);
            out.println("Registered! Welcome, " + currentUser.getName() + "!");
        } else {
            out.println("Registration failed: " + r);
        }
    }

    private void handleGameMode() throws IOException {
        out.println(MenuHandler.gameModeMenu());
        String c = readLine();
        if (c == null || c.equals("0")) { out.println("Goodbye!"); return; }
        switch (c) {
            case "1": handleSinglePlayer(); break;
            case "2": handleMultiplayer();  break;
            case "3": handleScores();       break;
            default:  out.println("Invalid choice."); handleGameMode();
        }
    }

    private void handleSinglePlayer() throws IOException {
        out.println("\n--- Single Player Setup ---");

        out.println(MenuHandler.categoryMenu());
        String cc = readLine();
        if (cc == null || cc.equals("0")) { out.println("Goodbye!"); return; }
        String category = MenuHandler.categoryFromChoice(cc);
        if (category == null) {
            out.println("Invalid category.");
            handleSinglePlayer();
            return;
        }

        out.println(MenuHandler.difficultyMenu());
        String dc = readLine();
        if (dc == null || dc.equals("0")) { out.println("Goodbye!"); return; }
        String difficulty = MenuHandler.difficultyFromChoice(dc);
        if (difficulty == null) {
            out.println("Invalid difficulty.");
            handleSinglePlayer();
            return;
        }

        out.println("Enter number of questions (1-20):");
        String ms = readLine();
        if (ms == null || ms.equals("0")) { out.println("Goodbye!"); return; }
        int maxQ = 5;
        try {
            maxQ = Integer.parseInt(ms);
            if (maxQ <= 0 || maxQ > 20) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            out.println("Invalid. Defaulting to 5.");
            maxQ = 5;
        }

        new SinglePlayerGame(currentUser, in, out).start(category, difficulty, maxQ);
        out.println("\nPlay again?");
        handleGameMode();
    }

    private void handleMultiplayer() throws IOException {
        out.println(MenuHandler.multiplayerMenu());
        String c = readLine();
        if (c == null || c.equals("0")) { handleGameMode(); return; }
        switch (c) {
            case "1": createTeam(); break;
            case "2": joinTeam();   break;
            default:  out.println("Invalid choice."); handleMultiplayer();
        }
    }

    private void createTeam() throws IOException {
        int maxPlayers = gm.getMaxPlayersPerTeam();

        out.println("Enter your team name:");
        String teamName = readLine();
        if (teamName == null) return;

        if (gm.teamExists(teamName.trim())) {
            out.println("That team name already exists. Use 'Join team' to join it.");
            handleMultiplayer();
            return;
        }

        out.println(MenuHandler.categoryMenu());
        String cc = readLine();
        if (cc == null || cc.equals("0")) { out.println("Goodbye!"); return; }
        String category = MenuHandler.categoryFromChoice(cc);
        if (category == null) { out.println("Invalid category."); createTeam(); return; }

        out.println(MenuHandler.difficultyMenu());
        String dc = readLine();
        if (dc == null || dc.equals("0")) { out.println("Goodbye!"); return; }
        String difficulty = MenuHandler.difficultyFromChoice(dc);
        if (difficulty == null) { out.println("Invalid difficulty."); createTeam(); return; }

        out.println("Enter number of questions (1-20):");
        String ms = readLine();
        if (ms == null || ms.equals("0")) { out.println("Goodbye!"); return; }
        int maxQ = 5;
        try {
            maxQ = Integer.parseInt(ms);
            if (maxQ <= 0 || maxQ > 20) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            out.println("Invalid. Defaulting to 5.");
            maxQ = 5;
        }

        Team team = gm.registerPlayer(
                currentUser, teamName.trim(), category, difficulty, maxQ);
        currentUser.setHandler(this);

        int current = team.getSize();
        out.println("Team '" + team.getName() + "' created! ("
                + current + "/" + maxPlayers + " players)");
        out.println("Settings: " + category + " | " + difficulty + " | " + maxQ + " questions");

        if (current < maxPlayers) {
            out.println("Waiting for teammates to join...");
        } else {
            out.println("Team is full! Searching for opponent...");
            gm.onHandlerSet(currentUser, category, difficulty, maxQ);
        }

        waitForGame();
    }

    private void joinTeam() throws IOException {
        out.println("Enter the team name to join:");
        String teamName = readLine();
        if (teamName == null) return;

        GameManager.TeamInfo info = gm.getTeamInfo(teamName.trim());
        if (info == null) {
            out.println("Team '" + teamName + "' not found. Create it first.");
            handleMultiplayer();
            return;
        }

        int maxPlayers = gm.getMaxPlayersPerTeam();
        if (info.team.getSize() >= maxPlayers) {
            out.println("Team '" + teamName + "' is already full.");
            handleMultiplayer();
            return;
        }

        Team team = gm.registerPlayer(currentUser, teamName.trim(),
                info.category, info.difficulty, info.maxQuestions);
        currentUser.setHandler(this);

        int current = team.getSize();
        out.println("Joined team '" + team.getName() + "' ("
                + current + "/" + maxPlayers + " players).");
        out.println("Settings: " + info.category + " | "
                + info.difficulty + " | " + info.maxQuestions + " questions");

        if (current >= maxPlayers) {
            out.println("Team is full! Searching for opponent...");
            gm.onHandlerSet(currentUser, info.category, info.difficulty, info.maxQuestions);
        } else {
            out.println("Waiting for more teammates...");
        }

        waitForGame();
    }

    private void waitForGame() throws IOException {
        String line;
        while ((line = readLine()) != null) {
            if (line.equals("0") || line.equals("-")) {
                out.println("Goodbye!");
                return;
            }

            String upper = line.toUpperCase();

            if (upper.matches("[ABCD]")) {
                GameSession session = gm.getSessionForUser(currentUser);
                if (session == null) {
                    out.println("Game hasn't started yet. Please wait.");
                    continue;
                }
                if (!session.isQuestionActive()) {
                    out.println("No active question right now.");
                    continue;
                }
                out.println(session.submitAnswer(
                        currentUser, session.getQuestionName(), upper));
                continue;
            }

            if (upper.startsWith("ANSWER ")) {
                String[] parts = upper.split(" ", 2);
                if (parts.length >= 2 && parts[1].trim().matches("[ABCD]")) {
                    GameSession session = gm.getSessionForUser(currentUser);
                    if (session == null) {
                        out.println("Game hasn't started yet. Please wait.");
                        continue;
                    }
                    if (!session.isQuestionActive()) {
                        out.println("No active question right now.");
                        continue;
                    }
                    out.println(session.submitAnswer(
                            currentUser, session.getQuestionName(), parts[1].trim()));
                    continue;
                }
            }

            out.println("Waiting... | Type A/B/C/D to answer | 0 to quit");
        }
    }

    private void handleScores() throws IOException {
        out.println(MenuHandler.scoresMenu());
        String c = readLine();
        if (c == null || c.equals("0")) { handleGameMode(); return; }
        switch (c) {
            case "1": showScoreHistory("single");      break;
            case "2": showScoreHistory("multiplayer"); break;
            default:  out.println("Invalid choice."); handleScores(); return;
        }
        handleGameMode();
    }

    private void showScoreHistory(String mode) {
        ScoreManager sm = new ScoreManager();
        List<Map<String, Object>> history = sm.getHistory(currentUser);

        List<Map<String, Object>> filtered = new java.util.ArrayList<>();
        for (Map<String, Object> record : history) {
            if (mode.equals(record.get("mode"))) filtered.add(record);
        }

        String label = mode.equals("single") ? "Single Player" : "Multiplayer";
        out.println("\n=== " + label + " Score History ===");
        out.println("Player: " + currentUser.getName()
                + " (" + currentUser.getUsername() + ")");

        if (filtered.isEmpty()) {
            out.println("No " + label + " games played yet.");
            return;
        }

        int gameNum = 1;
        for (Map<String, Object> record : filtered) {
            out.println("\n  Game #" + gameNum++);
            out.println("  Date      : " + record.getOrDefault("playedAt", "N/A"));
            out.println("  Category  : " + record.getOrDefault("category", "N/A"));
            out.println("  Difficulty: " + record.getOrDefault("difficulty", "N/A"));
            out.println("  Score     : " + record.getOrDefault("totalScore", 0) + " pts");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> questions =
                    (List<Map<String, Object>>) record.get("questions");
            if (questions != null && !questions.isEmpty()) {
                out.println("  Questions :");
                for (Map<String, Object> q : questions) {
                    boolean ok = Boolean.TRUE.equals(q.get("correct"));
                    String yourAnswer = (String) q.getOrDefault("yourAnswer", "-");
                    out.println("    [" + (ok ? "OK" : "XX") + "] "
                            + q.get("question")
                            + " | Your answer: " + yourAnswer
                            + " | Correct: "     + q.get("correctAnswer"));
                }
            }
        }
        out.println("\n----------------------------------");
    }

    private String readLine() throws IOException {
        String line = in.readLine();
        return line != null ? line.trim() : null;
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }
}