package server;

import models.User;

public interface IAuthManager {

    void loadUsers();
    String login(String username, String password);
    String register(String name, String username, String password);
    void saveUser(User user);
    User getUser(String username);
    
}
