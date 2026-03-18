package server;

import models.Score;
import models.User;

import java.io.IOException;
import java.util.*;

public class ScoreManager implements IScoreManager {

    private final Map<String, Score> scores = new HashMap<>();
    private static final String SCORES_FILE = "data/scores.json";

    public ScoreManager() { loadScores(); }

    private Score getOrCreate(User user) {
        return scores.computeIfAbsent(user.getUsername(), k -> new Score(user));
    }

    @Override
    public void addPoints(User user, int points) {
        getOrCreate(user).addPoints(points);
        persistAll();
    }

    @Override
    public int getScore(User user) {
        return getOrCreate(user).getPoints();
    }

    @Override
    public void resetScore(User user) {
        getOrCreate(user).reset();
        persistAll();
    }

    @Override
    public void saveScoreHistory(User user) {
        getOrCreate(user).saveGameRecord("", "", "", new ArrayList<>());
        persistAll();
    }

    /** Save a full game record with question details for a user. */
    public void saveGameRecord(User user, String category, String difficulty,
                               String mode, List<Map<String, Object>> questionDetails) {
        getOrCreate(user).saveGameRecord(category, difficulty, mode, questionDetails);
        persistAll();
    }

    public List<Map<String, Object>> getHistory(User user) {
        return getOrCreate(user).getHistory();
    }

    public Map<String, Integer> getAllScores() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Score> e : scores.entrySet())
            result.put(e.getKey(), e.getValue().getPoints());
        return result;
    }

    private void persistAll() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Score s : scores.values()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("username", s.getUser().getUsername());
            r.put("points",   s.getPoints());
            r.put("history",  s.getHistory());
            records.add(r);
        }
        try {
            JsonUtil.writeArray(SCORES_FILE, records);
        } catch (IOException e) {
            System.out.println("Error saving scores.json: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        try {
            List<Map<String, Object>> records = JsonUtil.readArray(SCORES_FILE);
            for (Map<String, Object> r : records) {
                String username = (String) r.get("username");
                int points = r.get("points") instanceof Number ? ((Number) r.get("points")).intValue() : 0;
                Score score = new Score(new User("", username, ""));
                score.addPoints(points);

                Object hist = r.get("history");
                if (hist instanceof List) {
                    for (Object entry : (List<?>) hist) {
                        if (entry instanceof Map) {
                            score.getHistory().add((Map<String, Object>) entry);
                        }
                        // skip old integer-format history entries
                    }
                }
                scores.put(username, score);
            }
        } catch (IOException e) {
            System.out.println("scores.json not found. Starting fresh.");
        }
    }
}
