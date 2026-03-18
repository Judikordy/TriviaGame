package models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Score {

    private User user;
    private int points;
    // Each entry: { "game": timestamp, "totalScore": int, "questions": [ { "question", "yourAnswer", "correct", "result" } ] }
    private List<Map<String, Object>> history;

    public Score(User user) {
        this.user    = user;
        this.points  = 0;
        this.history = new ArrayList<>();
    }

    public User getUser() { return user; }
    public int  getPoints() { return points; }

    public void addPoints(int pts) { points += pts; }
    public void reset()            { points = 0; }

    /** Save a full game record into history, then reset current points. */
    public void saveGameRecord(String category, String difficulty, String mode,
                               List<Map<String, Object>> questionDetails) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put("playedAt",   java.time.LocalDateTime.now().toString());
        record.put("mode",       mode);
        record.put("category",   category);
        record.put("difficulty", difficulty);
        record.put("totalScore", points);
        record.put("questions",  questionDetails);
        history.add(record);
        reset();
    }

    public List<Map<String, Object>> getHistory() { return history; }
}
