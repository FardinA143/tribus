import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.SurveyResponse;
import Response.IntAnswer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Proves unitàries per a la classe SurveyResponse.
 * Verifica la correcta construcció, l'afegit/eliminació de respostes
 * i el mètode d'igualtat (equals/hashCode).
 */
public class TestSurveyResponse {

    /**
     * Cas convencional: Constructor amb totes les dades vàlides.
     */
    @Test
    public void testConstructorValid() {
        List<Answer> answers = Arrays.asList(Answer.INT(1, 20));
        SurveyResponse sr = new SurveyResponse("R1", "S1", "U1", "2025-11-14", answers);
        assertEquals("R1", sr.getId());
        assertEquals("S1", sr.getSurveyId());
        assertEquals("U1", sr.getUserId());
        assertEquals("2025-11-14", sr.getSubmittedAt());
        assertEquals(1, sr.getAnswers().size());
    }


    /**
     * Cas límit: Constructor minimalista (sense data ni llista de respostes inicial).
     */
    @Test
    public void testConstructorMinimal() {
        SurveyResponse sr = new SurveyResponse("R2", "S2", "U2");
        assertNotNull(sr.getAnswers());
        assertTrue(sr.getAnswers().isEmpty());
    }


    /**
     * Cas extrem/error: ID de resposta nul.
     * Espera IllegalArgumentException (Nota: el test especifica NullArgumentException).
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorNullId() {
        new SurveyResponse(null, "S3", "U3", null, null);
    }


    /**
     * Cas extrem/error: Survey ID buit.
     * Espera IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorEmptySurveyId() {
        new SurveyResponse("R4", "", "U4");
    }


    /**
     * Cas convencional: Afegir i eliminar respostes.
     */
    @Test
    public void testAddAndRemoveAnswer() {
        SurveyResponse sr = new SurveyResponse("R5", "S5", "U5");
        Answer a1 = Answer.INT(1, 20);
        
        // Afegir la resposta
        sr.addAnswer(a1);
        assertEquals(1, sr.getAnswers().size());
        
        // Eliminar la resposta
        sr.removeAnswer(a1);
        assertEquals(0, sr.getAnswers().size());
    }


    /**
     * Cas extrem: Prova d'igualtat i HashCode.
     * La igualtat es basa únicament en l'ID, ignorant SurveyId i UserId.
     */
    @Test
    public void testEqualsAndHashCode() {
        SurveyResponse sr1 = new SurveyResponse("R6", "S6", "U6");
        // Mateix ID ("R6"), dades d'enquesta/usuari diferents.
        SurveyResponse sr2 = new SurveyResponse("R6", "S99", "U99"); 
        
        assertTrue(sr1.equals(sr2));
        assertEquals(sr1.hashCode(), sr2.hashCode());

        SurveyResponse sr3 = new SurveyResponse("R7", "S6", "U6"); 
        assertFalse(sr1.equals(sr3));
    }
}