package server;

import java.util.Scanner;

import models.User;

public class ServerMain {
    public static void main(String[] args) {

        AuthManager auth = new AuthManager();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nChoose an option: LOGIN / REGISTER / EXIT");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "LOGIN":
                    System.out.print("Username: ");
                    String loginUsername = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String loginPassword = scanner.nextLine().trim();

                    String loginResult = auth.login(loginUsername, loginPassword);
                    System.out.println("Result: " + loginResult);
                    break;

                case "REGISTER":
                    System.out.print("Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Username: ");
                    String regUsername = scanner.nextLine().trim();
                    System.out.print("Password: ");
                    String regPassword = scanner.nextLine().trim();

                    String registerResult = auth.register(name, regUsername, regPassword);
                    auth.saveUser(registerResult.equals("200 OK") ? new User(name, regUsername, regPassword) : null);
                    System.out.println("Result: " + registerResult);
                    break;

                case "EXIT":
                    System.out.println("Exiting test.");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
    
}
