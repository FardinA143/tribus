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

	private static final String EXT = ".tbs";

	private final Path responsesDir;
	private final ResponseSerializer serializer;

	public ResponsePersistance() {
		this(resolveDefaultResponsesDir(), new TxtResponseSerializer());
	}

	private static Path resolveDefaultResponsesDir() {
		// When running from `FONTS` (CLI/make), ../DATA points to repo DATA/.
		// When running from `FONTS/presentation` (Electron), we need ../../DATA.
		Path p1 = Path.of("..", "DATA", "responses");
		try {
			if (Files.exists(p1) || Files.exists(p1.getParent())) {
				return p1;
			}
		} catch (Exception ignored) {
			// fall through
		}
		return Path.of("..", "..", "DATA", "responses");
	}

	public ResponsePersistance(Path responsesDir, ResponseSerializer serializer) {
		if (responsesDir == null || serializer == null) {
			throw new IllegalArgumentException("responsesDir and serializer cannot be null");
		}
		this.responsesDir = responsesDir;
		this.serializer = serializer;
	}

	private static String stripExtIfPresent(String id) {
		if (id == null) return null;
		String out = id.trim();
		if (out.endsWith(EXT)) {
			out = out.substring(0, out.length() - EXT.length());
		}
		return out;
	}

	/** Desa totes les respostes d'una enquesta en un fitxer (surveyId.tbs). */
	public synchronized void saveAll(String surveyId, List<SurveyResponse> responses)
			throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		if (responses == null) {
			throw new NullArgumentException("responses");
		}
		ensureDir();
		String normalizedId = stripExtIfPresent(surveyId);
		if (normalizedId == null || normalizedId.isBlank()) {
			throw new PersistenceException("responses", "surveyId invàlid");
		}
		Path target = responsesDir.resolve(normalizedId + EXT);
		try {
			serializer.toFile(responses, target.toString());
		} catch (Exception e) {
			throw new PersistenceException("responses " + normalizedId, e.getMessage());
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
		String normalizedId = stripExtIfPresent(surveyId);
		Path target = responsesDir.resolve(normalizedId + EXT);
		if (!Files.exists(target)) {
			return new ArrayList<>();
		}
		try {
			return serializer.fromFile(target.toString());
		} catch (IOException e) {
			throw new PersistenceException("responses " + normalizedId, e.getMessage());
		}
	}

	/** Elimina el fitxer de respostes associat a l'enquesta. */
	public synchronized boolean delete(String surveyId) throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		try {
			String normalizedId = stripExtIfPresent(surveyId);
			boolean deleted = Files.deleteIfExists(responsesDir.resolve(normalizedId + EXT));
			return deleted;
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
