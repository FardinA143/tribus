package importexport;

import static org.junit.jupiter.api.Assertions.*;

import Response.*;
import Survey.*;

import org.junit.jupiter.api.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Proves unitàries per a la classe TxtResponseSerializer.
 * Verifica la correcta serialització i deserialització d'una SurveyResponse
 * amb diferents tipus de respostes.
 */
public class TxtResponseSerializerTest {

    private TxtResponseSerializer serializer;
    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        serializer = new TxtResponseSerializer();
        tempFile = Files.createTempFile("response_test", ".txt");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(tempFile);
    }

    /**
     * Comprova la serialització i deserialització d'una SurveyResponse
     * que conté respostes de tipus OpenInt (IntAnswer), OpenString (TextAnswer),
     * SingleChoice i MultipleChoice.
     */
    @Test
    void testToFileAndFromFile_int_text_sc_mc() throws Exception {
        List<Answer> answers = new ArrayList<>();
        answers.add(new IntAnswer(1, 42));
        answers.add(new TextAnswer(2, "hola"));
        answers.add(new SingleChoiceAnswer(3, 7));
        answers.add(new MultipleChoiceAnswer(4, "1,2,3")); // Llista de IDs 1, 2, 3

        SurveyResponse resp = new SurveyResponse("id1", "sid", "uid", "2025-01-01", answers);
        List<SurveyResponse> list = List.of(resp);

        serializer.toFile(list, tempFile.toString());
        SurveyResponse loaded = serializer.fromFile(tempFile.toString());

        assertEquals("id1", loaded.getId());
        assertEquals("sid", loaded.getSurveyId());
        assertEquals("uid", loaded.getUserId());
        assertEquals("2025-01-01", loaded.getSubmittedAt());
        assertEquals(4, loaded.getAnswers().size());

        assertTrue(loaded.getAnswers().get(0) instanceof IntAnswer);
        assertEquals(42, ((IntAnswer) loaded.getAnswers().get(0)).getValue());

        assertTrue(loaded.getAnswers().get(1) instanceof TextAnswer);
        assertEquals("hola", ((TextAnswer) loaded.getAnswers().get(1)).getValue());

        assertTrue(loaded.getAnswers().get(2) instanceof SingleChoiceAnswer);
        assertEquals(7, ((SingleChoiceAnswer) loaded.getAnswers().get(2)).getOptionId());

        assertTrue(loaded.getAnswers().get(3) instanceof MultipleChoiceAnswer);
        // S'assumeix que el format del test és "1|2|3" però el codi usa ","
        assertEquals("1,2,3", ((MultipleChoiceAnswer) loaded.getAnswers().get(3)).optionIdsCsv());
    }

    /**
     * Comprova que si l'arxiu està buit, es llança una excepció controlada.
     * (Nota: Amb el codi actual, s'espera NotValidFileException o IOException/Exception
     * dins del fromFile, que re-llança NotValidFileException).
     */
    @Test
    void testEmptyFileThrows() throws Exception {
        Files.writeString(tempFile, "");
        assertThrows(IOException.class, () -> serializer.fromFile(tempFile.toString()));
    }

    /**
     * Comprova que si la capçalera de l'arxiu (la línia de SurveyResponse) és mal formada,
     * es llança una excepció controlada.
     */
    @Test
    void testMalformedHeaderThrows() throws Exception {
        Files.writeString(tempFile, "bad,header\n");
        assertThrows(IOException.class, () -> serializer.fromFile(tempFile.toString()));
    }
}