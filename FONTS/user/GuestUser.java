package user;

/**
 * Representa a un usuari convidat (no registrat) amb identificador efímer.
 */
public class GuestUser extends User {

    /** Marca per identificar com a usuari convidat. */
    private final boolean guest = true;

    public GuestUser(String id, String displayName) {
        super(id, displayName);
    }

    public boolean isGuest() {
        return guest;
    }
}
