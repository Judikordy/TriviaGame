package server;

import java.util.HashMap;
import java.util.Map;

public class GameManager {
    private Map<String, GameRoom> rooms;

    public GameManager() {
        rooms = new HashMap<>();
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
}
