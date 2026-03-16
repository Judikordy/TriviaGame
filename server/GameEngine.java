package server;

import models.Question;
import java.util.List;

public class GameEngine {

    private QuestionBank questionBank;
    private AuthManager authManager;
    private ScoreManager scoreManager;
    private ConfigManager configManager;

    public GameEngine() {
        this.authManager = new AuthManager();
        this.scoreManager = new ScoreManager();
        this.configManager = new ConfigManager();
        this.questionBank = new QuestionBank();
        questionBank.loadQuestions();
    }

    public void startRound(GameSession session, int questionIndex) {

        List<Question> questions = questionBank.getAllQuestions();

        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }

        if (questionIndex < 0 || questionIndex >= questions.size()) {
            System.out.println("Invalid question index.");
            return;
        }

        Question q = questions.get(questionIndex);

        session.broadcast("Question: " + q.getText());
        session.broadcast("Choices: " + q.getChoices());

        session.openQuestion();
    
        int duration = configManager.getInt("questionTimeSeconds", 15);
        session.startQuestionTimer(duration);
    }

    public boolean checkQuit(String input){
        return input.trim().equals("-");
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public QuestionBank getQuestionBank() {
        return questionBank;
    }
}