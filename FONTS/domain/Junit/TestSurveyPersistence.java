package Junit;

import Exceptions.InvalidQuestionException;
import Exceptions.InvalidSurveyException;
import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Survey.ChoiceOption;
import Survey.SingleChoiceQuestion;
import Survey.Survey;
import importexport.TxtSurveySerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import persistence.SurveyPersistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Proves d'integració per a la persistència d'enquestes en fitxer.
 */
public class TestSurveyPersistence {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void SaveLoadDeleteSurvey() throws Exception {
        Path dir = tmp.newFolder("surveys").toPath();
        SurveyPersistence persistence = new SurveyPersistence(dir, new TxtSurveySerializer());

        Survey survey = buildSampleSurvey();
        persistence.save(survey);

        Survey loaded = persistence.load("s1");
        assertNotNull(loaded);
        assertEquals("s1", loaded.getId());
        assertEquals("Survey Title", loaded.getTitle());
        assertEquals(1, loaded.getQuestions().size());

        List<Survey> all = persistence.loadAll();
        assertEquals(1, all.size());

        boolean deleted = persistence.delete("s1");
        assertTrue(deleted);
        assertFalse(Files.exists(dir.resolve("s1.txt")));
    }

    private Survey buildSampleSurvey() throws InvalidQuestionException, NullArgumentException, InvalidSurveyException {
        Survey survey = new Survey(
                "s1",
                "Survey Title",
                "Desc",
                "author",
                3,
                "kmeans",
                "euclidean",
                "2024-01-01T00:00:00",
                "2024-01-01T00:00:00"
        );
        SingleChoiceQuestion q = new SingleChoiceQuestion(1, "Pregunta?", true, 1, 1.0);
        q.addOption(new ChoiceOption(1, "Opt A"));
        q.addOption(new ChoiceOption(2, "Opt B"));
        survey.addQuestion(q);
        return survey;
    }
}
