import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class ChatWindow extends JFrame {
    static final String CHAT_HISTORY_FILE = "chat_history.txt";
    private Set<String> processedMessages = new HashSet<>();
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton, logoutButton, removeButton, historyButton;
    private JComboBox<String> userSelector;
    private User currentUser;
    private Timer refreshTimer;

    public ChatWindow(User currentUser) {
        this.currentUser = currentUser;
        initializeUI();
        loadChatHistory();
        startAutoRefresh();
    }

    private void startAutoRefresh() {
        refreshTimer = new Timer();
        refreshTimer.schedule(new TimerTask() {
            public void run() {
                updateUserSelector();
                checkNewMessages();
            }
        }, 0, 3000);
    }

    private void checkNewMessages() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))) {
            reader.lines()
                .filter(line -> !processedMessages.contains(line))
                .forEach(line -> {
                    processedMessages.add(line);
                    String[] parts = line.split("\\] | -> |: ");
                    if (parts.length >= 4) {
                        String receiver = parts[2].trim();
                        if (receiver.equals(currentUser.username)) {
                            chatArea.append(formatHistoryLine(line) + "\n");
                        }
                    }
                });
        } catch (IOException e) {
            System.err.println("Error checking messages: " + e.getMessage());
        }
    }
    private void initializeUI() {
        setTitle("Chat Application - " + currentUser.username);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // User selector panel
        JPanel topPanel = new JPanel(new BorderLayout());
        userSelector = new JComboBox<>();
        updateUserSelector();
        topPanel.add(new JLabel("Chat with:"), BorderLayout.WEST);
        topPanel.add(userSelector, BorderLayout.CENTER);

        // Control buttons
        JPanel controlPanel = new JPanel();
        logoutButton = new JButton("Logout");
        removeButton = new JButton("Remove User");
        historyButton = new JButton("View Full History");
        controlPanel.add(logoutButton);
        controlPanel.add(removeButton);
        controlPanel.add(historyButton);
        topPanel.add(controlPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Message input
        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        sendButton = new JButton("Send");
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Event Listeners
        sendButton.addActionListener(this::handleSendMessage);
        logoutButton.addActionListener(this::handleLogout);
        removeButton.addActionListener(this::handleRemoveUser);
        historyButton.addActionListener(e -> showFullHistory());

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void updateUserSelector() {
        String previousSelection = (String) userSelector.getSelectedItem();
        userSelector.removeAllItems();

        List<User> allUsers = User.getAllUsersSorted();
        allUsers.stream()
                .filter(user -> !user.username.equals(currentUser.username)) // Exclude the current user
                .forEach(user -> userSelector.addItem(
                    user.username + " (" + (user.isOnline() ? "Online" : "Offline") + ")"
                ));

        if (previousSelection != null) {
            userSelector.setSelectedItem(previousSelection);
        }
    }

    private void handleSendMessage(ActionEvent e) {
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        String receiver = getSelectedReceiver();
        if (receiver == null || receiver.equals(currentUser.username)) {
            JOptionPane.showMessageDialog(this, "Invalid recipient!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Save message to file
        String logEntry = String.format("[%d] %s -> %s: %s%n",
            System.currentTimeMillis(),
            currentUser.username,
            receiver,
            message
        );

        try (FileWriter fw = new FileWriter(CHAT_HISTORY_FILE, true)) {
            fw.write(logEntry);
            chatArea.append("You -> " + receiver + ": " + message + "\n");
            messageField.setText("");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to save message!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadChatHistory() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))) {
            reader.lines().forEach(line -> {
                String[] parts = line.split("\\] | -> |: ");
                if (parts.length >= 4) {
                    String sender = parts[1].trim();
                    String receiver = parts[2].trim();
                    if (sender.equals(currentUser.username) || receiver.equals(currentUser.username)) {
                        chatArea.append(formatHistoryLine(line) + "\n");
                    }
                }
            });
        } catch (IOException e) {
            System.err.println("Error loading history: " + e.getMessage());
        }
    }

    private String formatHistoryLine(String rawLine) {
        return rawLine.replaceFirst("\\[\\d+\\]\\s+", "");
    }

    private void showFullHistory() {
        JDialog historyDialog = new JDialog(this, "Full Chat History", true);
        historyDialog.setSize(600, 400);

        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))) {
            reader.lines().forEach(line -> {
                String[] parts = line.split("\\] | -> |: ");
                if (parts.length >= 4) {
                    String sender = parts[1].trim();
                    String receiver = parts[2].trim();
                    if (sender.equals(currentUser.username) || receiver.equals(currentUser.username)) {
                        historyArea.append(formatHistoryLine(line) + "\n");
                    }
                }
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading full history!");
        }

        historyDialog.add(new JScrollPane(historyArea));
        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
    }
    private void loadUserHistory(JTextArea area) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))) {
            reader.lines().forEach(line -> {
                String[] parts = line.split(" -> |: ");
                if (parts.length == 4 &&
                    (parts[1].equals(currentUser.username) || parts[2].equals(currentUser.username))) {
                    area.append(formatHistoryLine(line) + "\n");
                }
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading history!");
        }
    }

    private String getSelectedReceiver() {
        Object selection = userSelector.getSelectedItem();
        if (selection == null) return null;

        String selected = selection.toString();
        return selected.split(" \\(")[0].trim(); // Extract username before the status
    }

    private void handleLogout(ActionEvent e) {
        currentUser.setOnlineStatus(false);
        dispose();
        new LoginPage().setVisible(true);
    }

    private void handleRemoveUser(ActionEvent e) {
        String target = getSelectedReceiver();
        if (target == null || target.equals(currentUser.username)) {
            JOptionPane.showMessageDialog(this, "Invalid user selection!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to remove user " + target + "?",
            "Confirm Removal",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (User.removeUser(target)) {
                updateUserSelector();
                JOptionPane.showMessageDialog(this, "User removed successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to remove user!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}