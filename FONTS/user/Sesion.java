package user;

import java.time.LocalDateTime;
import java.util.random.RandomGenerator;

/**
 * Representa una sessió activa associada a un usuari.
 *
 * <p>Una sessió conté un identificador únic, informació de l’usuari,
 * un registre de l’últim accés i un estat que indica si continua activa.</p>
 */
public class Sesion {

    /** Identificador únic de la sessió. */
    private String sessionId;

    /** Usuari al qual pertany la sessió. */
    private User user;

    /** Data i hora de l’últim accés. */
    private String lastAccessTime;

    /** Indica si la sessió està activa. */
    private boolean active;
    
    /**
     * Crea una nova sessió per a un usuari donat.
     *
     * @param user Usuari al qual s’assigna la sessió.
     */
    public Sesion(User user) {
        this.user = user;
        this.sessionId = generateSessionId();
        this.active = true;
        this.lastAccessTime = LocalDateTime.now().toString();
    }

    /**
     * Actualitza la marca temporal de l’últim accés a la sessió.
     */
    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now().toString();
    }

    /**
     * Genera un identificador aleatori únic per a la sessió.
     *
     * @return ID de sessió en format hexadecimal.
     */
    private String generateSessionId() {
        return Long.toHexString(RandomGenerator.getDefault().nextLong());
    }

    /**
     * Retorna l’identificador de la sessió.
     *
     * @return ID de la sessió.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Retorna l’usuari associat a la sessió.
     *
     * @return Usuari de la sessió.
     */
    public User getUser() {
        return user;
    }

    /**
     * Retorna la data i hora de l’últim accés.
     *
     * @return Cadena en format ISO-8601 amb data i hora.
     */
    public String getLastAccessTime() {
        return lastAccessTime;
    }   

    /**
     * Indica si la sessió continua activa.
     *
     * @return {@code true} si la sessió està activa; {@code false} en cas contrari.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Tanca la sessió i la marca com a inactiva.
     */
    public void close() {
        System.out.println("Sessió " + sessionId + " tancada.");
        this.active = false;
    }
}
