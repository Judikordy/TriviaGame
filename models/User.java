package models;

import server.ClientHandler;

public class User {

    public String name;
    public String username;
    public String password;
    private ClientHandler clientHandler;

    public User(String name, String username, String password){
        this.name = name;
        this.username = username;
        this.password = password;
    }
    
    public void setHandler(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void sendMessage(String message) {
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
}
