package models;

public class User {

    public String name;
    public String username;
    public String password;

    public User(String name, String username, String password){
        this.name = name;
        this.username = username;
        this.password = password;
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
