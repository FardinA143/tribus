package user;

/**
 * Clase base abstracta que representa a un usuario dentro del sistema.
 *
 * <p>Define atributos comunes como el identificador único y el nombre visible,
 * además de proporcionar métodos de acceso y modificación. Las clases derivadas
 * deben extender esta clase para añadir comportamientos o datos adicionales.</p>
 */
public abstract class User {

    /** Identificador único del usuario. */
    protected String id;

    /** Nombre visible del usuario. */
    protected String displayName;

    /**
     * Crea un nuevo usuario con un ID y un nombre visible.
     *
     * @param id           Identificador único del usuario.
     * @param displayName  Nombre visible asociado al usuario.
     */
    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return ID del usuario.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre visible del usuario.
     *
     * @return Nombre mostrado al público.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Cambia el nombre visible del usuario.
     *
     * @param name Nuevo nombre visible.
     */
    public void changeDisplayName(String name) {
        displayName = name;
    }
}
