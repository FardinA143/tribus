package user;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio encargado de gestionar el registro, autenticación y sesiones activas
 * de los usuarios del sistema.
 * 
 * <p>Proporciona operaciones para registrar nuevos usuarios, iniciar sesión,
 * cerrar sesión y realizar validaciones básicas de credenciales. Utiliza un 
 * almacenamiento en memoria basado en {@code HashMap}.</p>
 */
public class AuthService {

    /**
     * Crea un servei d'autenticació amb emmagatzematge en memòria.
     */
    public AuthService() {
    }

    /** Mapa de usuarios registrados, indexados por su ID. */
    private Map<String, RegisteredUser> registeredUsers = new HashMap<>(); 

    /** Mapa de sesiones activas, indexadas por su ID de sesión. */
    private Map<String, Sesion> activeSessions = new HashMap<>(); 

    /**
     * Registra un nuevo usuario en el sistema.
     *
     * @param id           ID único del usuario.
     * @param displayName  Nombre visible del usuario.
     * @param username     Nombre de usuario para iniciar sesión.
     * @param password     Contraseña en texto plano del usuario.
     * @return El {@link RegisteredUser} recién registrado, o {@code null} si el
     *         nombre de usuario ya está en uso.
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
     * Inicia sesión con un nombre de usuario y contraseña.
     *
     * @param username Nombre de usuario.
     * @param password Contraseña en texto plano.
     * @return Una nueva {@link Sesion} si las credenciales son válidas, o 
     *         {@code null} si la autenticación falla.
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
     * Cierra una sesión activa y la elimina del registro de sesiones.
     *
     * @param sess La sesión a cerrar.
     */
    public void logout(Sesion sess) {
        sess.close();
        activeSessions.remove(sess.getSessionId());
    }

    /**
     * Retorna una vista inmutable de los usuarios registrados.
     *
     * @return colección no modificable con los usuarios registrados.
     */
    public Collection<RegisteredUser> listRegisteredUsers() {
        return Collections.unmodifiableCollection(registeredUsers.values());
    }

    // ======================
    // Métodos auxiliares
    // ======================

    /**
     * Genera un hash simple basado en {@code hashCode()} para una contraseña.
     *
     * <p><b>Nota:</b> Este método no debe usarse en producción, ya que no es
     * criptográﬁcamente seguro.</p>
     *
     * @param password Contraseña en texto plano.
     * @return Cadena hash correspondiente.
     */
    private String hashPassword(String password) {
        return Integer.toHexString(password.hashCode());
    }

    /**
     * Verifica si una contraseña coincide con su hash almacenado.
     *
     * @param password Contraseña en texto plano.
     * @param hash     Hash almacenado.
     * @return {@code true} si la contraseña coincide; {@code false} en caso contrario.
     */
    private boolean verifyPassword(String password, String hash) {
        return hashPassword(password).equals(hash);
    }
}
