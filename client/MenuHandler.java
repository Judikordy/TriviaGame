package client;

public class MenuHandler {

    public static String authMenu() {
        return "\n=== Trivia Game ===\n" +
               "1. Login\n" +
               "2. Register\n" +
               "0. Quit\n" +
               "Enter choice: ";
    }

    public static String gameModeMenu() {
        return "\n=== Game Mode ===\n" +
               "1. Single Player\n" +
               "2. Multiplayer\n" +
               "3. Score History\n" +
               "0. Quit\n" +
               "Enter choice: ";
    }

    public static String multiplayerMenu() {
        return "\n=== Multiplayer ===\n" +
               "1. Create a team\n" +
               "2. Join an existing team\n" +
               "0. Back\n" +
               "Enter choice: ";
    }

    public static String scoresMenu() {
        return "\n=== Score History ===\n" +
               "1. Single Player\n" +
               "2. Multiplayer\n" +
               "0. Back\n" +
               "Enter choice: ";
    }

    public static String categoryMenu() {
        return "\n=== Select Category ===\n" +
               "1. Math\n" +
               "2. Science\n" +
               "3. Geography\n" +
               "4. History\n" +
               "5. General Knowledge\n" +
               "Enter choice: ";
    }

    public static String difficultyMenu() {
        return "\n=== Select Difficulty ===\n" +
               "1. Easy\n" +
               "2. Medium\n" +
               "3. Hard\n" +
               "Enter choice: ";
    }

    public static String categoryFromChoice(String choice) {
        switch (choice.trim()) {
            case "1": return "Math";
            case "2": return "Science";
            case "3": return "Geography";
            case "4": return "History";
            case "5": return "General Knowledge";
            default:  return null;
        }
    }

    public static String difficultyFromChoice(String choice) {
        switch (choice.trim()) {
            case "1": return "Easy";
            case "2": return "Medium";
            case "3": return "Hard";
            default:  return null;
        }
    }
}