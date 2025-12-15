package user;

import java.time.LocalDateTime;
import java.util.random.RandomGenerator;

/**
 * Representa una sesión activa asociada a un usuario.
 *
 * <p>Una sesión contiene un identificador único, información del usuario,
 * un registro del último acceso y un estado que indica si está activa.</p>
 */
public class Sesion {

    /** Identificador único de la sesión. */
    private String sessionId;

    /** Usuario al que pertenece la sesión. */
    private User user;

    /** Fecha y hora del último acceso. */
    private String lastAccessTime;

    /** Indica si la sesión está activa. */
    private boolean active;
    
    /**
     * Crea una nueva sesión para un usuario dado.
     *
     * @param user Usuario al que se asigna la sesión.
     */
    public Sesion(User user) {
        this.user = user;
        this.sessionId = generateSessionId();
        this.active = true;
        this.lastAccessTime = LocalDateTime.now().toString();
    }

    /**
     * Actualiza la marca temporal del último acceso a la sesión.
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now().toString();
    }

    /**
     * Genera un identificador aleatorio único para la sesión.
     *
     * @return ID de sesión en formato hexadecimal.
     */
    private String generateSessionId() {
        return Long.toHexString(RandomGenerator.getDefault().nextLong());
    }

    /**
     * Obtiene el identificador de la sesión.
     *
     * @return ID de la sesión.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Obtiene el usuario asociado a la sesión.
     *
     * @return Usuario de la sesión.
     */
    public User getUser() {
        return user;
    }

    /**
     * Obtiene la fecha y hora del último acceso.
     *
     * @return Cadena ISO-8601 con fecha y hora.
     */
    public String getLastAccessTime() {
        return lastAccessTime;
    }   

    /**
     * Indica si la sesión sigue activa.
     *
     * @return {@code true} si la sesión está activa; {@code false} en caso contrario.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Cierra la sesión y la marca como inactiva.
     */
    public void close() {
        System.out.println("Session " + sessionId + " closed.");
        this.active = false;
    }
}
