package user;

/**
 * Representa a un usuari convidat (no registrat) amb identificador efímer.
 */
public class GuestUser extends User {

    /** Marca per identificar com a usuari convidat. */
    private final boolean guest = true;
    /** Data d'expiració opcional per a la sessió convidat (ISO-8601). */
    private String expiresAt;

    /**
     * Crea un `GuestUser` sense data d'expiracio.
     *
     * @param id Identificador temporal de l'usuari
     * @param displayName Nom visible de l'usuari
     */
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

    /**
     * Indica si aquesta instancia representa un usuari convidat.
     *
     * @return `true` sempre per a `GuestUser`
     */
    public boolean isGuest() {
        return guest;
    }

    /**
     * Retorna la data d'expiracio de la sessio del guest en format ISO-8601,
     * o `null` si no s'ha establert.
     *
     * @return Data d'expiracio (ISO-8601) o `null`
     */
    public String getExpiresAt() {
        return expiresAt;
    }
}
