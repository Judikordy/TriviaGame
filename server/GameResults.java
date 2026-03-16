package server;

import models.Question;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GameResults {

    GameSession session;

    public GameResults(GameSession session) {
        this.session = session;
    }

    public void evaluateResults() {
        if (session == null || session.getCurrentQuestion() == null) return;

        session.endQuestion();
        Question question = session.getCurrentQuestion();
        String correctAnswer = question.getAnswer().trim().toLowerCase();

        session.broadcast("Evaluating results for question: " + question.getText());

        Set<String> answeredUsers = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader("data/answers.txt"))) {
            String line;
            StringBuilder correct = new StringBuilder();
            StringBuilder incorrect = new StringBuilder();

            while ((line = br.readLine()) != null) {
                // line format: username|questionName|answer|timestamp
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String username = parts[0];
                String questionName = parts[1];
                String answer = parts[2].trim().toLowerCase();

                if (!questionName.equals(question.getText())) continue;

                if (answeredUsers.contains(username)) continue;
                answeredUsers.add(username);

                if (!answer.matches("[abcd]")) {
                    session.sendMessage(username + " submitted invalid answer: " + answer);
                    incorrect.append(username).append(" ");
                    continue;
                }

                if (answer.equals(correctAnswer)) {
                    correct.append(username).append(" ");
                } else {
                    incorrect.append(username).append(" ");
                }
            }

            session.broadcast("Correct answers: " + (correct.length() > 0 ? correct.toString() : "None"));
            session.broadcast("Incorrect answers: " + (incorrect.length() > 0 ? incorrect.toString() : "None"));

        } catch (IOException e) {
            e.printStackTrace();
            session.broadcast("Error reading answers for evaluation.");
        }
    }
}