package Response; 

import java.util.*;
import java.util.stream.Collectors;

public final class MultipleChoiceAnswer extends Answer {
    private final List<Integer> optionIds;


    public MultipleChoiceAnswer(int questionId, Collection <Integer> optionIds) {
         super(questionId); 
         if (optionIds == null) {
             throw new IllegalArgumentException("optionIds cannot be null")
         }

        for (Integer id : optionIds) {
            if (id == null || id < 0) {
              throw new IllegalArgumentException("optionId negatiu o null");
            }
        }
        this.optionIds = Collection.unmodifiableList(new ArrayList<>(optionIds)); 
    }

    //return string with mutliple answers 
    public MultipleChoiceAnswer(int questionId, string optionIdsCsv) {
        this(questionId, parsercCsv(optionIdsCsv)); 
    }

    public List<Integer> getOptionIds() {
        return optionIds; 
    }

    //chat easy to export 
    public String optionIdsCsv() {
        return optionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override public Type getType() { return Type.MULTIPLE_CHOICE; }

    @Override public boolean isEmpty() { return optionIds.isEmpty(); }

    @Override public String toString() {
        return "MultipleChoiceAnswer{q=" + getQuestionId() + ", options=" + optionIds + "}";
    }


















}