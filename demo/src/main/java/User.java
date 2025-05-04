import java.io.*;
import java.util.*;

public class User extends ChatUser implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean isOnline;
    static final String USERS_FILE = "users.dat";

    public User() {
        super("");
        this.isOnline = false;
    }

    public User(String username) {
        super(username);
        this.isOnline = false;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnlineStatus(boolean status) {
        List<User> users = loadAllUsers();
        users.stream()
            .filter(u -> u.username.equals(this.username))
            .findFirst()
            .ifPresent(u -> u.isOnline = status);
        saveAllUsers(users);
        this.isOnline = status;
    }

    public static User findOrCreateUser(String username) {
        List<User> users = loadAllUsers();
        Optional<User> existing = users.stream()
            .filter(u -> u.username.equals(username))
            .findFirst();

        if (existing.isPresent()) {
            return existing.get();
        }

        User newUser = new User(username);
        users.add(newUser);
        saveAllUsers(users);
        return newUser;
    }

    public static List<User> getAllUsers() {
        return loadAllUsers();
    }

    public static List<User> getAllUsersSorted() {
        List<User> users = loadAllUsers();
        users.sort(Comparator.comparing(User::isOnline).reversed()
            .thenComparing(u -> u.username));
        return users;
    }

    public static boolean removeUser(String username) {
        List<User> users = loadAllUsers();
        boolean removed = users.removeIf(u -> u.username.equals(username));
        if (removed) saveAllUsers(users);
        return removed;
    }

    private static List<User> loadAllUsers() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            return (List<User>) ois.readObject();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static void saveAllUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public static void initUserFile() {
        saveAllUsers(new ArrayList<>());
    }

    @Override
    public String toString() {
        return username + " (" + (isOnline ? "Online" : "Offline") + ")";
    }

}