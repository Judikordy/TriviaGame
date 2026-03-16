package models;

import java.util.List;

public class Question {
    private int id;
    private String text;
    private String category;
    private String difficulty;
    private List<String> choices;
    private String answer;

    public Question(int id, String text, String category, String difficulty, List<String> choices, String answer) {
        this.id = id;
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
        this.choices = choices;
        this.answer = answer;

    }

    public int getId() {
        return id;
    }
    
    public String getText() {
        return text;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public List<String> getChoices() {
        return choices;
    }

    public String getAnswer() {
        return answer;
    }

    public String toString() {
        return "Question{" +
                "text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", choices=" + choices +
                ", answer='" + answer + '\'' +
                '}';
    }
    
}
