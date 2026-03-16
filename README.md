🎮 Java Multiplayer Trivia Game
📖 Overview
This project is a multiplayer trivia game built in Java. It uses socket programming for real-time client-server communication, supports multiple players and teams, and includes features like authentication, game rooms, timed trivia rounds, scoring, and persistent history.
✨ Features (Planned & Implemented)
- Authentication
    Login and register users with file-based storage (users.txt).
    Secure password handling (basic for now, extendable later).
- Networking
    Java ServerSocket with multithreaded ClientHandler for multiple clients.
    Console-based client application (ClientMain).
- Game Rooms
    Create and join rooms (single-player or multiplayer).
    Support for unique team names and equal team sizes.
- Teams
    Team creation and player assignment.
    Validation for balanced gameplay.
- Question Bank
    Load questions from questions.txt.
    Filter by category or difficulty.
    Random question selection.
    Save new questions to file.
- Game Engine
    Timed trivia rounds with countdown.
    Broadcast questions to all players in a room.
    Validate answers against correct solutions.
- Scoring
    Track player and team scores.
    Save score history (scores.txt).
    Display scoreboard at the end of each game.
- Persistence
    Users, questions, and scores stored in text files.
    Extendable to databases in future iterations.

   Flow Explanation:
1. Login/Register: Players authenticate with the server.
2. Lobby: Players can create or join rooms.
3. Teams: Teams are formed with unique names and balanced sizes.
4. Game Engine: Trivia rounds begin.
5. Question Broadcast: Server sends a random question + choices.
6. Answer Submission: Players respond within countdown.
7. Validation: Server checks answers against correct solution.
8. Scoring: Scores updated for players/teams.
9. Repeat Rounds until game ends.
10. Scoreboard: Final scores displayed and saved.



