package Junit;

import Encoder.OneHotEncoder;
import Exceptions.*;
import Response.*;
import Survey.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Proves unitàries per a OneHotEncoder.
 * Aquestes proves utilitzen objectes reals (Stubs) de Survey, Question i Answer
 * com a 'fixtures' (dades de prova) per verificar la lògica de codificació
 * dels mètodes fit() i transform().
 */
public class TestOneHotEncoder {

    private OneHotEncoder encoder;
    private Survey testSurvey;
    private List<SurveyResponse> testResponses;

    // Fixtures (Stubs)
    private SingleChoiceQuestion q1_sc;
    private OpenIntQuestion q2_oi;
    private MultipleChoiceQuestion q3_mc;

    /**
     * Prepara les dades de prova (fixtures) abans de cada test.
     * Crea una enquesta (stub) amb 3 preguntes (SingleChoice, OpenInt, MultipleChoice)
     * i dues respostes (stubs) per a l'enquesta.
     */
    @Before
    public void setUp() throws InvalidSurveyException, InvalidQuestionException, NullArgumentException, InvalidArgumentException {
        encoder = new OneHotEncoder();
        
        // 1. Crear Enquesta (Stub)
        String now = LocalDateTime.now().toString();
        testSurvey = new Survey("s1", "Test Survey", "", "user1", 3, "k++", "euclid", now, now);

        // 2. Crear Preguntes (Stubs)
        q1_sc = new SingleChoiceQuestion(10, "Color?", true, 1, 1.0);
        q1_sc.addOption(new ChoiceOption(101, "Red"));
        q1_sc.addOption(new ChoiceOption(102, "Green"));
        
        q2_oi = new OpenIntQuestion(20, "Age?", true, 2, 1.0, 0, 100);

        q3_mc = new MultipleChoiceQuestion(30, "Food?", false, 3, 1.0, 1, 3);
        q3_mc.addOption(new ChoiceOption(301, "Pizza"));
        q3_mc.addOption(new ChoiceOption(302, "Pasta"));
        q3_mc.addOption(new ChoiceOption(303, "Sushi"));

        testSurvey.addQuestion(q1_sc);
        testSurvey.addQuestion(q2_oi);
        testSurvey.addQuestion(q3_mc);

        // 3. Crear Respostes (Stubs)
        testResponses = new ArrayList<>();
        
        // Resposta 1: Green (102), Age 20, Pizza i Sushi (301, 303)
        List<Answer> answers1 = List.of(
            Answer.SINGLE_CHOICE(10, 102),
            Answer.INT(20, 20),
            Answer.MULTIPLE_CHOICE(30, List.of(301, 303))
        );
        testResponses.add(new SurveyResponse("r1", "s1", "u1", now, answers1));

        // Resposta 2: Red (101), Age 50, Pasta (302)
        List<Answer> answers2 = List.of(
            Answer.SINGLE_CHOICE(10, 101),
            Answer.INT(20, 50),
            Answer.MULTIPLE_CHOICE(30, List.of(302))
        );
        testResponses.add(new SurveyResponse("r2", "s1", "u2", now, answers2));
    }

    /**
     * Comprova que el mètode 'fit' genera els noms de 'features' correctes
     * i en l'ordre esperat segons la posició de les preguntes.
     */
    @Test
    public void testFitCreatesCorrectFeatureNames() {
        encoder.fit(testSurvey, testResponses);
        
        List<String> expectedNames = List.of(
            "q10_opt101", // q1 (SC) - Opció Red
            "q10_opt102", // q1 (SC) - Opció Green
            "q20_val",    // q2 (Int) - Age
            "q30_opt301", // q3 (MC) - Opció Pizza
            "q30_opt302", // q3 (MC) - Opció Pasta
            "q30_opt303"  // q3 (MC) - Opció Sushi
        );
        
        assertEquals(expectedNames, encoder.getFeatureNames());
        assertEquals(6, encoder.getFeatureNames().size());
    }

    /**
     * Comprova que el mètode 'transform' codifica correctament les respostes
     * en una matriu numèrica (double[][]) d'acord amb el 'fit' previ.
     * Verifica la normalització [0,1] per a OpenInt.
     */
    @Test
    public void testTransformEncodesCorrectly() {
        encoder.fit(testSurvey, testResponses);
        double[][] X = encoder.transform(testResponses);

        // Vector esperat per a Resposta 1: [Green (102), 20 anys, Pizza (301) i Sushi (303)]
        // q1_101=0, q1_102=1, 
        // q2_val=(20-20)/(50-20)=0.0 (Min=20, Max=50 del fit)
        // q3_301=1, q3_302=0, q3_303=1
        double[] expectedRow1 = {0.0, 1.0, 0.0, 1.0, 0.0, 1.0};
        
        // Vector esperat per a Resposta 2: [Red (101), 50 anys, Pasta (302)]
        // q1_101=1, q1_102=0,
        // q2_val=(50-20)/(50-20)=1.0
        // q3_301=0, q3_302=1, q3_303=0
        double[] expectedRow2 = {1.0, 0.0, 1.0, 0.0, 1.0, 0.0};

        assertArrayEquals(expectedRow1, X[0], 1e-9);
        assertArrayEquals(expectedRow2, X[1], 1e-9);
    }

    /**
     * Comprova la codificació d'una resposta amb valors buits (preguntes no contestades).
     * Els valors buits o 'null' s'han de codificar com 0.0.
     */
    @Test
    public void testTransformWithEmptyAnswers() throws NullArgumentException, InvalidArgumentException {
        // Resposta 3: Sense resposta a Q1, Edat 35, Sense resposta a Q3
        List<Answer> answers3 = List.of(Answer.INT(20, 35));
        testResponses.add(new SurveyResponse("r3", "s1", "u3", null, answers3));
        
        encoder.fit(testSurvey, testResponses); // Re-fit amb la nova resposta
        double[][] X = encoder.transform(testResponses);

        // Vector esperat per a Resposta 3: [Edat 35]
        // q1_101=0, q1_102=0, (buit)
        // q2_val=(35-20)/(50-20)=15/30=0.5
        // q3_301=0, q3_302=0, q3_303=0 (buit)
        double[] expectedRow3 = {0.0, 0.0, 0.5, 0.0, 0.0, 0.0};
        
        assertArrayEquals(expectedRow3, X[2], 1e-9);
    }

    /**
     * Comprova que 'transform' llença IllegalStateException si no s'ha cridat 'fit' prèviament.
     */
    @Test(expected = IllegalStateException.class)
    public void testTransformThrowsExceptionIfNotFitted() {
        // No cridem a encoder.fit()
        encoder.transform(testResponses); // Hauria de llençar IllegalStateException
    }
}