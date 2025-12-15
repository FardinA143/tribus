package Junit;

import importexport.TxtSurveySerializer;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestImExSurvey {

	@Test
	public void serializerCanBeCreated() {
		TxtSurveySerializer serializer = new TxtSurveySerializer();
		assertNotNull(serializer);
	}
}
