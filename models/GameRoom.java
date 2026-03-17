package models;

import java.util.ArrayList;
import java.util.List;

import server.GameSession;

public class GameRoom {
    private String roomName;
    private List<Team> teams;
    private boolean isMultiplayer;
    private GameSession session;

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
        for (Team t : teams) {
            if (t.getName().equalsIgnoreCase(team.getName())) {
                return false;
            }
        }
        teams.add(team);
        return true;
    }

    public boolean validateEqualPlayers() {
        if (teams.isEmpty()) return true;
        int size = teams.get(0).getSize();
        for (Team t : teams) {
            if (t.getSize() != size) {
                System.out.println("All teams must have the same number of players.");
                return false;
            }
        }
        return true;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public GameSession getSession() {
        if (session == null) {
            session = new GameSession(this); // ✅ pass this room into the constructor
        }
        return session;
    }
}