package server;

import models.GameRoom;
import models.Question;
import models.Team;
import models.User;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GameSession {

    ClientHandler clientHandler;
    ScoreManager scoreManager;
    public GameRoom currentRoom;
    private boolean questionActive = false;
    private Question currentQuestion;
    private final String ANSWERS_FILE = "data/answers.txt";

    public void setHandler(ClientHandler handler) {
        this.clientHandler = handler;
    }

    public void sendMessage(String message) {
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }

    public void broadcast(String message) {
        for (Team team : currentRoom.teams) {
            for (User player : team.getPlayers()) {
                player.sendMessage(message);
            }
        }
    }

     public String getQuestionName() {
        if (currentQuestion != null) {
            return currentQuestion.getText();
        }
        return null;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public boolean isQuestionActive() {
        return questionActive;
    }

    public void openQuestion() {
        questionActive = true;
    }

    public void closeQuestion(){
        questionActive = false;
    }

    public void startQuestion(Question question, int duration) {
        this.currentQuestion = question;
        openQuestion();
        startQuestionTimer(duration);
    }

    public void endQuestion() {
        closeQuestion();

    }

    public void submitAnswer(User user, String questionName, String answer) {
        if (!questionActive) {
            sendMessage("No active question. Answer ignored.");
            return;
        }

        answer = answer.trim().toLowerCase();

        if (!answer.matches("[abcd]")) {
            sendMessage("Invalid answer. Please answer with A, B, C, or D.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ANSWERS_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length < 4) continue;

                String username = parts[0];
                String qName = parts[1];

                if (username.equals(user.getUsername()) && qName.equals(questionName)) {
                    sendMessage("You have already answered this question. Ignored.");
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage("Error checking previous answers. Try again.");
            return;
        }

        String lineToWrite = user.getUsername() + "|" + questionName + "|" + answer + "|" + java.time.LocalDateTime.now();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ANSWERS_FILE, true))) {
            bw.write(lineToWrite);
            bw.newLine();
            bw.flush();
            sendMessage("Answer submitted: " + answer.toUpperCase());
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage("Error saving your answer. Try again.");
        }
    }

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


    public static void main(String[] args) {
    GameSession session = new GameSession();
    session.openQuestion(); // mark question as active
    System.out.println("Question started!");
    
    session.startQuestionTimer(15);

    // Keep main alive until the thread finishes
    try {
        Thread.sleep(16000); // 16 seconds to see all messages
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}
}