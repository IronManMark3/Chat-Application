public class UserAuthenticator {
    public static boolean authenticate(String username) {
        // Validate username: must be 3-20 characters long, alphanumeric, underscores, or hyphens
        return username != null && username.matches("^[a-zA-Z0-9_-]{3,20}$");
    }
}