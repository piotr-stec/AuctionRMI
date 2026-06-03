package org.example.auction.server;

import org.example.auction.common.User;

import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private static final String FILE = "users.csv";
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public UserRepository() {
        loadFromFile();
    }

    public boolean addUser(User user) {
        if (users.containsKey(user.getLogin())) return false;
        users.put(user.getLogin(), user);
        saveToFile();
        return true;
    }

    public User getUser(String login) {
        return users.get(login);
    }

    public boolean authenticate(String login, String password) {
        User user = users.get(login);
        return user != null && user.getPassword().equals(password);
    }

    public Collection<User> getAllUsers() {
        return users.values();
    }

    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (User u : users.values()) {
                pw.println(u.toCSV());
            }
        } catch (IOException e) {
            System.err.println("Błąd zapisu users.csv: " + e.getMessage());
        }
    }

    private void loadFromFile() {
        File file = new File(FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isBlank()) {
                    User u = User.fromCSV(line);
                    users.put(u.getLogin(), u);
                }
            }
            System.out.println("Wczytano " + users.size() + " użytkowników.");
        } catch (IOException e) {
            System.err.println("Błąd odczytu users.csv: " + e.getMessage());
        }
    }
}