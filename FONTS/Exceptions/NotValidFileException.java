package Exceptions;

public class NotValidFileException extends SurveyException {
    private static final long serialVersionUID = 1L;
    public NotValidFileException() {
        super("Fitxer no vàlid");
    }

    public NotValidFileException(String detail) {
        super("Fitxer no vàlid: " + detail);
    }
}
