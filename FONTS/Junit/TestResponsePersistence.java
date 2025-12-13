package Junit;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.Answer;
import Response.IntAnswer;
import Response.MultipleChoiceAnswer;
import Response.SingleChoiceAnswer;
import Response.SurveyResponse;
import Response.TextAnswer;
import importexport.TxtResponseSerializer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import persistence.ResponsePersistance;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Proves d'integració per a la persistència de respostes d'enquesta.
 */
public class TestResponsePersistence {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void AppendLoadDeleteResponses() throws Exception {
        Path dir = tmp.newFolder("responses").toPath();
        ResponsePersistance persistence = new ResponsePersistance(dir, new TxtResponseSerializer());

        SurveyResponse r1 = buildResponse("r1", "s1", "u1", 10, "hola", 2, new int[]{1, 2});
        SurveyResponse r2 = buildResponse("r2", "s1", "u2", 5, "adeu", 1, new int[]{2});

        persistence.append("s1", r1);
        persistence.append("s1", r2);

        List<SurveyResponse> loaded = persistence.loadAll("s1");
        assertEquals(2, loaded.size());

        boolean deleted = persistence.delete("s1");
        assertTrue(deleted);
        assertFalse(Files.exists(dir.resolve("s1.txt")));
    }

    private SurveyResponse buildResponse(String id, String surveyId, String userId,
                                         int intValue, String text, int singleOpt, int[] multiOpts)
            throws NullArgumentException, InvalidArgumentException {
        List<Answer> answers = new ArrayList<>();
        answers.add(new IntAnswer(1, intValue));
        answers.add(new TextAnswer(2, text));
        answers.add(new SingleChoiceAnswer(3, singleOpt));
        List<Integer> multi = new ArrayList<>();
        for (int opt : multiOpts) {
            multi.add(opt);
        }
        answers.add(new MultipleChoiceAnswer(4, multi));
        return new SurveyResponse(id, surveyId, userId, "2024-01-01T00:00:00", answers);
    }
}
