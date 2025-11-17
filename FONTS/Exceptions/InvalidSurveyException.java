package Exceptions;

public class InvalidSurveyException extends SurveyException {
    private static final long serialVersionUID = 1L;
    public InvalidSurveyException() {
        super("Enquesta invàlida");
    }

    public InvalidSurveyException(String detail) {
        super("Enquesta: " + detail);
    }
}

