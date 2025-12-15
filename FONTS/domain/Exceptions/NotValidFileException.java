package Exceptions;

/**
 * Indica que el fitxer de dades no és vàlid o conté errors.
 */
public class NotValidFileException extends SurveyException {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public NotValidFileException() {
        super("Fitxer no vàlid");
    }

    /**
     * Crea l'excepció especificant el motiu.
     *
     * @param detail detalls addicionals del problema detectat.
     */
    public NotValidFileException(String detail) {
        super("Fitxer no vàlid: " + detail);
    }
}
