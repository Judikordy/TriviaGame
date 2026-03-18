package server;

import models.Question;
import models.Team;
import models.User;

import java.util.*;

public class GameSession {

    private final String     sessionId;
    private final List<Team> teams;
    private final GameEngine engine;
    private final ScoreManager scoreManager;
    private final List<Question> questions;
    private final String category;
    private final String difficulty;

    // username -> current session score
    private final Map<String, Integer> sessionScores = new HashMap<>();
    // username -> list of per-question result maps
    private final Map<String, List<Map<String, Object>>> playerQuestionResults = new HashMap<>();

    private int      questionIndex = 0;
    private Question currentQuestion;
    private volatile boolean questionActive = false;

    public GameSession(List<Team> teams, String category, String difficulty, int maxQuestions) {
        this.sessionId    = UUID.randomUUID().toString();
        this.teams        = teams;
        this.engine       = new GameEngine();
        this.scoreManager = new ScoreManager();
        this.category     = category != null ? category : "";
        this.difficulty   = difficulty != null ? difficulty : "";

        List<Question> pool = (category != null && difficulty != null && maxQuestions > 0)
                ? engine.getQuestionBank().getRandomQuestions(category, difficulty, maxQuestions)
                : engine.getQuestionBank().getAllQuestions();
        this.questions = pool;

        for (Team t : teams)
            for (User p : t.getPlayers()) {
                sessionScores.put(p.getUsername(), 0);
                playerQuestionResults.put(p.getUsername(), new ArrayList<>());
            }
    }

    // ----------------------------------------------------------------- start

    public void start() {
        broadcast("=== Game Starting ===");
        broadcast("Session: " + sessionId);
        broadcast("Questions: " + questions.size());
        broadcast("Teams: " + teamNames());
        new Thread(() -> {
            try { Thread.sleep(2000); nextRound(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }).start();
    }

    // --------------------------------------------------------------- rounds

    public void nextRound() {
        if (questionIndex >= questions.size()) { endGame(); return; }

        currentQuestion = questions.get(questionIndex);
        int duration    = engine.getConfigManager().getInt("questionTimeSeconds", 15);

        broadcast("\nQuestion " + (questionIndex + 1) + "/" + questions.size()
                + " [" + currentQuestion.getCategory() + " | " + currentQuestion.getDifficulty() + "]");
        broadcast(currentQuestion.getText());

        String[] labels = {"A", "B", "C", "D"};
        List<String> choices = currentQuestion.getChoices();
        for (int i = 0; i < choices.size() && i < 4; i++)
            broadcast("  " + labels[i] + ") " + choices.get(i));

        broadcast("You have " + duration + " seconds. Type: ANSWER <A/B/C/D>");
        questionActive = true;

        final int qIdx = questionIndex;
        new Thread(() -> {
            try {
                broadcastCountdown(duration);
                questionActive = false;
                broadcast("[TIME'S UP]");
                new GameResults(this).evaluateResults();
                questionIndex++;
                Thread.sleep(2000);
                nextRound();
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "timer-q" + qIdx).start();
    }

    private void broadcastCountdown(int duration) throws InterruptedException {
        int[] checkpoints = {15, 10, 5, 4, 3, 2, 1};
        int elapsed = 0;
        for (int cp : checkpoints) {
            if (cp >= duration) continue;
            int wait = duration - cp - elapsed;
            if (wait > 0) { Thread.sleep(wait * 1000L); elapsed += wait; }
            if (questionActive) broadcast("[TIMER] " + cp + " seconds remaining!");
        }
        Thread.sleep(1000);
    }

    // ---------------------------------------------------------------- end game

    private void endGame() {
        new GameResults(this).showFinalScoreboard(teams);

        // Save detailed score history per player
        for (Team team : teams) {
            for (User player : team.getPlayers()) {
                int earned = sessionScores.getOrDefault(player.getUsername(), 0);
                scoreManager.addPoints(player, earned);
                List<Map<String, Object>> qDetails = playerQuestionResults.getOrDefault(
                        player.getUsername(), new ArrayList<>());
                scoreManager.saveGameRecord(player, category, difficulty, "multiplayer", qDetails);
            }
        }

        broadcast("Scores saved. Thanks for playing!");
        GameManager.instance().removeSession(this);
    }

    // ----------------------------------------------------------- score helpers

    public void addScore(String username, int points) {
        sessionScores.merge(username, points, Integer::sum);
    }

    public int getScore(String username) {
        return sessionScores.getOrDefault(username, 0);
    }

    /** Record per-question result for a player. */
    public void recordQuestionResult(String username, String questionText,
                                     String yourAnswer, String correctAnswer,
                                     boolean correct, int pointsEarned) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("question",      questionText);
        entry.put("yourAnswer",    yourAnswer);
        entry.put("correctAnswer", correctAnswer);
        entry.put("correct",       correct);
        entry.put("pointsEarned",  pointsEarned);
        playerQuestionResults.computeIfAbsent(username, k -> new ArrayList<>()).add(entry);
    }

    public List<Map<String, Object>> getPlayerQuestionResults(String username) {
        return playerQuestionResults.getOrDefault(username, new ArrayList<>());
    }

    public void broadcastIndividualScores() {
        for (Team team : teams)
            for (User player : team.getPlayers())
                player.sendMessage("[SCORE] " + player.getName() + ": "
                        + sessionScores.getOrDefault(player.getUsername(), 0) + " pts");
    }

    // ----------------------------------------------------------- answer submit

    public String submitAnswer(User user, String questionText, String answer) {
        if (!questionActive) return "No active question. Answer ignored.";
        answer = answer.trim().toUpperCase();
        if (!answer.matches("[ABCD]")) return "Invalid answer. Please enter A, B, C, or D.";
        AnswerManager.saveAnswer(sessionId, user.getUsername(), questionText, answer);
        return "Answer submitted: " + answer;
    }

    // ---------------------------------------------------------------- getters

    public String     getSessionId()      { return sessionId; }
    public Question   getCurrentQuestion(){ return currentQuestion; }
    public String     getQuestionName()   { return currentQuestion != null ? currentQuestion.getText() : null; }
    public boolean    isQuestionActive()  { return questionActive; }
    public List<Team> getTeams()          { return teams; }
    public void       closeQuestion()     { questionActive = false; }

    // ------------------------------------------------------------ broadcast

    public void broadcast(String message) {
        for (Team team : teams)
            for (User player : team.getPlayers())
                player.sendMessage(message);
    }

    private String teamNames() {
        StringBuilder sb = new StringBuilder();
        for (Team t : teams) { if (sb.length() > 0) sb.append(" vs "); sb.append(t.getName()); }
        return sb.toString();
    }
}
