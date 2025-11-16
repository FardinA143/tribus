import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.TextAnswer;
import Exceptions.NullArgumentException; 

/**
 * Proves unitàries per a la classe TextAnswer.
 * Verifica la gestió del valor de text, incloent-hi els casos buits, d'espais
 * en blanc i nuls.
 */
public class TestTextAnswer {


    /**
     * Cas convencional: Resposta vàlida amb text no buit.
     * Comprova el valor, el tipus i que no és buida.
     */
    @Test
    public void testTextAnswerValid() {
        TextAnswer a = new TextAnswer(1, "resposta vàlida");
        assertEquals("resposta vàlida", a.getValue());
        assertEquals(Answer.Type.TEXT, a.getType());
        assertFalse(a.isEmpty());
    }

    /**
     * Cas d'error: El valor de text és nul.
     * Ha de llançar NullArgumentException.
     * @throws NullArgumentException Si el valor és nul.
     */
    @Test(expected = NullArgumentException.class)
    public void testTextAnswerNull() throws NullArgumentException {
        new TextAnswer(5, null); 
    }

    /**
     * Cas extrem: Resposta buida (String buit "").
     * Ha de ser considerada buida (isEmpty = true).
     */
    @Test
    public void testTextAnswerEmptyString() {
        TextAnswer a = new TextAnswer(3, "");
        assertTrue(a.isEmpty());
    }

    /**
     * Cas extrem: Resposta amb només espais en blanc ("   \t").
     * Ha de ser considerada buida (isEmpty = true, ja que trim() és buit).
     */
    @Test
    public void testTextAnswerWhitespace() {
        TextAnswer a = new TextAnswer(4, "   \t");
        assertTrue(a.isEmpty());
    }


    /**
     * Comprova el mètode de fàbrica estàtic Answer.TEXT.
     * @throws NullArgumentException Si el valor és nul.
     */
    @Test
    public void testStaticFactoryMethod() throws NullArgumentException {
        TextAnswer a = Answer.TEXT(6, "Factory Test");
        assertNotNull(a);
        assertEquals("Factory Test", a.getValue());
    }
}