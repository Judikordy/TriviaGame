package models;

import java.util.ArrayList;
import java.util.List;

public class Score {

    private User user;
    private int points;
    private List<Integer> history; // previous game scores

    public Score(User user) {
        this.user = user;
        this.points = 0;
        this.history = new ArrayList<>();
    }

    public User getUser() {
        return user;
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int pts) {
        points += pts;
    }

    public void reset() {
        points = 0;
    }

    public void saveHistory() {
        history.add(points);
        reset();
    }

    public List<Integer> getHistory() {
        return history;
    }
}