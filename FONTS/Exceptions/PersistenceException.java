package Exceptions;

/**
 * Excepció genèrica per a errors en la capa de persistència.
 */
public class PersistenceException extends SurveyException {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb el missatge detallat rebut.
     *
     * @param message descripció del problema de persistència.
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Crea l'excepció amb la font de dades afectada i el detall concret.
     *
     * @param entity nom del recurs afectat.
     * @param detail informació addicional de l'error.
     */
    public PersistenceException(String entity, String detail) {
        super(entity + ": " + detail);
    }
}
