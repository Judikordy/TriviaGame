package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import models.User;

public class AuthManager implements IAuthManager {

    public static final String UNAUTHORIZED = "401 Unauthorized";
    public static final String USER_NOT_FOUND = "404 Not Found";
    private Map<String, User> users;
    private final String usersFilePath = "data/users.txt";

    public AuthManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public void loadUsers(){
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFilePath))){
            String line;
            while ((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                if (parts.length == 3){
                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();
                    users.put(username, new User(name, username, password));
                }
            }

        }catch (FileNotFoundException e){
            System.out.println("users.txt file not found. Starting with an empty user database.");
        }catch (IOException e){
            e.printStackTrace();
        }

    }


    @Override
    public String login(String username, String password){
        if (!users.containsKey(username)){
            return USER_NOT_FOUND;
        }

        User user = users.get(username);

        if (!user.getPassword().equals(password)){
            return UNAUTHORIZED;
        }

        return "200 OK";
    }

    @Override
    public String register(String name, String username, String password){
        if (users.containsKey(username)){
            return "Username already exists";

        }
        else{
            User newUser = new User(name, username, password);
            users.put(username, newUser);

            return "200 OK";
        }

    }

    @Override
    public void saveUser(User user) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFilePath, true))) {

            writer.write(user.getName() + "," +
                     user.getUsername() + "," +
                     user.getPassword());

            writer.newLine();

        } catch (IOException e) {
            System.out.println("Error saving user.");
        }
    }

}