package Exceptions;

public class EmptyArgumentException extends Exception {
    private static final long serialVersionUID = 1L;
    public EmptyArgumentException(String message) {
        super("Argument buit: " + message);
    }

    public EmptyArgumentException() {
        super("Argument buit");
    }
}
