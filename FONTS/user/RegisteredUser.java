package user;

/**
 * Representa a un usuario registrado dentro del sistema.
 * 
 * <p>Extiende la clase {@link User} añadiendo credenciales necesarias para
 * la autenticación, como el nombre de usuario y la contraseña (almacenada
 * como hash).</p>
 */
public class RegisteredUser extends User {

    /** Nombre de usuario utilizado para iniciar sesión. */
    private String username;

    /** Hash de la contraseña del usuario. */
    private String password;

    /**
     * Crea un nuevo usuario registrado.
     *
     * @param id          Identificador único del usuario.
     * @param displayName Nombre visible del usuario.
     * @param createdAt   Fecha y hora de creación del usuario.
     * @param username    Nombre de usuario para autenticación.
     * @param password    Hash de la contraseña del usuario.
     */
    public RegisteredUser(String id, String displayName, String createdAt, String username, String password) {
        super(id, displayName);
        this.username = username;
        this.password = password;
    }   

    /**
     * Obtiene el nombre de usuario.
     *
     * @return Nombre de usuario.
     */
    public String getUsername() {
        return username;
    }   

    /**
     * Obtiene el hash de la contraseña almacenada.
     *
     * @return Hash de la contraseña.
     */
    public String getPasswordHash() {
        return password;
    }   

    /**
     * Cambia el nombre de usuario.
     *
     * @param name Nuevo nombre de usuario.
     */
    public void changeUsername(String name){
        username = name;
    }

    /**
     * Cambia la contraseña almacenada.
     *
     * <p>Se espera que el valor recibido ya sea un hash seguro.</p>
     *
     * @param passwd Nuevo hash de contraseña.
     */
    public void changePassword(String passwd){
        password = passwd;
    }
}
