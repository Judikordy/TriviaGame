package models;

import java.util.List;

public class Question {
    private String text;
    private String category;
    private String difficulty;
    private List<String> choices;
    private String answer;

    public Question(String text, String category, String difficulty, List<String> choices, String answer) {
        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
        this.choices = choices;
        this.answer = answer;

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
    
}
