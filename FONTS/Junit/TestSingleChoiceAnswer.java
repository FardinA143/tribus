import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.SingleChoiceAnswer;
import Exceptions.InvalidArgumentException;

/**
 * Proves unitàries per a la classe SingleChoiceAnswer.
 * Verifica la creació d'objectes i la validació de l'ID d'opció.
 */
public class TestSingleChoiceAnswer {

    /**
     * Cas convencional: Option ID vàlid (positiu).
     * Comprova l'ID, el valor, el tipus i que no és buida.
     */
    @Test
    public void testSingleChoiceAnswerValid() {
        SingleChoiceAnswer a = new SingleChoiceAnswer(1, 5);
        assertEquals(1, a.getQuestionId());
        assertEquals(5, a.getOptionId());
        assertEquals(Answer.Type.SINGLE_CHOICE, a.getType());
        assertFalse(a.isEmpty()); // Una opció triada mai és buida
    }


    /**
     * Cas extrem/error: Option ID negatiu.
     * Ha de llançar InvalidArgumentException.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testSingleChoiceAnswerNegativeOptionId() {
        new SingleChoiceAnswer(2, -1);
    }
    

    /**
     * Cas convencional: Option ID zero.
     * Si l'ID zero és permès per la Question, l'Answer és vàlida.
     */
    @Test
    public void testSingleChoiceAnswerZeroOptionId() {
        SingleChoiceAnswer a = new SingleChoiceAnswer(3, 0);
        assertEquals(0, a.getOptionId());
    }


    /**
     * Prova conceptual: Comprova que l'objecte és immutable i només representa
     * una opció a la vegada, d'acord amb el seu propòsit (Single Choice).
     */
    @Test
    public void testCannotStoreMultipleOptions() {
        // Com que la classe és final i el camp optionId és final, l'única manera de 
        // tenir una opció diferent és creant un objecte nou.
        SingleChoiceAnswer a = new SingleChoiceAnswer(2, 10);
        assertEquals(10, a.getOptionId());

        SingleChoiceAnswer b = new SingleChoiceAnswer(2, 20);

        // Verifiquem que l'objecte original 'a' no ha estat afectat.
        assertEquals(10, a.getOptionId());
    }
}