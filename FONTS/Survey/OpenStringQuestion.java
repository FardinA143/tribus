package Survey;

public class OpenStringQuestion extends Question {
    private int maxLength;

    public OpenStringQuestion(int id, String text, boolean required, int position, double weight, int maxLength) {
        super(id, text, required, position, weight);
        this.maxLength = maxLength;
    }

    // Getters and setters
    public int getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
