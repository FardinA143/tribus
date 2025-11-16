import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.MultipleChoiceAnswer;
import Exceptions.InvalidArgumentException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TestMultipleChoiceAnswer {

    // Cas convencional: Constructor amb Collection (llista d'enters)
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


    // Cas extrem: Llista buida (ha de ser considerada buida)
    @Test
    public void testMultipleChoiceAnswerEmptyList() throws InvalidArgumentException {
        List<Integer> options = new ArrayList<>();
        MultipleChoiceAnswer a = Answer.MULTIPLE_CHOICE(2, options);
        
        //no options selected
        assertTrue(a.isEmpty());
        assertTrue(a.getOptionIds().isEmpty());
        assertEquals("", a.optionIdsCsv());
    }


    //comprovacio per valor al csv no integer 
    @Test(expected = InvalidArgumentException.class) 
    public void testMultipleChoiceAnswerInvalidCsv() throws InvalidArgumentException {
    // Si s'inclou un string que no és un enter ("ABC"), NumberFormatException és llençada.
    Answer.MULTIPLE_CHOICE(6, "1, ABC, 5"); 
}


    // Cas d'error: Identificador negatiu a la llista d'opcions 
    //resvisar(es pot escollir opcions negatives?)
    @Test
    public void testMultipleChoiceAnswerNegativeIdInList() throws InvalidArgumentException {
        Answer.MULTIPLE_CHOICE(3, Arrays.asList(1, -5, 10)); 
    }

