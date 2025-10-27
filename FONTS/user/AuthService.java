package user;
import java.util.Map;
import java.util.HashMap;

public class AuthService {
    private Map<String, RegisteredUser> registeredUsers = new HashMap<>();
    private Map<String, Sesion> activeSessions = new HashMap<>();

    public RegisteredUser register(String username, String password) {
        //
    }

    public Sesion login(String username, String password) {
        //
    }

    public void logout(Sesion sess) {
        sess.close();
        activeSessions.remove(sess.getSessionId());
    }

    // Métodos auxiliares
    private String hashPassword(String password, String salt) {
        //
    }

    private boolean verifyPassword(String password, String hash, String salt) {
        //
    }
}
g