package server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import models.Question;

public class QuestionBank implements IQuestionBank {

    private List<Question> questions;
    public static final String questionsFilePath = "questions.txt";

    public QuestionBank() {
        questions = new ArrayList<>();
    }

    @Override
    public void loadQuestions() {
        try (BufferedReader reader = new BufferedReader(new FileReader(questionsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length < 5) continue; // ignore invalid lines

                String text = parts[0].trim();
                String category = parts[1].trim();
                String difficulty = parts[2].trim();
                String answer = parts[3].trim();

                List<String> choices = new ArrayList<>();
                for (int i = 4; i < parts.length; i++) {
                    choices.add(parts[i].trim());
                }

                Question q = new Question(text, category, difficulty, choices, answer);
                questions.add(q);
            }

            System.out.println("Loaded " + questions.size() + " questions.");

        } catch (Exception e) {
            System.out.println("Error loading questions: " + e.getMessage());
        }
    }

    @Override
    public void addQuestion(Question question) {
        questions.add(question);
    }

    @Override
    public void removeQuestion(Question question) {
        questions.remove(question);
    }

    @Override
    public List<Question> getQuestionByCategory(String category) {
        List<Question> result = new ArrayList<>();
        for (Question q : questions) {
            if (q.getCategory().equalsIgnoreCase(category)) {
                result.add(q);
            }
        }
        return result;
    }

    @Override
    public List<Question> getQuestionByDifficulty(String difficulty) {
        List<Question> result = new ArrayList<>();
        for (Question q : questions) {
            if (q.getDifficulty().equalsIgnoreCase(difficulty)) {
                result.add(q);
            }
        }
        return result;
    }

    @Override
    public void saveQuestion(Question question) {
        try (FileWriter writer = new FileWriter(questionsFilePath, true)) {
            StringBuilder sb = new StringBuilder();
            sb.append(question.getText()).append(",")
              .append(question.getCategory()).append(",")
              .append(question.getDifficulty()).append(",")
              .append(question.getAnswer());
            for (String choice : question.getChoices()) {
                sb.append(",").append(choice);
            }
            sb.append("\n");
            writer.write(sb.toString());
        } catch (Exception e) {
            System.out.println("Error saving question: " + e.getMessage());
        }
    }

    // New helper method for random question selection
    public Question getRandomQuestion() {
        if (questions.isEmpty()) return null;
        int index = (int)(Math.random() * questions.size());
        return questions.get(index);
    }
}
