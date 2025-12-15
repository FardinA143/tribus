package Response; 

/**
 * Representa una resposta a una pregunta de tipus numèric enter.
 * No té validacions de rang, s'ocupa d'això la classe Question.
 */

public final class IntAnswer extends Answer {
    /** El valor enter respost. */
    private final int value;


    /**
     * Constructor per a IntAnswer.
     * @param questionId ID de la pregunta.
     * @param value El valor enter respost.
     */
    public IntAnswer(int questionId, int value) { 
        super(questionId); 
        this.value = value; 
    }


    /**
     * Retorna el valor enter respost.
     * @return El valor enter.
     */
    public int getValue() { 
        return value; 
    }


    /**
     * Retorna el tipus de resposta INT.
     * @return Type.INT.
     */
    @Override public Type getType() { 
        return Type.INT; 
    }

    /**
     * Una resposta de tipus enter sempre conté un valor (fins i tot 0 o negatiu), per tant no es considera mai buida.
     * @return Sempre fals.
     */
    @Override public boolean isEmpty() { 
        return false; 
    }

    /**
     * Retorna la representació en String de l'objecte.
     * @return String amb l'ID de la pregunta i el valor.
     */
    @Override public String toString() {
         return "IntAnswer{q=" + getQuestionId() + ", value=" + value + "}"; 
    }
}
