package server;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class AnswerManager {
    private static final String ANSWERS_FILE = "data/answers.txt";

    public static void saveAnswer(String username, String questionName, String answer) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(ANSWERS_FILE, true))) {
            bw.write(username + "|" + questionName + "|" + answer + "|" + java.time.LocalDateTime.now());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error saving answer: " + e.getMessage());
        }
    }
}