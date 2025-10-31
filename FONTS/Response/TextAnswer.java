package Response; 

import Exceptions.InvalidArgumentException.java;
import Exceptions.NullArgumentException.java;

public final class TextAnswer extends Answer {
    private final String value; 

    public TextAnswer(int questionId, String value) {
        super(questionId); 
        //no pot ser null 
        if (value == null) {
            throw new NullArgumentException("Text answer cannot be null");
        }
        if (value.TYPE != String) {
            throw new InvalidArgumentException("Text answer must be a string");
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