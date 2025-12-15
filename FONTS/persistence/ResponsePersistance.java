package persistence;

import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.SurveyResponse;
import importexport.ResponseSerializer;
import importexport.TxtResponseSerializer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gestiona la persistència de respostes d'enquesta per fitxer, agrupades per surveyId.
 */
public class ResponsePersistance {

	private final Path responsesDir;
	private final ResponseSerializer serializer;

	public ResponsePersistance() {
		this(Path.of("..", "DATA", "responses"), new TxtResponseSerializer());
	}

	public ResponsePersistance(Path responsesDir, ResponseSerializer serializer) {
		if (responsesDir == null || serializer == null) {
			throw new IllegalArgumentException("responsesDir and serializer cannot be null");
		}
		this.responsesDir = responsesDir;
		this.serializer = serializer;
	}

	/** Desa totes les respostes d'una enquesta en un fitxer (surveyId.txt). */
	public synchronized void saveAll(String surveyId, List<SurveyResponse> responses)
			throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		if (responses == null) {
			throw new NullArgumentException("responses");
		}
		ensureDir();
		Path target = responsesDir.resolve(surveyId + ".txt");
		try {
			serializer.toFile(responses, target.toString());
		} catch (Exception e) {
			throw new PersistenceException("responses " + surveyId, e.getMessage());
		}
	}

	/** Afegeix una resposta a les ja persistides per a l'enquesta. */
	public synchronized void append(String surveyId, SurveyResponse response)
			throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		if (response == null) {
			throw new NullArgumentException("response");
		}
		List<SurveyResponse> current = loadAll(surveyId);
		current.add(response);
		saveAll(surveyId, current);
	}

	/** Carrega totes les respostes d'una enquesta. Si no existeix el fitxer, retorna llista buida. */
	public synchronized List<SurveyResponse> loadAll(String surveyId)
			throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		ensureDir();
		Path target = responsesDir.resolve(surveyId + ".txt");
		if (!Files.exists(target)) {
			return new ArrayList<>();
		}
		try {
			return serializer.fromFile(target.toString());
		} catch (IOException e) {
			throw new PersistenceException("responses " + surveyId, e.getMessage());
		}
	}

	/** Elimina el fitxer de respostes associat a l'enquesta. */
	public synchronized boolean delete(String surveyId) throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		Path target = responsesDir.resolve(surveyId + ".txt");
		try {
			return Files.deleteIfExists(target);
		} catch (IOException e) {
			throw new PersistenceException("responses " + surveyId, e.getMessage());
		}
	}

	private void ensureDir() throws PersistenceException {
		try {
			Files.createDirectories(responsesDir);
		} catch (IOException e) {
			throw new PersistenceException("responses dir", e.getMessage());
		}
	}
}
