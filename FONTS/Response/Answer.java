package Response; 


import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.List;
import FONTS.Exceptions.InvalidArgumentException; // Import assumit
import FONTS.Exceptions.NullArgumentException; // Import assumit



public abstract class Answer {
    private final int questionId; 

    



    public Answer(int questionId) {
        //si el sistema el dona no se si cal posar la excepcio 
        if (questionId < 0) {
            throw new IllegalArgumentException("id no pot ser negatiu"); 
        }
        this.questionId = questionId; 

    }

    //getters
    public int getQuestionId() { return questionId; }

    public enum Type {
        TEXT, 
        INT, 
        MULTIPLE_CHOICE, 
        SINGLE_CHOICE
    } 

    
    //tipus de resposta 
    public abstract Type getType();
    public abstract boolean isEmpty();
    


    //if it is text 
    public static TextAnswer TEXT(int questionId, String value) {
        return new TextAnswer(questionId, value);
    }
    //if it is an integer
    public static IntAnswer INT(int questionId, int value) {
        return new IntAnswer(questionId, value);
    }


    //if it is multiple choice, options separated by commas
    public static MultipleChoiceAnswer MULTIPLE_CHOICE(int questionId, String optionIdsCsv) {
        return new MultipleChoiceAnswer(questionId, optionIdsCsv);
    }

    //if it is multiple choice, and collection of integers options 
    public static MultipleChoiceAnswer MULTIPLE_CHOICE(int questionId, Collection<Integer> optionIds) {
        return new MultipleChoiceAnswer(questionId, new ArrayList<>(optionIds));
    }


    //if it is single choice
    public static SingleChoiceAnswer SINGLE_CHOICE(int questionId, int optionId) {
        return new SingleChoiceAnswer(questionId, optionId); 
    }
   





}



