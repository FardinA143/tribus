package Survey;

import java.util.*;

/**
 * Pregunta de selecció única que ofereix un conjunt tancat d'opcions.
 */
public class SingleChoiceQuestion extends Question {
    private List<ChoiceOption> options;

    /**
     * Crea la pregunta sense opcions inicials.
     *
     * @param id identificador únic de la pregunta.
     * @param text literal que veurà l'usuari.
     * @param required indica si és obligatòria.
     * @param position ordre dins de l'enquesta.
     * @param weight pes utilitzat per mètriques.
     */
    public SingleChoiceQuestion(int id, String text, boolean required, int position, double weight) {
        super(id, text, required, position, weight);
        this.options = new ArrayList<>();
    }

    /**
     * Crea la pregunta amb una llista d'opcions inicials (útil per imports).
     *
     * @param id identificador únic de la pregunta.
     * @param text literal que es mostra.
     * @param required indica si és obligatòria.
     * @param position posició dins de l'enquesta.
     * @param weight pes utilitzat en les mètriques.
     * @param initialOptions llista d'opcions a prémer; es fa una còpia defensiva.
     */
    public SingleChoiceQuestion(int id, String text, boolean required, int position, double weight, List<ChoiceOption> initialOptions) {
        super(id, text, required, position, weight);
        if (initialOptions == null) {
            this.options = new ArrayList<>();
        } else {
            this.options = new ArrayList<>(initialOptions);
        }
    }

    /**
     * Obté la col·lecció editable d'opcions definides.
     *
     * @return llista mutable d'opcions configurades
     */
    public List<ChoiceOption> getOptions() {
        return options;
    }

    /**
     * Afegeix una nova opció disponible.
     *
     * @param option opció a incloure
     */
    public void addOption(ChoiceOption option) {
        this.options.add(option);
    }
}
