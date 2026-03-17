package server;

import models.GameRoom;
import models.Question;
import models.Team;
import models.User;

public class GameSession {
    private GameRoom currentRoom;
    private GameEngine engine;
    private int questionIndex = 0;
    private Question currentQuestion;
    private boolean questionActive = false;

    public GameSession(GameRoom room) {
        this.currentRoom = room;
        this.engine = new GameEngine();
    }

    // Start the game
    public void start() {
        broadcast("Game started in room " + currentRoom.getRoomName());
        nextRound();
    }

    // Run one round (one question)
    public void nextRound() {
        if (questionIndex >= engine.getQuestionBank().getAllQuestions().size()) {
            broadcast("Game over! Thanks for playing.");
            return;
        }

        currentQuestion = engine.getQuestionBank().getAllQuestions().get(questionIndex);
        broadcast("Question: " + currentQuestion.getText());
        broadcast("Choices: " + currentQuestion.getChoices());

        openQuestion();
        int duration = engine.getConfigManager().getInt("questionTimeSeconds", 15);

        // Start countdown timer
        startQuestionTimer(duration);

        // After duration, evaluate results and move to next question
        new Thread(() -> {
            try {
                Thread.sleep(duration * 1000);
                new GameResults(this).evaluateResults();
                questionIndex++;
                nextRound();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public String getQuestionName() {
        return currentQuestion != null ? currentQuestion.getText() : null;
    }

    public boolean isQuestionActive() {
        return questionActive;
    }

    public void openQuestion() {
        questionActive = true;
    }

    public void closeQuestion() {
        questionActive = false;
    }

    // Save and validate answers
    public String submitAnswer(User user, String questionName, String answer) {
        if (!questionActive) {
            return "No active question. Answer ignored.";
        }
        answer = answer.trim().toLowerCase();
        if (!answer.matches("[abcd]")) {
            return "Invalid answer. Please answer with A, B, C, or D.";
        }
        AnswerManager.saveAnswer(user.getUsername(), questionName, answer);
        return "Answer submitted: " + answer.toUpperCase();
    }

    // Broadcast to all players in the room
    public void broadcast(String message) {
        for (Team team : currentRoom.getTeams()) {
            for (User player : team.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }

    public void sendMessage(String username, String message) {
        for (Team team : currentRoom.getTeams()) {
            for (User player : team.getPlayers()) {
                if (player.getUsername().equals(username)) {
                    player.sendMessage(message);
                }
            }
        }
    }

    // Countdown timer with updates
    public void startQuestionTimer(int duration) {
        new Thread(() -> {
            try {
                if (duration > 10) {
                    Thread.sleep((duration - 10) * 1000);
                    broadcast("10 seconds remaining!");
                }
                Thread.sleep(5000);
                broadcast("5 seconds remaining!");
                Thread.sleep(5000);
                broadcast("Time is up!");
                closeQuestion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}