package Exceptions;

/**
 * Classe base per totes les excepcions específiques del domini d'enquestes.
 */
public class SurveyException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Crea una excepció amb el missatge proporcionat.
     *
     * @param message text descriptiu del problema.
     */
    public SurveyException(String message) {
        super(message);
    }

    /**
     * Crea una excepció amb missatge i causa original.
     *
     * @param message text descriptiu del problema.
     * @param cause causa arrel a encadenar.
     */
    public SurveyException(String message, Throwable cause) {
        super(message, cause);
    }
}
