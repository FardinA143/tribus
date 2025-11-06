package Survey;

public abstract class Question {
    private int id;
    private String text;
    private boolean required;
    private int position;
    private double weight;

    public Question(int id, String text, boolean required, int position, double weight) {
        this.id = id;
        this.text = text;
        this.required = required;
        this.position = position;
        this.weight = weight;
    }

    // Getters and setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public int getPosition() {
            return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }
}
