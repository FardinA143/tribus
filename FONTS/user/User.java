package user;

/**
 * Classe base abstracta que representa un usuari dins del sistema.
 *
 * <p>Defineix atributs comuns com l’identificador únic i el nom visible,
 * a més de proporcionar mètodes d’accés i modificació. Les classes derivades
 * han d’estendre aquesta classe per afegir comportaments o dades addicionals.</p>
 */
public abstract class User {

    /** Identificador únic de l’usuari. */
    protected String id;

    /** Nom visible de l’usuari. */
    protected String displayName;

    /**
     * Crea un nou usuari amb un ID i un nom visible.
     *
     * @param id           Identificador únic de l’usuari.
     * @param displayName  Nom visible associat a l’usuari.
     */
    public User(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /**
     * Retorna l’identificador de l’usuari.
     *
     * @return ID de l’usuari.
     */
    public String getId() {
        return id;
    }

    /**
     * Retorna el nom visible de l’usuari.
     *
     * @return Nom mostrat públicament.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Canvia el nom visible de l’usuari.
     *
     * @param name Nou nom visible.
     */
    public void changeDisplayName(String name) {
        displayName = name;
    }
}
