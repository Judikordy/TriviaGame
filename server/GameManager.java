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

    public void saveRoom(GameRoom room) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(roomsFilePath, true))) {

            writer.write(room.getRoomName() + "," + room.isMultiplayer() + "," + room.getTeams().size());

            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error saving room.");
        }
    }

    public String createRoom(String roomName, boolean isMultiplayer) {
        if (rooms.containsKey(roomName)) {
            return "Room already exists";
        }
        rooms.put(roomName, new GameRoom(roomName, isMultiplayer));
        return "Room created: " + roomName;
    }

    public GameRoom getRoom(String roomName) {
        return rooms.get(roomName);
    }

    public void removeRoom(String roomName) {
        rooms.remove(roomName);
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
}
