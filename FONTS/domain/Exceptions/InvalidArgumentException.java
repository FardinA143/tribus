package Exceptions;

/**
 * S'emet quan un argument té un valor no vàlid pel context d'execució.
 */
public class InvalidArgumentException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció indicant la causa del valor invàlid.
     *
     * @param message text descriptiu del problema detectat.
     */
    public InvalidArgumentException(String message) {
        super("Argument incorrecte: " + message);
    }

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public InvalidArgumentException() {
        super("Argument incorrecte");
    }
}
