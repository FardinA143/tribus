import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.TextAnswer;
import Exceptions.NullArgumentException; // Necessita la importació de l'excepció

public class TestTextAnswer {


    // Cas convencional: Resposta vàlida
    @Test
    public void testTextAnswerValid() {
        TextAnswer a = new TextAnswer(1, "resposta vàlida");
        assertEquals("resposta vàlida", a.getValue());
        assertEquals(Answer.Type.TEXT, a.getType());
        assertFalse(a.isEmpty());
    }

    @Test(expected = NullArgumentException.class)
    public void testTextAnswerNull() throws NullArgumentException {
        // La crida al constructor ha de llançar NullArgumentException
        new TextAnswer(5, null); 
    }

    // Cas extrem: Resposta buida (String buit)
    @Test
    public void testTextAnswerEmptyString() {
        TextAnswer a = new TextAnswer(3, "");
        assertTrue(a.isEmpty());
    }

    // Cas extrem: Resposta amb només espais en blanc (ha de ser considerada buida)
    @Test
    public void testTextAnswerWhitespace() {
        TextAnswer a = new TextAnswer(4, "   \t");
        assertTrue(a.isEmpty());
    }


    //mira és si l'objecte ha estat creat i inicialitzat correctament.
    @Test
    public void testStaticFactoryMethod() throws NullArgumentException {
        TextAnswer a = Answer.TEXT(6, "Factory Test");
        assertNotNull(a);
        assertEquals("Factory Test", a.getValue());
    }

    


    


}