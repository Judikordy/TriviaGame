package server;

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

    // GameSession now manages its own round loop and timer.
    // This method is kept for any external callers but is no longer used internally.
    public void startRound(GameSession session, int questionIndex) {
        session.nextRound();
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