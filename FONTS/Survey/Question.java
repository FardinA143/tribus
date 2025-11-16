package Survey;

/**
 * Classe base abstracta per a totes les preguntes d'una enquesta.
 * Defineix les propietats comunes com l'identificador, text, obligatorietat, posició i pes.
 */
public abstract class Question {
    private int id;
    private String text;
    private boolean required;
    private int position;
    private double weight;

    /**
     * Crea una pregunta amb les propietats bàsiques.
     *
     * @param id       identificador únic de la pregunta
     * @param text     text de la pregunta
     * @param required si la pregunta és obligatòria
     * @param position posició en l'enquesta
     * @param weight   pes per a càlculs d'anàlisi
     */
    public Question(int id, String text, boolean required, int position, double weight) {
        this.id = id;
        this.text = text;
        this.required = required;
        this.position = position;
        this.weight = weight;
    }

    /**
     * @return l'identificador de la pregunta
     */
    public int getId() {
        return id;
    }

    /**
     * Defineix un nou identificador (per imports/proves).
     *
     * @param id nou valor
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return el text que es mostra a l'usuari
     */
    public String getText() {
        return text;
    }

    /**
     * Actualitza el text de la pregunta.
     *
     * @param text nou text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @return cert si la pregunta és obligatòria
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Marca la pregunta com a obligatòria o opcional.
     *
     * @param required nou valor
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * @return la posició dins de l'enquesta
     */
    public int getPosition() {
        return position;
    }

    /**
     * Actualitza la posició (ús intern quan es reordenen preguntes).
     *
     * @param position nova posició base 1
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * @return el pes utilitzat per mètriques d'anàlisi
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Defineix un nou pes.
     *
     * @param weight valor positiu utilitzat en càlculs
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
}
