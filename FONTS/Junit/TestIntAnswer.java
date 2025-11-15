import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.IntAnswer;

public class TestIntAnswer {

    // Cas convencional: Valor positiu
    //el Ansert.Type es INT, obligatoriament a tenir un valor integer-> sortiria error de compilacio
    @Test
    public void testIntAnswerPositive() {
        //primer valor indica el id de la pregunta, el segon el valor indica el valor del integer
        IntAnswer a = Answer.INT(1, 42);
        assertEquals(1, a.getQuestionId());
        assertEquals(42, a.getValue());
        assertEquals(Answer.Type.INT, a.getType());
        assertFalse(a.isEmpty());
    }


    // Cas convencional: Valor zero
    @Test
    public void testIntAnswerZero() {
        IntAnswer a = Answer.INT(2, 0);
        assertEquals(0, a.getValue());
    }

    // Cas convencional: Valor negatiu (si bé pot ser invalidat per la Question, 
    // la classe Answer en sí mateixa ha de permetre guardar el valor)
    @Test
    public void testIntAnswerNegative() {
        IntAnswer a = Answer.INT(3, -100);
        assertEquals(-100, a.getValue());
    }


    //cas limit: string


