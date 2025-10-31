package Survey;

import java.util.*;

public class SingleChoiceQuestion extends Question {
    private List<ChoiceOption> options;

    public SingleChoiceQuestion(int id, String text, boolean required, int position, double weight) {
        super(id, text, required, position, weight);
        this.options = new ArrayList<>();
    }

    // Getters and setters
    public List<ChoiceOption> getOptions() {
        return options;
    }

    public void addOption(ChoiceOption option) {
        this.options.add(option);
    }
}
