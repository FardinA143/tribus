package Response;

import Exceptions.InvalidArgumentException;

/**
 * Representa una resposta a una pregunta de selecció única.
 * Només emmagatzema un únic identificador d'opció.
 */
public final class SingleChoiceAnswer extends Answer {
    
    /** L'identificador de l'opció seleccionada. */
    private final int optionId; 
    

    /**
     * Constructor per a SingleChoiceAnswer.
     * @param questionId ID de la pregunta.
     * @param optionId L'ID de l'opció triada.
     * @throws InvalidArgumentException si optionId és negatiu.
     */
    public SingleChoiceAnswer(int questionId, int optionId) throws InvalidArgumentException {
        super(questionId); 

        // Validació de precondició: l'ID d'opció ha de ser no negatiu.
        if (optionId < 0) {
            throw new InvalidArgumentException("optionId cannot be negative for q=:" + questionId);
        }
    
        this.optionId = optionId; 
    }

    /**
     * Retorna l'ID de l'opció seleccionada.
     * @return L'ID de l'opció.
     */
    public int getOptionId() {
        return optionId; 
    }
    
    /**
     * Retorna el tipus de resposta SINGLE_CHOICE.
     * @return Type.SINGLE_CHOICE.
     */
    @Override public Type getType() { return Type.SINGLE_CHOICE; }

    /**
     * Una resposta d'opció única (amb un ID no negatiu) sempre es considera contestada.
     * @return Sempre fals.
     */
    @Override public boolean isEmpty() { return false; }

    /**
     * Retorna la representació en String de l'objecte.
     * @return String amb l'ID de la pregunta i l'ID de l'opció.
     */
    @Override public String toString() {
        return "SingleChoiceAnswer{q=" + getQuestionId() + ", optionId=" + optionId + "}";
    }
}