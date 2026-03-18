package server;

import models.Question;
import models.Team;
import models.User;

import java.util.*;

public class GameResults {

    private final GameSession session;

    public GameResults(GameSession session) { this.session = session; }

    public void evaluateResults() {
        if (session == null || session.getCurrentQuestion() == null) return;
        session.closeQuestion();

        Question question    = session.getCurrentQuestion();
        String correctAnswer = question.getAnswer().trim().toUpperCase();
        int    points        = pointsFor(question.getDifficulty());

        session.broadcast("--- Results: " + question.getText() + " ---");
        session.broadcast("Correct answer: " + correctAnswer);

        List<Map<String, Object>> answers =
                AnswerManager.getAnswersForQuestion(session.getSessionId(), question.getText());

        Set<String>  seen      = new HashSet<>();
        List<String> correct   = new ArrayList<>();
        List<String> incorrect = new ArrayList<>();

        for (Map<String, Object> r : answers) {
            String username = (String) r.get("username");
            String answer   = ((String) r.get("answer")).trim().toUpperCase();
            if (seen.contains(username)) continue;
            seen.add(username);
            if (answer.matches("[ABCD]") && answer.equals(correctAnswer)) {
                correct.add(username);
                session.addScore(username, points);
                session.recordQuestionResult(username, question.getText(), answer, correctAnswer, true, points);
            } else {
                incorrect.add(username);
                session.recordQuestionResult(username, question.getText(), answer, correctAnswer, false, 0);
            }
        }

        // Players who did not answer
        for (Team team : session.getTeams())
            for (User p : team.getPlayers())
                if (!seen.contains(p.getUsername()))
                    session.recordQuestionResult(p.getUsername(), question.getText(), "-", correctAnswer, false, 0);

        session.broadcast("Correct (+" + points + " pts): " + (correct.isEmpty()   ? "None" : String.join(", ", correct)));
        session.broadcast("Incorrect:              "        + (incorrect.isEmpty() ? "None" : String.join(", ", incorrect)));
        session.broadcastIndividualScores();
    }

    public void showFinalScoreboard(List<Team> teams) {
        session.broadcast("\n+==============================+");
        session.broadcast("|       FINAL SCOREBOARD       |");
        session.broadcast("+==============================+");

        String winnerTeam  = null;
        int    winnerScore = -1;

        for (Team team : teams) {
            int teamTotal = 0;
            session.broadcast("\n  Team: " + team.getName());
            session.broadcast("  ------------------------------");

            for (User p : team.getPlayers()) {
                int score = session.getScore(p.getUsername());
                teamTotal += score;
                session.broadcast("    " + p.getName() + " (" + p.getUsername() + "): " + score + " pts");

                List<Map<String, Object>> qResults = session.getPlayerQuestionResults(p.getUsername());
                if (qResults != null && !qResults.isEmpty()) {
                    for (Map<String, Object> qr : qResults) {
                        boolean ok = Boolean.TRUE.equals(qr.get("correct"));
                        session.broadcast("      [" + (ok ? "OK" : "XX") + "] "
                                + qr.get("question")
                                + " | Your answer: " + qr.get("yourAnswer")
                                + " | Correct: "     + qr.get("correctAnswer"));
                    }
                }
            }

            session.broadcast("  Team Total: " + teamTotal + " pts");
            if (teamTotal > winnerScore) { winnerScore = teamTotal; winnerTeam = team.getName(); }
        }

        session.broadcast("\n------------------------------");
        session.broadcast("  WINNER: " + (winnerTeam != null ? winnerTeam : "Draw") + " with " + winnerScore + " pts");
        session.broadcast("------------------------------\n");
    }

    private int pointsFor(String difficulty) {
        switch (difficulty.toLowerCase()) {
            case "hard":   return 30;
            case "medium": return 20;
            default:       return 10;
        }
    }
}