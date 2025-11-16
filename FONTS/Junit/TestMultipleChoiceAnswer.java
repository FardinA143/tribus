import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.MultipleChoiceAnswer;
import Exceptions.InvalidArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Proves unitàries per a la classe MultipleChoiceAnswer.
 * Verifica la creació a partir de llistes i CSV, així com la validació d'arguments.
 */
public class TestMultipleChoiceAnswer {

    /**
     * Cas convencional: Constructor amb Collection (llista d'enters) vàlida.
     * Comprova l'ID, la mida de la llista i la representació CSV.
     */
    @Test
    public void testMultipleChoiceAnswerFromCollectionValid() throws InvalidArgumentException {
        List<Integer> options = Arrays.asList(1, 5, 10);
        MultipleChoiceAnswer a = Answer.MULTIPLE_CHOICE(1, options);
        
        assertEquals(1, a.getQuestionId());
        assertEquals(3, a.getOptionIds().size());
        assertTrue(a.getOptionIds().contains(5));
        assertFalse(a.isEmpty());
        assertEquals("1,5,10", a.optionIdsCsv());
    }


    /**
     * Cas extrem: Llista buida (ha de ser considerada una resposta buida).
     */
    @Test
    public void testMultipleChoiceAnswerEmptyList() throws InvalidArgumentException {
        List<Integer> options = new ArrayList<>();
        MultipleChoiceAnswer a = Answer.MULTIPLE_CHOICE(2, options);
        
        //no options selected
        assertTrue(a.isEmpty());
        assertTrue(a.getOptionIds().isEmpty());
        assertEquals("", a.optionIdsCsv());
    }


    /**
     * Cas d'error: Comprovació d'un valor no enter en el CSV.
     * Hauria de llançar NumberFormatException, però és capturat i re-llançat
     * com a InvalidArgumentException pel mètode constructor.
     */
    @Test(expected = InvalidArgumentException.class) 
    public void testMultipleChoiceAnswerInvalidCsv() throws InvalidArgumentException {
        // Si s'inclou un string que no és un enter ("ABC"), NumberFormatException és llençada internament.
        Answer.MULTIPLE_CHOICE(6, "1, ABC, 5"); 
    }


    /**
     * Cas d'error: Identificador negatiu a la llista d'opcions.
     * El constructor ha de llançar InvalidArgumentException si hi ha un ID negatiu.
     * (Nota: El test hauria d'estar marcat amb @Test(expected = InvalidArgumentException.class))
     */
    @Test(expected = InvalidArgumentException.class)
    public void testMultipleChoiceAnswerNegativeIdInList() throws InvalidArgumentException {
        Answer.MULTIPLE_CHOICE(3, Arrays.asList(1, -5, 10)); 
    }
}