package user;

import java.util.Map;
import java.util.HashMap;

/**
 * Servei encarregat de gestionar el registre, l'autenticació i les sessions 
 * actives dels usuaris del sistema.
 * 
 * <p>Proporciona operacions per registrar nous usuaris, iniciar sessió,
 * tancar sessió i realitzar validacions bàsiques de credencials. Utilitza un
 * emmagatzematge en memòria basat en {@code HashMap}.</p>
 */
public class AuthService {

    /** Map d'usuaris registrats, indexats pel seu ID. */
    private Map<String, RegisteredUser> registeredUsers = new HashMap<>(); 

    /** Map de sessions actives, indexades pel seu ID de sessió. */
    private Map<String, Sesion> activeSessions = new HashMap<>(); 

    /**
     * Registra un usuari nou al sistema.
     *
     * @param id           ID únic de l'usuari.
     * @param displayName  Nom visible de l'usuari.
     * @param username     Nom d'usuari per iniciar sessió.
     * @param password     Contrasenya en text pla de l'usuari.
     * @return El {@link RegisteredUser} acabat de registrar, o {@code null} si
     *         el nom d'usuari ja està en ús.
     */
    public RegisteredUser register(String id, String displayName, String username, String password) {
        if (registeredUsers.values().stream().anyMatch(u -> u.getUsername().equals(username))) {
            System.out.println("Username already taken");
            return null;
        }
        String passwordHash = hashPassword(password);
        RegisteredUser newUser = new RegisteredUser(
                id,
                displayName,
                java.time.LocalDateTime.now().toString(),
                username,
                passwordHash
        );
        registeredUsers.put(id, newUser);
        return newUser;
    }

    /**
     * Inicia sessió amb un nom d'usuari i contrasenya.
     *
     * @param username Nom d'usuari.
     * @param password Contrasenya en text pla.
     * @return Una nova {@link Sesion} si les credencials són vàlides, o 
     *         {@code null} si l'autenticació falla.
     */
    public Sesion login(String username, String password) {
        for (RegisteredUser usr : registeredUsers.values()) {
            if (usr.getUsername().equals(username) && verifyPassword(password, usr.getPasswordHash())) {
                Sesion sess = new Sesion(usr);
                activeSessions.put(sess.getSessionId(), sess);
                return sess;
            }
        }
        System.out.println("Invalid credentials");
        return null;
    }

    /**
     * Tanca una sessió activa i l'elimina del registre de sessions.
     *
     * @param sess La sessió que s'ha de tancar.
     */
    public void logout(Sesion sess) {
        sess.close();
        activeSessions.remove(sess.getSessionId());
    }

    // ======================
    // Mètodes auxiliars
    // ======================

    /**
     * Genera un hash simple basat en {@code hashCode()} per a una contrasenya.
     *
     *
     * @param password Contrasenya en text pla.
     * @return Cadena hash corresponent.
     */
    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    /**
     * Verifica si una contrasenya coincideix amb el seu hash emmagatzemat.
     *
     * @param password Contrasenya en text pla.
     * @param hash     Hash emmagatzemat.
     * @return {@code true} si la contrasenya coincideix; {@code false} en cas contrari.
     */
    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
