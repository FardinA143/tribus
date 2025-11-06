package Survey;

import java.util.*;

public class MultipleChoiceQuestion extends Question {
    private int minChoices;
    private int maxChoices;
    private List<ChoiceOption> options;

    public MultipleChoiceQuestion(int id, String text, boolean required, int position, double weight, int minChoices, int maxChoices) {
        super(id, text, required, position, weight);
        this.minChoices = minChoices;
        this.maxChoices = maxChoices;
        this.options = new ArrayList<>();
    }

    // Getters and setters
    public int getMinChoices() {
        return minChoices;
    }

    public void setMinChoices(int minChoices) {
        this.minChoices = minChoices;
    }

    public int getMaxChoices() {
        return maxChoices;
    }

    public void setMaxChoices(int maxChoices) {
        this.maxChoices = maxChoices;
    }

    public List<ChoiceOption> getOptions() {
        return options;
    }

    public void addOption(ChoiceOption option) {
        this.options.add(option);
    }
}
