package server;

import models.Question;
import models.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SinglePlayerGame {

    private final User user;
    private final BufferedReader in;
    private final PrintWriter out;
    private final QuestionBank questionBank;
    private final ScoreManager scoreManager;
    private final ConfigManager configManager;

    private int score = 0;
    // track per-question result for end summary
    private final Map<String, Boolean> questionResults = new HashMap<>();

    private String category;
    private String difficulty;

    public SinglePlayerGame(User user, BufferedReader in, PrintWriter out) {
        this.user = user;
        this.in = in;
        this.out = out;
        this.questionBank = new QuestionBank();
        this.questionBank.loadQuestions();
        this.scoreManager = new ScoreManager();
        this.configManager = new ConfigManager();
    }

    public void start(String category, String difficulty, int maxQuestions) throws IOException {
        this.category   = category;
        this.difficulty = difficulty;
        List<Question> questions = questionBank.getRandomQuestions(category, difficulty, maxQuestions);

        if (questions.isEmpty()) {
            out.println("No questions found for category '" + category + "' and difficulty '" + difficulty + "'. Game cancelled.");
            return;
        }

        out.println("=== Single Player Game Started ===");
        out.println("Category: " + category + " | Difficulty: " + difficulty + " | Questions: " + questions.size());
        out.println("Type your answer (A/B/C/D) or '-' to quit.");
        out.println("==================================");

        int questionDuration = configManager.getInt("questionTimeSeconds", 15);

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            out.println("\nQuestion " + (i + 1) + "/" + questions.size() + ": " + q.getText());
            List<String> choices = q.getChoices();
            String[] labels = {"A", "B", "C", "D"};
            for (int c = 0; c < choices.size() && c < 4; c++) {
                out.println("  " + labels[c] + ") " + choices.get(c));
            }
            out.println("You have " + questionDuration + " seconds to answer.");

            AtomicBoolean answered = new AtomicBoolean(false);
            AtomicBoolean quit = new AtomicBoolean(false);
            AtomicReference<String> playerAnswer = new AtomicReference<>("");

            // Timer thread — broadcasts countdown and closes question
            Thread timerThread = new Thread(() -> {
                try {
                    int[] checkpoints = {10, 5, 4, 3, 2, 1};
                    int elapsed = 0;
                    for (int cp : checkpoints) {
                        int waitFor = questionDuration - cp - elapsed;
                        if (waitFor > 0) {
                            Thread.sleep(waitFor * 1000L);
                            elapsed += waitFor;
                        }
                        if (!answered.get()) {
                            out.println("[TIMER] " + cp + " seconds remaining!");
                        }
                    }
                    Thread.sleep(1000);
                    if (!answered.get()) {
                        answered.set(true); // force close
                        out.println("[TIMER] Time is up!");
                    }
                } catch (InterruptedException e) {
                    // question was answered, timer cancelled
                }
            });
            timerThread.setDaemon(true);
            timerThread.start();

            // Read answer from client on this thread (TCP socket read)
            while (!answered.get()) {
                if (in.ready()) {
                    String input = in.readLine();
                    if (input == null) {
                        quit.set(true);
                        answered.set(true);
                        break;
                    }
                    input = input.trim();
                    if (input.equals("-")) {
                        quit.set(true);
                        answered.set(true);
                        timerThread.interrupt();
                        break;
                    }
                    if (input.equalsIgnoreCase("A") || input.equalsIgnoreCase("B") ||
                        input.equalsIgnoreCase("C") || input.equalsIgnoreCase("D")) {
                        playerAnswer.set(input.toUpperCase());
                        answered.set(true);
                        timerThread.interrupt();
                    } else {
                        out.println("Invalid input. Please enter A, B, C, or D (or '-' to quit).");
                    }
                } else {
                    try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                }
            }

            if (quit.get()) {
                out.println("You quit the game.");
                break;
            }

            // Evaluate answer
            String given = playerAnswer.get();
            String correct = q.getAnswer().trim().toUpperCase();

            if (given.isEmpty()) {
                out.println("No answer submitted. Correct answer was: " + correct);
                questionResults.put(q.getText(), false);
            } else if (given.equals(correct)) {
                int points = pointsFor(difficulty);
                score += points;
                out.println("Correct! +" + points + " points. Your score: " + score);
                questionResults.put(q.getText(), true);
            } else {
                out.println("Wrong! You answered " + given + ". Correct answer was: " + correct + ". Your score: " + score);
                questionResults.put(q.getText(), false);
            }
        }

        showSummary();

        // Build question detail list for history
        List<Map<String, Object>> qDetails = new java.util.ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> entry = new java.util.LinkedHashMap<>();
            entry.put("question",      q.getText());
            entry.put("correctAnswer", q.getAnswer().trim().toUpperCase());
            Boolean res = questionResults.get(q.getText());
            entry.put("correct",       res != null && res);
            qDetails.add(entry);
        }

        scoreManager.addPoints(user, score);
        scoreManager.saveGameRecord(user, category, difficulty, "single", qDetails);
    }

    private int pointsFor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "hard":   return 30;
            case "medium": return 20;
            default:       return 10; // easy
        }
    }

    private void showSummary() {
        out.println("\n========== GAME OVER ==========");
        out.println("Player: " + user.getName());
        out.println("Final Score: " + score);
        out.println("--------------------------------");
        int correct = 0, wrong = 0;
        for (Map.Entry<String, Boolean> entry : questionResults.entrySet()) {
            String status = entry.getValue() ? "[OK] Correct" : "[XX] Wrong";
            out.println(status + " | " + entry.getKey());
            if (entry.getValue()) correct++; else wrong++;
        }
        out.println("--------------------------------");
        out.println("Correct: " + correct + " | Wrong: " + wrong);
        out.println("================================");
    }
}
