package Exceptions;

public class QuestionNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    public QuestionNotFoundException(String message) {
        super(message);
    }

    public QuestionNotFoundException(int questionId) {
        super("Pregunta " + questionId + " no trobada");
    }
}

