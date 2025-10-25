package response;

public final class IntAnswer extends Answer {
    private final int value;

    public IntAnswer(int questionId, int value) { 
        super(questionId); 
        this.value = value; 
    }

    public int getValue() { 
        return value; 
    }



    @Override public Type getType() { 
        return Type.INT; 
    }
    @Override public boolean isEmpty() { 
        return false; 
    }
    @Override public String toString() {
         return "IntAnswer{q=" + getQuestionId() + ", value=" + value + "}"; 
    }
}
