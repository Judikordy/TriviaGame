package server;

import java.util.ArrayList;
import java.util.List;
import models.User;

public class Team {
    private String name;
    private List<User> players;

    public Team(String name) {
        this.name = name;
        this.players = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<User> getPlayers() {
        return players;
    }

    public void addPlayer(User user) {
        players.add(user);
    }

    public int getSize() {
        return players.size();
    }
}
