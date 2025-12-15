package Exceptions;

/**
 * Indica que la pregunta referenciada no existeix en l'enquesta.
 */
public class QuestionNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb un missatge personalitzat.
     *
     * @param message text descriptiu del problema.
     */
    public QuestionNotFoundException(String message) {
        super(message);
    }

    /**
     * Crea l'excepció indicant l'identificador de la pregunta.
     *
     * @param questionId identificador de la pregunta no trobada.
     */
    public QuestionNotFoundException(int questionId) {
        super("Pregunta " + questionId + " no trobada");
    }
}

