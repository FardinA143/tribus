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
     *
     * @param id identificador únic de la pregunta.
     * @param text literal que es mostrarà a l'usuari.
     * @param required si la pregunta és obligatòria.
     * @param position posició dins l'enquesta.
     * @param weight pes utilitzat per mètriques futures.
     * @param minChoices mínim d'opcions que l'usuari ha de marcar.
     * @param maxChoices màxim d'opcions permeses.
     */
    public MultipleChoiceQuestion(int id, String text, boolean required, int position, double weight, int minChoices, int maxChoices) {
        super(id, text, required, position, weight);
        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
        this.options = new ArrayList<>();
    }

    /**
     * Crea la pregunta amb una llista d'opcions inicials (útil per imports).
     * Es fa una còpia defensiva de la llista proporcionada.
     *
     * @param id identificador únic de la pregunta.
     * @param text literal que es mostra a l'usuari.
     * @param required si és obligatòria.
     * @param position posició dins de l'enquesta.
     * @param weight pes per a càlculs.
     * @param minChoices mínim d'opcions requerides.
     * @param maxChoices màxim d'opcions permeses.
     * @param initialOptions opcions inicials, es copien defensivament.
     */
    public MultipleChoiceQuestion(int id, String text, boolean required, int position, double weight, int minChoices, int maxChoices, List<ChoiceOption> initialOptions) {
        super(id, text, required, position, weight);
        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
        if (initialOptions == null) {
            this.options = new ArrayList<>();
        } else {
            this.options = new ArrayList<>(initialOptions);
        }
    }

    /**
     * Obté el límit inferior d'opcions que cal marcar.
     *
     * @return el nombre mínim d'opcions que cal marcar
     */
    public int getMinChoices() {
        return minChoices;
    }

    /**
     * Actualitza el mínim requerit.
     *
     * @param minChoices nou valor mínim obligatori.
     */
    public void setMinChoices(int minChoices) {
        this.minChoices = minChoices;
    }

    /**
     * Proporciona el límit superior d'opcions permeses.
     *
     * @return el màxim d'opcions permeses
     */
    public int getMaxChoices() {
        return maxChoices;
    }

    /**
     * Defineix un nou màxim permis.
     *
     * @param maxChoices valor màxim a establir.
     */
    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices;
    }

    /**
     * Retorna la llista editable d'opcions de resposta.
     *
     * @return la llista mutable d'opcions
     */
    public List<ChoiceOption> getOptions() {
        return options;
    }

    /**
     * Afegeix una opció addicional.
     *
     * @param option nova opció a incorporar.
     */
    public void addOption(ChoiceOption option) {
        this.options.add(option);
    }
}
