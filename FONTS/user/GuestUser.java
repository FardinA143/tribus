package user;

public class GuestUser extends User{
    String expiresAt;

    public GuestUser(String id, String displayName, String expiresAt) {
        super(id, displayName);
        this.expiresAt = expiresAt;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}

