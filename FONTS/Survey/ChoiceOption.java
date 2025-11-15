package Survey;

public class ChoiceOption {
    private int id;
    private String label;

    public ChoiceOption(int id, String label) {
        this.id = id;
        this.label = label;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    // probablemente terminemos no usando los setters, pero los dejo por si acaso.

    public void setId(int id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
