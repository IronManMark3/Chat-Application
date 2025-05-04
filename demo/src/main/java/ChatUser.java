import java.io.Serializable;

public abstract class ChatUser implements Serializable {
    private static final long serialVersionUID = 1L;
    protected String username;

    public ChatUser() {
        this.username = "";
    }

    public ChatUser(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "ChatUser{" +
                "username='" + username + '\'' +
                '}';
    }
}