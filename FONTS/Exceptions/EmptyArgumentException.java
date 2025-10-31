package Exceptions;

public class EmptyArgumentException extends Exception {
    public EmptyArgumentException() {
        super("Empty argument provided");
    }
}
