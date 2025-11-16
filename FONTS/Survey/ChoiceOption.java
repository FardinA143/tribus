package Survey;

/**
 * Representa una opció seleccionable dins d'una pregunta de tipus "single" o "multi".
 * Cada opció conté un identificador numèric i una etiqueta descriptiva.
 */
public class ChoiceOption {
    private int id;
    private String label;

    /**
     * Crea una opció identificada.
     *
     * @param id    identificador únic dins de la pregunta
     * @param label text mostrable a l'usuari
     */
    public ChoiceOption(int id, String label) {
        this.id = id;
        this.label = label;
    }

    /**
     * @return l'identificador intern de l'opció
     */
    public int getId() {
        return id;
    }

    /**
     * @return el text visible associat a l'opció
     */
    public String getLabel() {
        return label;
    }

    /**
     * Defineix un nou identificador. S'utilitza principalment per imports o proves.
     *
     * @param id nou valor
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Actualitza l'etiqueta mostrada.
     *
     * @param label nou text
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
