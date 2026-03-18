package server;

import java.io.IOException;
import java.util.*;

public class AnswerManager {

    private static final String ANSWERS_FILE = "data/answers.json";

    public static void saveAnswer(String sessionId, String username, String questionText, String answer) {
        try {
            List<Map<String, Object>> records = JsonUtil.readArray(ANSWERS_FILE);
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("sessionId", sessionId);
            r.put("username",  username);
            r.put("question",  questionText);
            r.put("answer",    answer);
            r.put("timestamp", java.time.LocalDateTime.now().toString());
            records.add(r);
            JsonUtil.writeArray(ANSWERS_FILE, records);
        } catch (IOException e) {
            System.out.println("Error saving answer: " + e.getMessage());
        }
    }

    /** Returns all answers for a specific question within a specific session. */
    public static List<Map<String, Object>> getAnswersForQuestion(String sessionId, String questionText) {
        try {
            List<Map<String, Object>> all = JsonUtil.readArray(ANSWERS_FILE);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map<String, Object> r : all)
                if (sessionId.equals(r.get("sessionId")) && questionText.equals(r.get("question")))
                    result.add(r);
            return result;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /** Returns all answers submitted by a user in a session. */
    public static Map<String, String> getUserAnswersForSession(String sessionId, String username) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            List<Map<String, Object>> all = JsonUtil.readArray(ANSWERS_FILE);
            for (Map<String, Object> r : all)
                if (sessionId.equals(r.get("sessionId")) && username.equals(r.get("username")))
                    result.put((String) r.get("question"), (String) r.get("answer"));
        } catch (IOException ignored) {}
        return result;
    }
}
