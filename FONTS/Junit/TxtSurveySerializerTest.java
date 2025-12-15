package Junit;

import Survey.*;
import importexport.TxtSurveySerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe TxtSurveySerializer.
 * Verifica la correcta serialització i deserialització d'una enquesta (Survey)
 * amb diferents tipus de preguntes.
 */
public class TxtSurveySerializerTest {

    private TxtSurveySerializer serializer;
    private Path temp;

    @Before
    public void setup() throws Exception {
        serializer = new TxtSurveySerializer();
        temp = Files.createTempFile("survey_test",".txt");
    }

    @After
    public void cleanup() throws Exception {
        Files.deleteIfExists(temp);
    }

    /**
     * Comprova la serialització completa a fitxer i la posterior deserialització,
     * verificant que els atributs de l'enquesta i els tipus de preguntes es mantenen.
     */
    @Test
    public void testToFileAndFromFileFullSurvey() throws Exception {
        List<Question> qs = new ArrayList<>();
        qs.add(new OpenIntQuestion(1, "Edad?", true, 1, 1.0, 0, 100));
        qs.add(new OpenStringQuestion(2, "Nombre?", false, 2, 0.5, 80));
        qs.add(new SingleChoiceQuestion(3, "Color?", true, 3, 1.0));
        qs.add(new MultipleChoiceQuestion(4, "Hobbies?", false, 4, 2.0, 1, 3));

        Survey s = new Survey("sid","title","desc","yo",3,"init","dist","2025","2025");
        s.importQuestions(qs);

        serializer.toFile(s, temp.toString());
        Survey loaded = serializer.fromFile(temp.toString());

        assertEquals(s.getId(), loaded.getId());
        assertEquals(4, loaded.getQuestions().size());

        // Verificació dels tipus de preguntes
        assertTrue(loaded.getQuestions().get(0) instanceof OpenIntQuestion);
        assertTrue(loaded.getQuestions().get(1) instanceof OpenStringQuestion);
        assertTrue(loaded.getQuestions().get(2) instanceof SingleChoiceQuestion);
        assertTrue(loaded.getQuestions().get(3) instanceof MultipleChoiceQuestion);
    }

    /**
     * Comprova que si l'arxiu està completament buit, es llança una excepció.
     */
    @Test(expected = IOException.class)
    public void testEmptyFileThrows() throws Exception {
        Files.writeString(temp, "");
        serializer.fromFile(temp.toString());
    }

    /**
     * Comprova que si la capçalera (la línia de Survey) és mal formada (menys de 9 camps),
     * es llança una excepció.
     */
    @Test(expected = IOException.class)
    public void testMalformedHeaderThrows() throws Exception {
        Files.writeString(temp, "bad,header\n");
        serializer.fromFile(temp.toString());
    }

    /**
     * Comprova que les línies de preguntes mal formades (que provoquen excepcions internes)
     * són ignorades, i només les línies vàlides són carregades.
     */
    @Test
    public void testMalformedQuestionLineIgnored() throws Exception {
        String content = "sid,title,desc,yo,3,init,dist,2025,2025\n" +
                         "1,Texto,true,1,1.0,oi,0,100\n" + // VÀLIDA
                         "mal,formed,line\n" + // MAL FORMADA (s'ignora o provoca excepció)
                         "2,Texto2,false,2,0.5,sc\n"; // VÀLIDA
        Files.writeString(temp, content);

        Survey s = serializer.fromFile(temp.toString());
        // El test espera que només les dues preguntes vàlides siguin carregades.
        assertEquals(2, s.getQuestions().size());
    }
}