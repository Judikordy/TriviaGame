package server;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {
    private String roomName;
    private List<Team> teams;
    private boolean isMultiplayer;

    public GameRoom(String roomName, boolean isMultiplayer) {
        this.roomName = roomName;
        this.isMultiplayer = isMultiplayer;
        this.teams = new ArrayList<>();
    }

    public String getRoomName() {
        return roomName;
    }

    public boolean isMultiplayer() {
        return isMultiplayer;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public boolean addTeam(Team team) {
        // enforce unique team names
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(team.getName())) {
                return false; // duplicate name
            }
        }
        teams.add(team);
        return true;
    }

    public boolean validateEqualPlayers() {
        if (teams.isEmpty()) return true;
        int size = teams.get(0).getSize();
        for (Team t : teams) {
            if (t.getSize() != size) return false;
        }
        return true;
    }
}