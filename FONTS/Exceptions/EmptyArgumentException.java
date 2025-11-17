package Exceptions;

/**
 * Excepció llançada quan un argument requerit està present però buit.
 */
public class EmptyArgumentException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Crea l'excepció indicant quin argument es considera buit.
     *
     * @param message descripció addicional del problema.
     */
    public EmptyArgumentException(String message) {
        super("Argument buit: " + message);
    }

    /**
     * Crea l'excepció amb un missatge genèric.
     */
    public EmptyArgumentException() {
        super("Argument buit");
    }
}
