package Exceptions;

public class SurveyException extends Exception {
    private static final long serialVersionUID = 1L;
    public SurveyException(String message) {
        super(message);
    }

    public SurveyException(String message, Throwable cause) {
        super(message, cause);
    }
}
