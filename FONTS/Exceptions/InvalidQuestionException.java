package Exceptions;

/**
 * Indica que la pregunta processada no és vàlida dins l'enquesta.
 */
public class InvalidQuestionException extends SurveyException {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public InvalidQuestionException() {
        super("Pregunta invàlida");
    }

    /**
     * Crea l'excepció especificant la causa concreta.
     *
     * @param detail informació addicional sobre el problema detectat.
     */
    public InvalidQuestionException(String detail) {
        super("Pregunta: " + detail);
    }
}

