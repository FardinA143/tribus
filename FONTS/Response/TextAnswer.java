package Response;

import Exceptions.NullArgumentException;



/**
 * Representa una resposta a una pregunta de tipus text lliure.
 */
public final class TextAnswer extends Answer {
    /** El valor de text lliure respost. */
    private final String value;

    /**
     * Constructor per a TextAnswer.
     * @param questionId ID de la pregunta.
     * @param value El valor de text lliure.
     * @throws NullArgumentException si el valor de text és null.
     */
    public TextAnswer(int questionId, String value) throws NullArgumentException {
        super(questionId);
        //value no pot ser null 
        if (value == null) {
            throw new NullArgumentException("Text answer cannot be null");
        }
        this.value = value;
    }

    /**
     * Retorna el valor de text respost.
     * @return El valor de text.
     */

    public String getValue() {
        return value;
    }

    /**
     * Retorna el tipus de resposta TEXT.
     * @return Type.TEXT.
     */
    @Override
    public Type getType() {
        return Type.TEXT;
    }

    /**
     * Indica si la resposta és buida (string buit o només espais en blanc).
     * @return Cert si el valor és buit o conté només espais en blanc.
     */

    @Override
    public boolean isEmpty() {
        return value.trim().isEmpty();
    }
}