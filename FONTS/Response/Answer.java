package Response; 


import java.util.ArrayList;
import java.util.Collection;
import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;


/**
 * Classe abstracta base per a totes les respostes (Answer) a una pregunta d'una enquesta.
 * Proporciona l'estructura fonamental (Question ID) i mètodes de fàbrica estàtics 
 * per a la creació dels subtipus de resposta.
 */

public abstract class Answer {

    /** Identificador de la pregunta a la qual pertany aquesta resposta. */
    private final int questionId; 


    /**
     * Constructor de la classe abstracta Answer.
     * @param questionId Identificador de la pregunta. Ha de ser no negatiu.
     * @throws IllegalArgumentException si questionId és negatiu.
     */

    public Answer(int questionId) {
      
        if (questionId < 0) {
            throw new IllegalArgumentException("id no pot ser negatiu"); 
        }
        this.questionId = questionId; 

    }

    /**
     * Retorna l'identificador de la pregunta.
     * @return L'ID de la pregunta.
     */    
    public int getQuestionId() { return questionId; }


    /**
     * Enumeració que representa els diferents tipus de resposta suportats.
     */
    public enum Type {
        /** Resposta basada en text lliure. */
        TEXT,
        /** Resposta numèrica de tipus enter. */
        INT,
        /** Selecció de múltiples opcions. */
        MULTIPLE_CHOICE,
        /** Selecció d'una única opció. */
        SINGLE_CHOICE
    }

    
    /**
     * Retorna el tipus concret de la resposta.
     * @return El tipus de resposta (TEXT, INT, etc.).
     */    
    public abstract Type getType();

    /**
     * Indica si la resposta està buida (p. ex., un string buit o sense opcions seleccionades).
     * @return Cert si la resposta no conté dades significatives, fals altrament.
     */
    public abstract boolean isEmpty();
    


    /**
     * Mètode de fàbrica per crear una resposta de text lliure (TextAnswer).
     * @param questionId ID de la pregunta.
     * @param value El valor de text lliure.
     * @return Una nova instància de TextAnswer.
     * @throws NullArgumentException si el valor és null.
     */    
    public static TextAnswer TEXT(int questionId, String value) throws NullArgumentException {
        return new TextAnswer(questionId, value);
    }


    /**
     * Mètode de fàbrica per crear una resposta numèrica (IntAnswer).
     * @param questionId ID de la pregunta.
     * @param value El valor enter respost.
     * @return Una nova instància de IntAnswer.
     */    
    public static IntAnswer INT(int questionId, int value) {
        return new IntAnswer(questionId, value);
    }

    /**
     * Mètode de fàbrica per crear una resposta de selecció múltiple a partir d'un string CSV.
     * @param questionId ID de la pregunta.
     * @param optionIdsCsv String amb els IDs de les opcions seleccionades separats per comes.
     * @return Una nova instància de MultipleChoiceAnswer.
     * @throws InvalidArgumentException si algun ID d'opció és negatiu o no és un enter.
     * @throws NullArgumentException si el string CSV és null.
     */
    public static MultipleChoiceAnswer MULTIPLE_CHOICE(int questionId, String optionIdsCsv) throws InvalidArgumentException, NullArgumentException {
        return new MultipleChoiceAnswer(questionId, optionIdsCsv);
    }

    /**
     * Mètode de fàbrica per crear una resposta de selecció múltiple a partir d'una col·lecció d'IDs.
     * @param questionId ID de la pregunta.
     * @param optionIds Col·lecció d'enters que representen els IDs de les opcions seleccionades.
     * @return Una nova instància de MultipleChoiceAnswer.
     * @throws InvalidArgumentException si algun ID d'opció és negatiu.
     * @throws NullArgumentException si la col·lecció d'IDs és null.
     */
    public static MultipleChoiceAnswer MULTIPLE_CHOICE(int questionId, Collection<Integer> optionIds) throws InvalidArgumentException, NullArgumentException {
        return new MultipleChoiceAnswer(questionId, new ArrayList<>(optionIds));
    }


    /**
     * Mètode de fàbrica per crear una resposta de selecció única (SingleChoiceAnswer).
     * @param questionId ID de la pregunta.
     * @param optionId L'ID de l'única opció seleccionada.
     * @return Una nova instància de SingleChoiceAnswer.
     * @throws InvalidArgumentException si l'ID de l'opció és negatiu.
     */
    public static SingleChoiceAnswer SINGLE_CHOICE(int questionId, int optionId) throws InvalidArgumentException {
        return new SingleChoiceAnswer(questionId, optionId); 
    }
   





}



