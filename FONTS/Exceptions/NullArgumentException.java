package Exceptions;

/**
 * Es llança quan un argument obligatori rep el valor {@code null}.
 */
public class NullArgumentException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció amb el nom de l'argument nul.
     *
     * @param message descripció del paràmetre o context.
     */
    public NullArgumentException(String message) {
        super("Argument nul: " + message);
    }

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public NullArgumentException() {
        super("Argument nul");
    }
}
