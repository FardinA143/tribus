package Survey;

/**
 * Pregunta oberta que captura enters dins d'un rang determinat.
 */
public class OpenIntQuestion extends Question {
    private int min;
    private int max;

    /**
     * Defineix la pregunta numèrica amb límits.
     *
     * @param id       identificador intern
     * @param text     literal mostrat
     * @param required si és obligatòria
     * @param position ordre dins l'enquesta
     * @param weight   pes analític
     * @param min      valor mínim acceptat
     * @param max      valor màxim acceptat
     */
    public OpenIntQuestion(int id, String text, boolean required, int position, double weight, int min, int max) {
        super(id, text, required, position, weight);
        this.min = min;
        this.max = max;
    }

    /**
     * @return el valor mínim admès
     */
    public int getMin() {
        return min;
    }

    /**
     * Ajusta el mínim (per imports/proves).
     *
     * @param min nou límit inferior
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * @return el valor màxim admès
     */
    public int getMax() {
        return max;
    }

    /**
     * Ajusta el màxim permès.
     *
     * @param max nou límit superior
     */
    public void setMax(int max) {
        this.max = max;
    }
}
