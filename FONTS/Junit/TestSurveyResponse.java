import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.SurveyResponse;
import Response.IntAnswer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSurveyResponse {

    // Cas convencional: Constructor amb dades completes
    @Test
    //test principal: constructor valid
    public void testConstructorValid() {
        list<answer> answers = Array.asList(new IntAnswer(1, 20)); 

        SurveyResponse sr = new SurveyResponse("R1", "S1", "U1", "2025-11-14", answers);
        assertEquals("R1", sr.getId());
        assertEquals("S1", sr.getSurveyId());
        assertEquals("U1", sr.getUserId());
        assertEquals("2025-11-14", sr.getSubmittedAt());
        assertEquals(1, sr.getAnswers().size());
    }



    // Cas límit: Constructor sense respostes 
    @Test
    public void testConstructorMinimal() {
        SurveyResponse sr = new SurveyResponse("R2", "S2", "U2");
        assertNotNull(sr.getAnswers());
        assertTrue(sr.getAnswers().isEmpty());
    }



    // Cas extrem/error: ID de resposta nul
    @Test(expected = NullrgumentException.class)
    public void testConstructorNullId() {
        new SurveyResponse(null, "S3", "U3");
    }



    // Cas extrem/error: Survey ID buit
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptySurveyId() {
        new SurveyResponse("R4", "", "U4");
    }



    // Cas convencional: Afegir i eliminar respostes
    @Test
    public void testAddAndRemoveAnswer() {
        SurveyResponse sr = new SurveyResponse("R5", "S5", "U5");
        Answer a1 = Answer.INT(1, 20);
        
        sr.addAnswer(a1);
        assertEquals(1, sr.getAnswers().size());
        
        sr.removeAnswer(a1);
        assertEquals(0, sr.getAnswers().size());
    }



    // Cas extrem: Prova d'Equals (basat només en l'ID)
    @Test
    public void testEqualsAndHashCode() {
        SurveyResponse sr1 = new SurveyResponse("R6", "S6", "U6");
        // Mateix ID, dades d'enquesta/usuari diferents
        SurveyResponse sr2 = new SurveyResponse("R6", "S99", "U99"); 
        
        assertTrue(sr1.equals(sr2));
        assertEquals(sr1.hashCode(), sr2.hashCode());

        SurveyResponse sr3 = new SurveyResponse("R7", "S6", "U6"); 
        assertFalse(sr1.equals(sr3));
    }
}




