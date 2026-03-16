package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final String configFilePath = "data/config.txt";
    private Map<String, String> config;

    public ConfigManager() {
        config = new HashMap<>();
        loadConfig();
    }

    private void loadConfig(){
        File file = new File(configFilePath);
        if (!file.exists()){
            System.out.println("config.txt file not found. Starting with a default configuration.");
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))){
            String line;
            while ((line = reader.readLine()) != null){
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("=", 2);

                if (parts.length == 2){
                    config.put(parts[0].trim(), parts[1].trim());
                }
            }
            
        } catch (IOException e) {
            System.out.println("Error reading config file: " + e.getMessage());
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(config.get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getString(String key, String defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(config.get(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
