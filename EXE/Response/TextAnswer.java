package response; 


public final class TextAnswer extends Answer {
    private final String value; 

    public TextAnswer(int questionId, String value) {
        super(questionId); 
        //no pot ser null 
        if (value == null) {
            throw new IllegalArgumentException("El valor no pot ser null");
        }

        this.value = value; 
    }


    //gets the value 
    public String getValue() {
        return value; 
    }

    //returns the type of the answer
    @Override
    public Type getType() {
        return Type.TEXT;
    }

    //returns if the answer is empty
    @Override
    public boolean isEmpty() {
        return value.trim().isEmpty();
    }

    //string representation of the object
    @Override 
    public String toString() { 
        return "TextAnswer{q=" + getQuestionId() + ", value=\"" + value + "\"}"; 
    }
}