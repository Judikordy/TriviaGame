package server;

import java.io.IOException;
import java.util.*;

public class ConfigManager {

    private static final String CONFIG_FILE = "data/config.json";
    private final Map<String, Object> config = new HashMap<>();

    public ConfigManager() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            List<Map<String, Object>> records = JsonUtil.readArray(CONFIG_FILE);
            // config.json is a single-element array: [{ "key": value, ... }]
            if (!records.isEmpty()) config.putAll(records.get(0));
            System.out.println("Config loaded.");
        } catch (IOException e) {
            System.out.println("config.json not found. Using defaults.");
        }
    }

    public int getInt(String key, int defaultValue) {
        try { return (int) config.get(key); } catch (Exception e) { return defaultValue; }
    }

    public String getString(String key, String defaultValue) {
        Object v = config.get(key);
        return v != null ? v.toString() : defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try { return (boolean) config.get(key); } catch (Exception e) { return defaultValue; }
    }
}
