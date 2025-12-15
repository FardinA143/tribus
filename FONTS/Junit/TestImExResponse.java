package Junit;

import importexport.TxtResponseSerializer;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestImExResponse {

	@Test
	public void serializerCanBeCreated() {
		TxtResponseSerializer serializer = new TxtResponseSerializer();
		assertNotNull(serializer);
	}
}
