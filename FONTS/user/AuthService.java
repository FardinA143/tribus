package user;
import java.util.Map;
import java.util.HashMap;

public class AuthService {
    private Map<String, RegisteredUser> registeredUsers = new HashMap<>(); //ID User, classe
    private Map<String, Sesion> activeSessions = new HashMap<>(); //ID Sessió, classe

    public RegisteredUser register(String id, String displayName, String username, String password) {
        if(registeredUsers.values().stream().anyMatch(u -> u.getUsername().equals(username))){
            System.out.println("Username already taken");
            return null;
        }
        String passwordHash = hashPassword(password);
        RegisteredUser newUser = new RegisteredUser(id, displayName, java.time.LocalDateTime.now().toString(), username, passwordHash);
        registeredUsers.put(id, newUser);
        return newUser;
    }

    public Sesion login(String username, String password) {
        for(RegisteredUser usr : registeredUsers.values()){
            if(usr.getUsername().equals(username) && verifyPassword(password, usr.getPasswordHash())){
                Sesion sess = new Sesion(usr);
                activeSessions.put(sess.getSessionId(), sess);
                return sess;
            }
        }
        System.out.println("Invalid credentials");
        return null;

    }

    public void logout(Sesion sess) {
        sess.close();
        activeSessions.remove(sess.getSessionId());
    }

    // Métodos auxiliares

    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
