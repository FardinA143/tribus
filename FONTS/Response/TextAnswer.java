// java
package Response;

import FONTS.Exceptions.NullArgumentException;

public final class TextAnswer extends Answer {
    private final String value;

    public TextAnswer(int questionId, String value) {
        super(questionId);
        if (value == null) {
            throw new NullArgumentException("Text answer cannot be null");
        }
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    @Override
    public boolean isEmpty() {
        return value.trim().isEmpty();
    }
}