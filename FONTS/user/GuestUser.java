package user;

/**
 * Representa a un usuario invitado dentro del sistema.
 *
 * <p>Un {@code GuestUser} es un tipo de usuario temporal que posee una fecha
 * de expiración, después de la cual ya no se considera válido. Extiende la 
 * clase {@link User} añadiendo el campo {@code expiresAt}.</p>
 */
public class GuestUser extends User {

    /** Fecha y hora en que expira el usuario invitado. */
    String expiresAt;

    /**
     * Crea un nuevo usuario invitado con un tiempo de expiración.
     *
     * @param id           Identificador único del usuario.
     * @param displayName  Nombre visible del usuario invitado.
     * @param expiresAt    Fecha y hora de expiración del usuario (formato ISO-8601 recomendado).
     */
    public GuestUser(String id, String displayName, String expiresAt) {
        super(id, displayName);
        this.expiresAt = expiresAt;
    }

    /**
     * Obtiene la fecha y hora de expiración del usuario invitado.
     *
     * @return Cadena que indica cuándo expira el usuario.
     */
    public String getExpiresAt() {
        return expiresAt;
    }
}
