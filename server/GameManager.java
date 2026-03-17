package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.GameRoom;
import models.Team;
import models.User;

public class GameManager {
    private Map<String, GameRoom> rooms;
    private final String roomsFilePath = "data/rooms.txt";

    public GameManager() {
        rooms = new HashMap<>();
        loadRooms();
    }

    // Load rooms from file
    public void loadRooms() {
        try (BufferedReader reader = new BufferedReader(new FileReader(roomsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String name = parts[0].trim();
                    boolean isMultiplayer = Boolean.parseBoolean(parts[1].trim());
                    GameRoom room = new GameRoom(name, isMultiplayer);
                    rooms.put(name, room);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("rooms.txt file not found. Starting with an empty room list.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save a room to file
    public void saveRoom(GameRoom room) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(roomsFilePath, true))) {
            writer.write(room.getRoomName() + "," + room.isMultiplayer());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving room: " + e.getMessage());
        }
    }

    // Create a new room
    public String createRoom(String roomName, boolean isMultiplayer) {
        if (rooms.containsKey(roomName)) {
            return "Room already exists.";
        }
        GameRoom room = new GameRoom(roomName, isMultiplayer);
        rooms.put(roomName, room);
        saveRoom(room); // ✅ persist to file
        return "Room created: " + roomName;
    }

    public GameRoom getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public void removeRoom(String roomName) {
        rooms.remove(roomName);
        // (Optional) rewrite rooms.txt without this room
    }

    // Join a room and create team if needed
    public String joinRoom(String roomName, String teamName, User user) {
        GameRoom room = rooms.get(roomName);
        if (room == null) {
            return "Room not found.";
        }

        Team team = null;
        for (Team t : room.getTeams()) {
            if (t.getName().equalsIgnoreCase(teamName)) {
                team = t;
                break;
            }
        }

        if (team == null) {
            team = new Team(teamName);
            room.addTeam(team);
        }

        team.addPlayer(user);
        return "Joined room " + roomName + " as team " + teamName;
    }

    public GameSession getSessionForUser(User user) {
        for (GameRoom room : rooms.values()) {
            for (Team team : room.getTeams()) {
                for (User player : team.getPlayers()) {
                    if (player.equals(user)) {
                        return room.getSession();
                    }
                }
            }
        }
        return null;
    }

    public String startSession(String roomName) {
        GameRoom room = rooms.get(roomName);
        if (room == null) {
            return "Room not found.";
        }
        if (room.getTeams().size() < 2) {
            return "Need at least 2 teams to start the game.";
        }
        if (room.getSession() != null) {
            return "Game already in progress for room " + roomName;
        }

        GameSession session = new GameSession(room);
        room.setSession(session);
        session.start();
        return "Game started in room " + roomName + ".";
    }

    public String stopSession(String roomName) {
        GameRoom room = rooms.get(roomName);
        if (room == null || room.getSession() == null) {
            return "No active session for room " + roomName;
        }
        room.setSession(null);
        return "Game session ended for room " + roomName;
    }
}
