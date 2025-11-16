package Junit;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import Survey.*;
import org.junit.jupiter.api.*;

/**
 * Proves unitàries per a la classe TxtSurveySerializer.
 * Verifica la correcta serialització i deserialització d'una enquesta (Survey)
 * amb diferents tipus de preguntes.
 */
public class TxtSurveySerializerTest {

    private TxtSurveySerializer serializer;
    private Path temp;

    @BeforeEach
    void setup() throws Exception {
        serializer = new TxtSurveySerializer();
        temp = Files.createTempFile("survey_test",".txt");
    }

    @AfterEach
    void cleanup() throws Exception {
        Files.deleteIfExists(temp);
    }

    /**
     * Comprova la serialització completa a fitxer i la posterior deserialització,
     * verificant que els atributs de l'enquesta i els tipus de preguntes es mantenen.
     */
    @Test
    void testToFileAndFromFile_fullSurvey() throws Exception {
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
    @Test
    void testEmptyFileThrows() throws Exception {
        Files.writeString(temp, "");
        // El fromFile re-llança la IOException com a NotValidFileException si no troba línia
        assertThrows(IOException.class, () -> serializer.fromFile(temp.toString()));
    }

    /**
     * Comprova que si la capçalera (la línia de Survey) és mal formada (menys de 9 camps),
     * es llança una excepció.
     */
    @Test
    void testMalformedHeaderThrows() throws Exception {
        Files.writeString(temp, "bad,header\n");
        assertThrows(IOException.class, () -> serializer.fromFile(temp.toString()));
    }

    /**
     * Comprova que les línies de preguntes mal formades (que provoquen excepcions internes)
     * són ignorades, i només les línies vàlides són carregades.
     */
    @Test
    void testMalformedQuestionLineIgnored() throws Exception {
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