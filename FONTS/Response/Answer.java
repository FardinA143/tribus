package Response; 


import java.util.*;
import java.util.stream.Collectors;


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

    public int getQuestionId() {
        return questionId;
    }

    public enum Type {
        TEXT, 
        INT, 
        MULTIPLE_CHOISE, 
        SINGLE_CHOISE
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
    public static MultipleChoiceAnswer MULTIPLE_CHOISE(int questionId, String optionIdsCsv) {
        return new MultipleChoiceAnswer(questionId, optionIdsCsv);
    }
 //if it is multiple choice, and collection of integers options 

    public static MultipleChoiceAnswer MULTIPLE_CHOISE(int questionId, Collection<Integer> optionIds, ) {
        return new MultipleChoiceAnswer(questionId, optionIds);
    }


    //if it is single choice
    public static SingleChoiceAnswer SINGLE_CHOISE(int questionId, int optionId) {
        return new SingleChoiceAnswer(questionId, optionId); 
    }
   





}



