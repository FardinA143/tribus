package Exceptions;

public class InvalidQuestionException extends SurveyException {
    private static final long serialVersionUID = 1L;
    public InvalidQuestionException() {
        super("Pregunta invàlida");
    }

    public InvalidQuestionException(String detail) {
        super("Pregunta: " + detail);
    }
}

