import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;

public class ChatMain {
    public static void main(String[] args) {
        // Initialize data files
        initDataFiles();

        // Launch the login page
        SwingUtilities.invokeLater(() -> new LoginPage());
    }

    private static void initDataFiles() {
        try {
            // Initialize users.dat
            File usersFile = new File(User.USERS_FILE);
            if (!usersFile.exists()) {
                System.out.println("Creating users file: " + usersFile.getAbsolutePath());
                User.initUserFile(); // Initialize with empty list
            } else {
                System.out.println("Using existing users file: " + usersFile.getAbsolutePath());
            }

            // Initialize chat_history.txt
            File chatFile = new File(ChatWindow.CHAT_HISTORY_FILE);
            if (!chatFile.exists()) {
                System.out.println("Creating chat history file: " + chatFile.getAbsolutePath());
                chatFile.createNewFile(); // Create empty file
            } else {
                System.out.println("Using existing chat history file: " + chatFile.getAbsolutePath());
            }

        } catch (IOException e) {
            System.err.println("Critical error during initialization: " + e.getMessage());
            System.exit(1); // Halt execution if files can't be created
        }
    }
}