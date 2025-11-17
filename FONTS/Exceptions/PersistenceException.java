package Exceptions;

public class PersistenceException extends SurveyException {
    private static final long serialVersionUID = 1L;
    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String entity, String detail) {
        super(entity + ": " + detail);
    }
}
