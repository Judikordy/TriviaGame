package server;

import models.Question;
import java.util.Timer;
import java.util.TimerTask;

public class GameEngine {
    private QuestionBank questionBank;

    public GameEngine() {
        this.questionBank = new QuestionBank();
        questionBank.loadQuestions();
    }

    public void startRound(GameRoom room) {
        Question q = questionBank.getRandomQuestion();
        if (q == null) {
            System.out.println("No questions available.");
            return;
        }

        // Broadcast question to all teams/players
        System.out.println("Question: " + q.getText());
        System.out.println("Choices: " + q.getChoices());

        // Start 15s timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            int countdown = 15;

            @Override
            public void run() {
                if (countdown == 0) {
                    System.out.println("Time's up!");
                    timer.cancel();
                } else {
                    if (countdown == 15 || countdown == 10 || countdown == 5) {
                        System.out.println("Countdown: " + countdown + " seconds left");
                    }
                    countdown--;
                }
            }
        }, 0, 1000);
    }
}
