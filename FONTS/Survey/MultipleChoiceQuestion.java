package Survey;

import java.util.*;

/**
 * Pregunta de selecció múltiple amb límits mínim i màxim de seleccions.
 */
public class MultipleChoiceQuestion extends Question {
    private int minChoices;
    private int maxChoices;
    private List<ChoiceOption> options;

    /**
     * Construeix la pregunta indicant el rang de seleccions permeses.
     */
    public MultipleChoiceQuestion(int id, String text, boolean required, int position, double weight, int minChoices, int maxChoices) {
        super(id, text, required, position, weight);
        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
        this.options = new ArrayList<>();
    }

    /**
     * @return el nombre mínim d'opcions que cal marcar
     */
    public int getMinChoices() {
        return minChoices;
    }

    /**
     * Actualitza el mínim requerit.
     */
    public void setMinChoices(int minChoices) {
        this.minChoices = minChoices;
    }

    /**
     * @return el màxim d'opcions permeses
     */
    public int getMaxChoices() {
        return maxChoices;
    }

    /**
     * Defineix un nou màxim permis.
     */
    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices;
    }

    /**
     * @return la llista mutable d'opcions
     */
    public List<ChoiceOption> getOptions() {
        return options;
    }

    /**
     * Afegeix una opció addicional.
     */
    public void addOption(ChoiceOption option) {
        this.options.add(option);
    }
}
