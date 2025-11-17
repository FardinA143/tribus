package Exceptions;

public class NullArgumentException extends Exception {
    private static final long serialVersionUID = 1L;
    public NullArgumentException(String message) {
        super("Argument nul: " + message);
    }

    public NullArgumentException() {
        super("Argument nul");
    }
}
