package user;

import java.time.LocalDateTime;

/**
 * Representa un usuari convidat dins del sistema.
 *
 * <p>Un {@code GuestUser} és un tipus d’usuari temporal que disposa d’una data
 * d’expiració, a partir de la qual deixa de ser vàlid. Extén la classe
 * {@link User} afegint-hi el camp {@code expiresAt}.</p>
 */
public class GuestUser extends User {

    /** Data i hora en què expira l’usuari convidat. */
    private String expiresAt;

    /**
     * Crea un nou usuari convidat amb un temps d’expiració determinat.
     *
     * @param id           Identificador únic de l’usuari.
     * @param displayName  Nom visible de l’usuari convidat.
     * @param expiresAt    Data i hora d’expiració de l’usuari
     *                     (es recomana el format ISO-8601).
     */
    public GuestUser(String id, String displayName, String expiresAt) {
        super(id, displayName);
        this.expiresAt = expiresAt;
    }

    /**
     * Retorna la data i hora en què expira l’usuari convidat.
     *
     * @return Cadena amb la data d’expiració.
     */
    public String getExpiresAt() {
        return expiresAt;
    }
}
