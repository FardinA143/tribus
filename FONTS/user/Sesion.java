package user;
import java.time.LocalDateTime;
import java.util.random.RandomGenerator;


public class Sesion {
    private String sessionId;
    private User user;
    private String lastAccessTime;
    private boolean active;
    

    public Sesion(User user) {
        this.user = user;
        this.sessionId = generateSessionId();
        this.active = true;
        this.lastAccessTime = LocalDateTime.now().toString();
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now().toString();
    }
    private String generateSessionId() {
        return Long.toHexString(RandomGenerator.getDefault().nextLong());
    }

    public String getSessionId() {
        return sessionId;
    }

    public User getUser() {
        return user;
    }

    public String getLastAccessTime() {
        return lastAccessTime;
    }   

    public boolean isActive() {
        return active;
    }

    public void close() {
        System.out.println("Session " + sessionId + " closed.");
        this.active = false;
    }
}
