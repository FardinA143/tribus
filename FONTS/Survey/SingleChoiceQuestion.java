package Survey;

import java.util.*;

/**
 * Pregunta de selecció única que ofereix un conjunt tancat d'opcions.
 */
public class SingleChoiceQuestion extends Question {
    private List<ChoiceOption> options;

    /**
     * Crea la pregunta sense opcions inicials.
     */
    public SingleChoiceQuestion(int id, String text, boolean required, int position, double weight) {
        super(id, text, required, position, weight);
        this.options = new ArrayList<>();
    }

    /**
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
