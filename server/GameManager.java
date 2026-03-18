package server;

import models.Team;
import models.User;
import java.util.*;

public class GameManager {

    private static GameManager INSTANCE;

    public static synchronized GameManager instance() {
        if (INSTANCE == null) INSTANCE = new GameManager();
        return INSTANCE;
    }

    public static class TeamInfo {
        public final Team   team;
        public final String category;
        public final String difficulty;
        public final int    maxQuestions;

        public TeamInfo(Team t, String cat, String diff, int mq) {
            team = t; category = cat; difficulty = diff; maxQuestions = mq;
        }
    }

    private final ConfigManager           config         = new ConfigManager();
    private final Map<String,List<Team>>  waitingTeams   = new HashMap<>();
    private final Map<String,TeamInfo>    teamInfoMap    = new HashMap<>();
    private final List<GameSession>       activeSessions = new ArrayList<>();
    private final Map<String,Team>        playerTeamMap  = new HashMap<>();

    private GameManager() {}

    public int getMaxPlayersPerTeam() {
        return config.getInt("maxPlayersPerTeam", 1);
    }

    public synchronized boolean teamExists(String name) {
        return teamInfoMap.containsKey(name.toLowerCase());
    }

    public synchronized TeamInfo getTeamInfo(String name) {
        return teamInfoMap.get(name.toLowerCase());
    }

    public synchronized Team registerPlayer(
            User user, String teamName,
            String category, String difficulty, int maxQ) {

        String key = key(category, difficulty, maxQ);
        List<Team> pool = waitingTeams.computeIfAbsent(key, k -> new ArrayList<>());

        Team team = null;
        for (Team t : pool) {
            if (t.getName().equalsIgnoreCase(teamName)) { team = t; break; }
        }
        if (team == null) {
            team = new Team(teamName);
            pool.add(team);
            teamInfoMap.put(teamName.toLowerCase(),
                    new TeamInfo(team, category, difficulty, maxQ));
        }

        team.addPlayer(user);
        playerTeamMap.put(user.getUsername(), team);

        if (team.getSize() >= getMaxPlayersPerTeam()) {
            tryMatch(team, category, difficulty, maxQ, pool);
        }
        return team;
    }

    public synchronized void onHandlerSet(
            User user, String category, String difficulty, int maxQ) {
        Team team = playerTeamMap.get(user.getUsername());
        if (team == null || team.getSize() < getMaxPlayersPerTeam()) return;
        List<Team> pool = waitingTeams.getOrDefault(
                key(category, difficulty, maxQ), new ArrayList<>());
        tryMatch(team, category, difficulty, maxQ, pool);
    }

    private void tryMatch(
            Team full, String category, String difficulty,
            int maxQ, List<Team> pool) {
        int max = getMaxPlayersPerTeam();
        for (Iterator<Team> it = pool.iterator(); it.hasNext(); ) {
            Team opp = it.next();
            if (opp == full || opp.getSize() < max) continue;
            it.remove();
            pool.remove(full);
            teamInfoMap.remove(full.getName().toLowerCase());
            teamInfoMap.remove(opp.getName().toLowerCase());
            List<Team> matched = Arrays.asList(full, opp);
            GameSession session = new GameSession(matched, category, difficulty, maxQ);
            activeSessions.add(session);
            System.out.println("[MATCH] " + full.getName()
                    + " vs " + opp.getName()
                    + " | " + category + " | " + difficulty + " | " + maxQ + "q");
            session.start();
            return;
        }
    }

    public synchronized GameSession getSessionForUser(User user) {
        for (GameSession s : activeSessions) {
            for (Team t : s.getTeams()) {
                for (User p : t.getPlayers()) {
                    if (p.getUsername().equals(user.getUsername())) return s;
                }
            }
        }
        return null;
    }

    public synchronized void removeSession(GameSession session) {
        activeSessions.remove(session);
    }

    public synchronized void removePlayer(User user) {
        Team team = playerTeamMap.remove(user.getUsername());
        if (team == null) return;
        team.getPlayers().remove(user);
        if (team.getSize() == 0) {
            for (List<Team> pool : waitingTeams.values()) pool.remove(team);
            teamInfoMap.remove(team.getName().toLowerCase());
        }
    }

    public Team getTeamForUser(User user) {
        return playerTeamMap.get(user.getUsername());
    }

    private String key(String category, String difficulty, int maxQ) {
        return category + "|" + difficulty + "|" + maxQ;
    }
}
