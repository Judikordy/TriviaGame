package server;

import models.Score;
import models.User;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScoreManager implements IScoreManager {

    private Map<String, Score> scores;
    private final String SCORES_FILE = "data/scores.txt";

    public ScoreManager() {
        scores = new HashMap<>();
        loadScores();
    }

    private Score getOrCreateScore(User user) {
        return scores.computeIfAbsent(user.getUsername(), k -> new Score(user));
    }

    @Override
    public void addPoints(User user, int points) {
        Score score = getOrCreateScore(user);
        score.addPoints(points);
        saveScores();
    }

    @Override
    public int getScore(User user) {
        Score score = getOrCreateScore(user);
        return score.getPoints();
    }

    @Override
    public void resetScore(User user) {
        Score score = getOrCreateScore(user);
        score.reset();
        saveScores();
    }

    @Override
    public void saveScoreHistory(User user) {
        Score score = getOrCreateScore(user);
        score.saveHistory();
        saveScores();
    }

    public Map<String, Integer> getAllScores() {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Score> entry : scores.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getPoints());
        }
        return result;
    }

    private void saveScores() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(SCORES_FILE))) {
            for (Score score : scores.values()) {
                // Format: username|currentPoints|history1,history2,history3
                StringBuilder historyStr = new StringBuilder();
                for (int h : score.getHistory()) {
                    if (historyStr.length() > 0) historyStr.append(",");
                    historyStr.append(h);
                }
                bw.write(score.getUser().getUsername() + "|" + score.getPoints() + "|" + historyStr.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error saving scores to file.");
        }
    }

    private void loadScores() {
        File file = new File(SCORES_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Format: username|currentPoints|history1,history2,history3
                String[] parts = line.split("\\|");
                if (parts.length < 2) continue;

                String username = parts[0];
                int currentPoints = Integer.parseInt(parts[1]);
                Score score = new Score(new User("", username, "")); // dummy user object for now
                score.addPoints(currentPoints);

                if (parts.length > 2 && !parts[2].isEmpty()) {
                    String[] historyParts = parts[2].split(",");
                    for (String h : historyParts) {
                        score.getHistory().add(Integer.parseInt(h));
                    }
                }

                scores.put(username, score);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error loading scores from file.");
        }
    }
}