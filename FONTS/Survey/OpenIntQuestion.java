package Survey;

public class OpenIntQuestion extends Question {
    private int min;
    private int max;

    public OpenIntQuestion(int id, String text, boolean required, int position, double weight, int min, int max) {
        super(id, text, required, position, weight);
        this.min = min;
        this.max = max;
    }

    // Getters and setters
    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }
}
