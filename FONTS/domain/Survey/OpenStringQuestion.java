package Survey;

/**
 * Pregunta oberta que captura respostes de text lliure amb una longitud màxima.
 */
public class OpenStringQuestion extends Question {
    private int maxLength;

    /**
     * Construeix la pregunta amb la longitud màxima permesa.
     *
     * @param id        identificador intern
     * @param text      literal mostrat
     * @param required  si és obligatòria
     * @param position  ordre dins l'enquesta
     * @param weight    pes analític
     * @param maxLength longitud màxima acceptada
     */
    public OpenStringQuestion(int id, String text, boolean required, int position, double weight, int maxLength) {
        super(id, text, required, position, weight);
        this.maxLength = maxLength;
    }

    /**
     * Mostra la longitud màxima acceptada per la resposta.
     *
     * @return la longitud màxima permitida
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Actualitza la llargada màxima (per imports/proves).
     *
     * @param maxLength nou límit
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
