package Response; 

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;

/**
 * Representa una resposta a una pregunta de selecció múltiple.
 * Emmagatzema una llista d'identificadors d'opcions seleccionades.
 */
public final class MultipleChoiceAnswer extends Answer {
    
    /** Llista no modificable dels IDs de les opcions seleccionades. */
    private final List<Integer> optionIds;


    /**
     * Constructor principal que rep la col·lecció d'IDs d'opció.
     * @param questionId ID de la pregunta.
     * @param optionIds Col·lecció dels IDs d'opció seleccionats.
     * @throws InvalidArgumentException si algun ID d'opció és negatiu.
     * @throws NullArgumentException si la col·lecció d'IDs és null.
     */
    public MultipleChoiceAnswer(int questionId, Collection<Integer> optionIds) throws InvalidArgumentException, NullArgumentException {
        super(questionId); 
        
        // Validació de precondició: la col·lecció no pot ser null.
        if (optionIds == null) {
             throw new NullArgumentException("ID List cannot be null");
        } 

        // Validació d'integritat: cap ID d'opció pot ser null o negatiu.
        for (Integer id : optionIds) {
            if (id == null || id < 0) {
              throw new InvalidArgumentException("optionId negatiu o null");
            }
        }
        // Emmagatzemem una llista no modificable per garantir la immutabilitat.
        this.optionIds = Collections.unmodifiableList(new ArrayList<>(optionIds)); 
    }

    /**
     * Constructor alternatiu que rep els IDs de les opcions com a string CSV.
     * @param questionId ID de la pregunta.
     * @param optionIdsCsv String amb els IDs separats per comes.
     * @throws InvalidArgumentException si algun ID d'opció és negatiu o no és un enter.
     * @throws NullArgumentException si el string CSV és null.
     */
    public MultipleChoiceAnswer(int questionId, String optionIdsCsv) throws InvalidArgumentException, NullArgumentException {
        // El constructor delega el parsing i la validació al constructor principal.
        this(questionId, parseCsv(optionIdsCsv)); 
    }

    /**
     * Retorna la llista d'IDs d'opció seleccionats.
     * @return Una llista d'enters no modificable.
     */
    public List<Integer> getOptionIds() {
        return optionIds; 
    }

    /**
     * Converteix la llista d'IDs d'opció a un format CSV.
     * @return String amb els IDs separats per comes.
     */
    public String optionIdsCsv() {
        return optionIds.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * Retorna el tipus de resposta MULTIPLE_CHOICE.
     * @return Type.MULTIPLE_CHOICE.
     */
    @Override public Type getType() { return Type.MULTIPLE_CHOICE; }

    /**
     * Indica si la resposta és buida (no s'ha seleccionat cap opció).
     * @return Cert si la llista d'opcions seleccionades és buida.
     */
    @Override public boolean isEmpty() { return optionIds.isEmpty(); }

    /**
     * Retorna la representació en String de l'objecte.
     * @return String amb l'ID de la pregunta i els IDs d'opció seleccionats.
     */
    @Override public String toString() {
        return "MultipleChoiceAnswer{q=" + getQuestionId() + ", options=" + optionIds + "}";
    }

    /**
     * Mètode auxiliar per parsejar un string CSV en una col·lecció d'enters.
     * @param optionIdsCsv El string CSV.
     * @return Col·lecció d'enters.
     */
    private static Collection<Integer> parseCsv(String optionIdsCsv) {
        // Si el CSV és nul o buit, retorna una llista buida.
        if (optionIdsCsv == null || optionIdsCsv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // Utilitzem streams per dividir, netejar espais i convertir a enters.
        // Si parseInt falla (no és un enter), es propagarà una NumberFormatException.
        return Stream.of(optionIdsCsv.split(","))
            .map(String::trim)
            .map(Integer::parseInt)
            .collect(Collectors.toList());
    }
}