package server;

import java.util.List;

import models.Question;

public interface IQuestionBank {
    
    public void addQuestion(Question question);
    public void removeQuestion(Question question);
    List<Question> getQuestionByCategory(String category);
    List<Question> getQuestionByDifficulty(String difficulty);
    public void loadQuestions();
    public void saveQuestion(Question question);

}
