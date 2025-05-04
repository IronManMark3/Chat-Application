import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoginPage extends JFrame {
    public LoginPage() {
        setTitle("Chat Login");
        setSize(400, 200);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2, 10, 10));

        JTextField usernameField = new JTextField();
        JComboBox<String> userList = new JComboBox<>();
        loadUsers(userList);

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Existing Users:"));
        add(userList);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> handleLogin(usernameField.getText().trim()));
        add(loginBtn);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadUsers(JComboBox<String> comboBox) {
        comboBox.removeAllItems();
        User.getAllUsers().stream()
            .filter(u -> !u.isOnline())
            .map(u -> u.username)
            .forEach(comboBox::addItem);
    }

    private void handleLogin(String username) {
        if (!username.matches("^[a-zA-Z0-9_-]{3,20}$")) {
            JOptionPane.showMessageDialog(this, "Invalid username!");
            return;
        }

        User user = User.findOrCreateUser(username);
        if (user.isOnline()) {
            JOptionPane.showMessageDialog(this, "User already logged in!");
            return;
        }

        new ChatWindow(user);
        dispose();
    }
}