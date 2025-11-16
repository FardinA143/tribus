import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.IntAnswer;

/**
 * Proves unitàries per a la classe IntAnswer.
 * Verifica la correcta creació d'objectes i la recuperació de valors.
 */
public class TestIntAnswer {

    /**
     * Cas convencional: Valor enter positiu.
     * Comprova l'ID, el valor, el tipus i que no és buida.
     */
    @Test
    public void testIntAnswerPositive() {
        // primer valor indica el id de la pregunta, el segon el valor indica el valor de l'enter
        IntAnswer a = Answer.INT(1, 42);
        assertEquals(1, a.getQuestionId());
        assertEquals(42, a.getValue());
        assertEquals(Answer.Type.INT, a.getType());
        assertFalse(a.isEmpty());
    }


    /**
     * Cas convencional: Valor enter zero.
     */
    @Test
    public void testIntAnswerZero() {
        IntAnswer a = Answer.INT(2, 0);
        assertEquals(0, a.getValue());
    }

    /**
     * Cas convencional: Valor enter negatiu.
     * L'objecte Answer ha de permetre guardar el valor, tot i que la classe Question
     * pugui invalidar-lo en un context de validació.
     */
    @Test
    public void testIntAnswerNegative() {
        IntAnswer a = Answer.INT(3, -100);
        assertEquals(-100, a.getValue());
    }
}