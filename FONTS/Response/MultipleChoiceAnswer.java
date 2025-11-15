package Response; 

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;

public final class MultipleChoiceAnswer extends Answer {
    private final List<Integer> optionIds;


    public MultipleChoiceAnswer(int questionId, Collection<Integer> optionIds) throws InvalidArgumentException, NullArgumentException {
        super(questionId); 
        if (optionIds == null) {
             throw new NullArgumentException("ID List cannot be null");
        } 

        for (Integer id : optionIds) {
            if (id == null || id < 0) {
              throw new InvalidArgumentException("optionId negatiu o null");
            }
        }
        this.optionIds = Collections.unmodifiableList(new ArrayList<>(optionIds)); 
    }

    //return string with mutliple answers 
    public MultipleChoiceAnswer(int questionId, String optionIdsCsv) throws InvalidArgumentException, NullArgumentException {
        this(questionId, parseCsv(optionIdsCsv)); 
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

    private static Collection<Integer> parseCsv(String optionIdsCsv) {
        if (optionIdsCsv == null || optionIdsCsv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Stream.of(optionIdsCsv.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }


















}