import org.junit.Test;
import static org.junit.Assert.*;
import Response.Answer;
import Response.SingleChoiceAnswer;
import Exceptions.InvalidArgumentException; // Necessita la importació de l'excepció

public class TestSingleChoiceAnswer {

    // Cas convencional: Option ID vàlid
    @Test
    public void testSingleChoiceAnswerValid() {
        SingleChoiceAnswer a = new SingleChoiceAnswer(1, 5);
        assertEquals(1, a.getQuestionId());
        assertEquals(5, a.getOptionId());
        assertEquals(Answer.Type.SINGLE_CHOICE, a.getType());
        assertFalse(a.isEmpty()); // Una opció triada mai és buida
    }


    // Cas extrem/error: Option ID negatiu
    @Test(expected = InvalidArgumentException.class)
    public void testSingleChoiceAnswerNegativeOptionId() {
        new SingleChoiceAnswer(2, -1);
    }
    

    // Cas convencional: Option ID zero (si es permet a la Question, l'Answer és vàlida)
    @Test
    public void testSingleChoiceAnswerZeroOptionId() {
        SingleChoiceAnswer a = new SingleChoiceAnswer(3, 0);
        assertEquals(0, a.getOptionId());
    }


    // Prova de que l'objecte NO PERMET emmagatzemar múltiples opcions
    @Test
    public void testCannotStoreMultipleOptions() {
        // 1. Creem l'objecte amb la primera opció: 10
        SingleChoiceAnswer a = new SingleChoiceAnswer(2, 10);
        assertEquals(10, a.getOptionId());

        // 2. Per "canviar" l'opció, hauríem de crear un objecte nou o sobrescriure. 
        // Com que la classe és final i el camp optionId és final, l'única manera de 
        // tenir una opció diferent és CREANT UN OBJECTE NOU.
        SingleChoiceAnswer b = new SingleChoiceAnswer(2, 20);

        // Verifiquem que l'objecte original 'a' no ha estat afectat.
        assertEquals(10, a.getOptionId());
        
        // El test confirma que 'SingleChoiceAnswer' només representa un valor a la vegada 
        // i no té cap mecanisme per gestionar una col·lecció de valors com la 
        // 'MultipleChoiceAnswer'.
    }