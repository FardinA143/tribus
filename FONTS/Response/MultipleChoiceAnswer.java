package Response; 

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;

public final class MultipleChoiceAnswer extends Answer {
    private final List<Integer> optionIds;


    public MultipleChoiceAnswer(int questionId, String optionIdsCsv) { // split the csv string into a list of integers
        super(questionId); 
        if (questionId < 0) {
             throw new NullArgumentException("ID List cannot be null");
        } 

        for (Integer id : optionIds) {
            if (id == null || id < 0) {
              throw new InvalidArgumentException("optionId negatiu o null");

            }
        }
        this.optionIds = Collection.unmodifiableList(new ArrayList<>(optionIds)); 
    }

    //return string with mutliple answers 
    public MultipleChoiceAnswer(int questionId, Collection<Integer> optionIds) {
        this(questionId, parseCsv(optionIds)); 
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