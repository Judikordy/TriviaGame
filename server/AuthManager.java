package server;

import models.User;

import java.io.IOException;
import java.util.*;

public class AuthManager implements IAuthManager {

    public static final String UNAUTHORIZED  = "401 Unauthorized";
    public static final String USER_NOT_FOUND = "404 Not Found";

    private final Map<String, User> users = new HashMap<>();
    private static final String USERS_FILE = "data/users.json";

    public AuthManager() {
        loadUsers();
    }

    public void loadUsers() {
        try {
            List<Map<String, Object>> records = JsonUtil.readArray(USERS_FILE);
            for (Map<String, Object> r : records) {
                String name     = (String) r.get("name");
                String username = (String) r.get("username");
                String password = (String) r.get("password");
                if (username != null) users.put(username, new User(name, username, password));
            }
            System.out.println("Loaded " + users.size() + " users.");
        } catch (IOException e) {
            System.out.println("users.json not found. Starting with empty user database.");
        }
    }

    @Override
    public String login(String username, String password) {
        if (!users.containsKey(username)) return USER_NOT_FOUND;
        if (!users.get(username).getPassword().equals(password)) return UNAUTHORIZED;
        return "200 OK";
    }

    @Override
    public String register(String name, String username, String password) {
        if (users.containsKey(username)) return "Username already exists";
        users.put(username, new User(name, username, password));
        return "200 OK";
    }

    @Override
    public void saveUser(User user) {
        users.put(user.getUsername(), user);
        persistAll();
    }

    @Override
    public User getUser(String username) {
        return users.get(username);
    }

    private void persistAll() {
        List<Map<String, Object>> records = new ArrayList<>();
        for (User u : users.values()) {
            Map<String, Object> r = new LinkedHashMap<>();
            r.put("name",     u.getName());
            r.put("username", u.getUsername());
            r.put("password", u.getPassword());
            records.add(r);
        }
        try {
            JsonUtil.writeArray(USERS_FILE, records);
        } catch (IOException e) {
            System.out.println("Error saving users.json: " + e.getMessage());
        }
    }
}
