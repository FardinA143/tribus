package response; 


import java.util.*;
import java.util.stream.Collectors;


public abstract class Answer {
    private int questionId; 


    public Answer(int questionId) {
        if (questionId < 0) {
            throw new IllegalArgumentException("id no pot ser negatiu"); 
        }
        this.questionId = questionId; 

    }

    //getters

    public int getQuestionId() {
        return questionId;
    }

    //tipus de resposta 
    public abstract Type getType();
    public abstract boolean isEmpty();
    public enum Type {
        TEXT, 
        INT, 
        MULTIPLE_CHOISE, 
        SINGLE_CHOISE
    } 



    //if it is text 
    public static TextAnswer text(int questionId, String value) {
        return new TextAnswer(questionId, value);
    }
    //if it is an integer
    public static IntAnswer integer(int questionId, int value) {
        return new IntAnswer(questionId, value);
    }


    //if it is multiple choice, options separated by commas
    public static MultipleChoiceAnswer multipleChoise(int questionId, String optionIdsCsv) {
        return new MultipleChoiceAnswer(questionId, optionIdsCsv);
    }
 //if it is multiple choice, and collection of integers options 

    public static MultipleChoiceAnswer multipleChoise(int questionId, Collection<Integer> optionIds, Set<Integer> validOptionIds) {
        return new MultipleChoiceAnswer(questionId, optionIds, validOptionIds);
    }


    //if it is single choice
    public static SingleChoiceAnswer singleChoise(int questionId, int optionId, Set<Integer> validOptionIds) {
        return new SingleChoiceAnswer(questionId, optionId, validOptionIds);
    }

    public static EmptyAnswer empty(int questionId) {
        return new EmptyAnswer(questionId);
    }
   





}



