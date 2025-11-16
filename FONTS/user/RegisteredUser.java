package user;

/**
 * Representa un usuari registrat dins del sistema.
 * 
 * <p>Extén la classe {@link User} afegint-hi les credencials necessàries per
 * a l'autenticació, com el nom d'usuari i la contrasenya (emmagatzemada
 * com a hash).</p>
 */
public class RegisteredUser extends User {

    /** Nom d’usuari utilitzat per iniciar sessió. */
    private String username;

    /** Hash de la contrasenya de l’usuari. */
    private String password;

    /**
     * Crea un nou usuari registrat.
     *
     * @param id          Identificador únic de l’usuari.
     * @param displayName Nom visible de l’usuari.
     * @param createdAt   Data i hora de creació de l’usuari.
     * @param username    Nom d’usuari per a l’autenticació.
     * @param password    Hash de la contrasenya de l’usuari.
     */
    public RegisteredUser(String id, String displayName, String createdAt, String username, String password) {
        super(id, displayName);
        this.username = username;
        this.password = password;
    }   

    /**
     * Retorna el nom d’usuari.
     *
     * @return Nom d’usuari.
     */
    public String getUsername() {
        return username;
    }   

    /**
     * Retorna el hash de la contrasenya emmagatzemada.
     *
     * @return Hash de la contrasenya.
     */
    public String getPasswordHash() {
        return password;
    }   

    /**
     * Canvia el nom d’usuari.
     *
     * @param name Nou nom d’usuari.
     */
    public void changeUsername(String name){
        username = name;
    }

    /**
     * Canvia la contrasenya emmagatzemada.
     *
     * <p>S’espera que el valor rebut ja sigui un hash segur.</p>
     *
     * @param passwd Nou hash de contrasenya.
     */
    public void changePassword(String passwd){
        password = passwd;
    }
}
