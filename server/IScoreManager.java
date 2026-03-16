package server;

import models.User;

public interface IScoreManager {
    
    void addPoints(User user, int points);
    int getScore(User user);
    void resetScore(User user);
    void saveScoreHistory(User user);

}