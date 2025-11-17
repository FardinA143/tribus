package Junit;

import importexport.TxtSurveySerializer;
import Survey.*;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

import static org.junit.Assert.*;

public class TestImportSurveyOptions {

    @Test
    public void testImportSurveyWithOptions() throws Exception {
        String yesB = Base64.getEncoder().encodeToString("Yes".getBytes("UTF-8"));
        String noB = Base64.getEncoder().encodeToString("No".getBytes("UTF-8"));
        String scOptions = "1:" + yesB + "|2:" + noB;

        String aB = Base64.getEncoder().encodeToString("A".getBytes("UTF-8"));
        String bB = Base64.getEncoder().encodeToString("B".getBytes("UTF-8"));
        String mcOptions = "10:" + aB + "|20:" + bB;

        StringBuilder sb = new StringBuilder();
        sb.append("s1,Title,Desc,me,3,init,euclid,2025-11-17,2025-11-17\n");
        sb.append("1,Do you agree,true,1,1.0,sc,").append(scOptions).append('\n');
        sb.append("2,Select letters,false,2,1.0,mc,1,2,").append(mcOptions).append('\n');

        Path temp = Files.createTempFile("imex_survey", ".txt");
        Files.writeString(temp, sb.toString());

        TxtSurveySerializer ser = new TxtSurveySerializer();
        Survey s = ser.fromFile(temp.toString());
        List<Question> qs = s.getQuestions();

        assertEquals(2, qs.size());

        assertTrue(qs.get(0) instanceof SingleChoiceQuestion);
        SingleChoiceQuestion scq = (SingleChoiceQuestion) qs.get(0);
        assertEquals(2, scq.getOptions().size());
        assertEquals(1, scq.getOptions().get(0).getId());
        assertEquals("Yes", scq.getOptions().get(0).getLabel());

        assertTrue(qs.get(1) instanceof MultipleChoiceQuestion);
        MultipleChoiceQuestion mcq = (MultipleChoiceQuestion) qs.get(1);
        assertEquals(2, mcq.getOptions().size());
        assertEquals(10, mcq.getOptions().get(0).getId());
        assertEquals("A", mcq.getOptions().get(0).getLabel());

        Files.deleteIfExists(temp);
    }
}
