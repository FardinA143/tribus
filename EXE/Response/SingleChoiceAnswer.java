package Response; 

public final class SingleChoiseAnswer extends Answer {
    private final int optionId; 
    

    public SingleChoiceAnswer(int questionId, int optionId) {
        super(questionId); 
        if (optionId < 0) {
            throw new IllegalArgumentException("optionId cannot no be negative");
        }
        this.optionId = optionId; 
    }

    public int getOptionId() {
        return optionId; 
    }
    //tipe of answer
    @Override public Type getType() { return Type.SINGLE_CHOICE; }

    @Override public boolean isEmpty() { return false; }

//returns the string representation of the object
@Override public String toString() {
        return "SingleChoiceAnswer{q=" + getQuestionId() + ", optionId=" + optionId + "}";
    }





}