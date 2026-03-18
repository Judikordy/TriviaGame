package server;

import models.Question;

import java.io.IOException;
import java.util.*;

public class QuestionBank implements IQuestionBank {

    private final List<Question> questions = new ArrayList<>();
    public static final String QUESTIONS_FILE = "data/questions.json";

    public QuestionBank() {}

    @Override
    public void loadQuestions() {
        try {
            List<Map<String, Object>> records = JsonUtil.readArray(QUESTIONS_FILE);
            for (Map<String, Object> r : records) {
                int    id         = (int)    r.getOrDefault("id", 0);
                String text       = (String) r.get("text");
                String category   = (String) r.get("category");
                String difficulty = (String) r.get("difficulty");
                String answer     = (String) r.get("answer");

                @SuppressWarnings("unchecked")
                List<Object> rawChoices = (List<Object>) r.get("choices");
                List<String> choices = new ArrayList<>();
                if (rawChoices != null) for (Object c : rawChoices) choices.add(c.toString());

                questions.add(new Question(id, text, category, difficulty, choices, answer));
            }
            System.out.println("Loaded " + questions.size() + " questions.");
        } catch (IOException e) {
            System.out.println("questions.json not found: " + e.getMessage());
        }
    }

    @Override public void addQuestion(Question q)    { questions.add(q); }
    @Override public void removeQuestion(Question q) { questions.remove(q); }

    @Override
    public void saveQuestion(Question q) {
        questions.add(q);
        persistAll();
    }

    @Override
    public List<Question> getAllQuestions() { return new ArrayList<>(questions); }

    @Override
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        return questions.get((int)(Math.random() * questions.size()));
    }

    @Override
    public List<Question> getQuestionByCategory(String category) {
        List<Question> r = new ArrayList<>();
        for (Question q : questions)
            if (q.getCategory().equalsIgnoreCase(category)) r.add(q);
        return r;
    }

    @Override
    public List<Question> getQuestionByDifficulty(String difficulty) {
        List<Question> r = new ArrayList<>();
        for (Question q : questions)
            if (q.getDifficulty().equalsIgnoreCase(difficulty)) r.add(q);
        return r;
    }

    @Override
    public List<Question> getQuestionsByCategoryAndDifficulty(String category, String difficulty) {
        List<Question> r = new ArrayList<>();
        for (Question q : questions)
            if (q.getCategory().equalsIgnoreCase(category) && q.getDifficulty().equalsIgnoreCase(difficulty))
                r.add(q);
        return r;
    }

    @Override
    public List<Question> getRandomQuestions(String category, String difficulty, int n) {
        List<Question> filtered = getQuestionsByCategoryAndDifficulty(category, difficulty);
        Collections.shuffle(filtered);
        return filtered.size() > n ? filtered.subList(0, n) : filtered;
    }

    public Question getQuestionById(int id) {
        for (Question q : questions) if (q.getId() == id) return q;
        return null;
    }

    private void persistAll() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (Question q : questions) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("id",         q.getId());
            r.put("text",       q.getText());
            r.put("category",   q.getCategory());
            r.put("difficulty", q.getDifficulty());
            r.put("answer",     q.getAnswer());
            r.put("choices",    new ArrayList<>(q.getChoices()));
            records.add(r);
        }
        try {
            JsonUtil.writeArray(QUESTIONS_FILE, records);
        } catch (IOException e) {
            System.out.println("Error saving questions.json: " + e.getMessage());
        }
    }
}
