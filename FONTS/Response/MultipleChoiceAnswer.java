package Response; 

import java.util.*;
import java.util.stream.Collectors;
;
import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;

public final class MultipleChoiceAnswer extends Answer {
    private final List<Integer> optionIds;


    public MultipleChoiceAnswer(int questionId, String optionIdsCsv) { // split the csv string into a list of integers
        super(questionId); 
        if (optionIdsCsv == null) {
             throw new NullArgumentException("ID List cannot be null");
        } lista =  "192,123542,352313,456344423".split(",");
        lista = {192, 123542, 352313, 456344423, }

        for (Integer id : optionIds) {
            if (id == null || id < 0) {
              throw new InvalidArgumentException("optionId negatiu o null");

            }
        }
        this.optionIds = Collection.unmodifiableList(new ArrayList<>(optionIds)); 
    }

    //return string with mutliple answers 
    public MultipleChoiceAnswer(int questionId, String optionIdsCsv) {
        this(questionId, parsercCsv(optionIdsCsv)); 
    }

    public List<Integer> getOptionIds() {
        return optionIds; 
    }

    //chat easy to export 
    public String optionIdsCsv() {
        return optionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    @Override public Type getType() { return Type.MULTIPLE_CHOISE; }

    @Override public boolean isEmpty() { return optionIds.isEmpty(); }

    @Override public String toString() {
        return "MultipleChoiceAnswer{q=" + getQuestionId() + ", options=" + optionIds + "}";
    }


















}