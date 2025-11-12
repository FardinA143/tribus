package Response;

import java.util.Objects;

import FONTS.Exceptions.InvalidArgumentException;

public final class SingleChoiceAnswer extends Answer {
    private final int optionId; 
    

    public SingleChoiceAnswer(int questionId, int optionId) {
        super(questionId); 

        if (optionId < 0) {
            throw new InvalidArgumentException("optionId cannot be negative for q=:" + questionId);
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