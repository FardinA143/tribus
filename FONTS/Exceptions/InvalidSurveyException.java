package Exceptions;

/**
 * Es llança quan una enquesta no compleix el format o les regles esperades.
 */
public class InvalidSurveyException extends SurveyException {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public InvalidSurveyException() {
        super("Enquesta invàlida");
    }

    /**
     * Crea l'excepció indicant el motiu concret.
     *
     * @param detail descripció addicional del problema.
     */
    public InvalidSurveyException(String detail) {
        super("Enquesta: " + detail);
    }
}

