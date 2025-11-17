package user;

/**
 * Representa a un usuari convidat (no registrat) amb identificador efímer.
 */
public class GuestUser extends User {

    /** Marca per identificar com a usuari convidat. */
    private final boolean guest = true;
    /** Data d'expiració opcional per a la sessió convidat (ISO-8601). */
    private String expiresAt;

    public GuestUser(String id, String displayName) {
        super(id, displayName);
        this.expiresAt = null;
    }

    /**
     * Constructor amb data d'expiració.
     * @param id Identificador del guest
     * @param displayName Nom visible
     * @param expiresAt Data d'expiració (ISO-8601)
     */
    public GuestUser(String id, String displayName, String expiresAt) {
        super(id, displayName);
        this.expiresAt = expiresAt;
    }

    public boolean isGuest() {
        return guest;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
