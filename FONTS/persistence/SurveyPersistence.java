package persistence;

import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Survey.Survey;
import importexport.SurveySerializer;
import importexport.TxtSurveySerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SurveyPersistence {

	private static final String EXT = ".tbs";

	private final Path surveysDir;
	private final SurveySerializer serializer;

	public SurveyPersistence() {
		this(resolveDefaultSurveysDir(), new TxtSurveySerializer());
	}

	private static Path resolveDefaultSurveysDir() {
		// When running from `FONTS` (CLI/make), ../DATA points to repo DATA/.
		// When running from `FONTS/presentation` (Electron), we need ../../DATA.
		Path p1 = Path.of("..", "DATA", "surveys");
		try {
			if (Files.exists(p1) || Files.exists(p1.getParent())) {
				return p1;
			}
		} catch (Exception ignored) {
			// fall through
		}
		return Path.of("..", "..", "DATA", "surveys");
	}

	public SurveyPersistence(Path surveysDir, SurveySerializer serializer) {
		if (surveysDir == null || serializer == null) {
			throw new IllegalArgumentException("surveysDir and serializer cannot be null");
		}
		this.surveysDir = surveysDir;
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

	/** Desa una enquesta en un fitxer (id.tbs) sota la carpeta configurada. */
	public synchronized void save(Survey survey) throws NullArgumentException, PersistenceException {
		if (survey == null) {
			throw new NullArgumentException("survey");
		}
		ensureDir();
		String normalizedId = stripExtIfPresent(survey.getId());
		if (normalizedId == null || normalizedId.isBlank()) {
			throw new PersistenceException("survey", "ID invàlid");
		}
		Path target = surveysDir.resolve(normalizedId + EXT);
		try {
			serializer.toFile(survey, target.toString());
		} catch (Exception e) {
			throw new PersistenceException("survey " + normalizedId, e.getMessage());
		}
	}

	/** Carrega una enquesta pel seu identificador. */
	public synchronized Survey load(String surveyId) throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		ensureDir();
		String normalizedId = stripExtIfPresent(surveyId);
		if (normalizedId == null || normalizedId.isBlank()) {
			throw new PersistenceException("survey", "ID invàlid");
		}
		Path target = surveysDir.resolve(normalizedId + EXT);
		if (!Files.exists(target)) {
			throw new PersistenceException("survey " + normalizedId, "Fitxer no trobat");
		}
		try {
			return serializer.fromFile(target.toString());
		} catch (IOException e) {
			throw new PersistenceException("survey " + normalizedId, e.getMessage());
		}
	}

	/** Llista totes les enquestes disponibles carregant cada fitxer .tbs. */
	public synchronized List<Survey> loadAll() throws PersistenceException {
		ensureDir();
		List<Survey> surveys = new ArrayList<>();
		try {
			Files.list(surveysDir)
				.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(EXT))
				.forEach(p -> {
					try {
						surveys.add(serializer.fromFile(p.toString()));
					} catch (Exception ignored) {
						// Skip malformed files
					}
				});
			return surveys;
		} catch (IOException e) {
			throw new PersistenceException("surveys", e.getMessage());
		}
	}

	/** Elimina el fitxer associat a una enquesta. */
	public synchronized boolean delete(String surveyId) throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		ensureDir();
		String normalizedId = stripExtIfPresent(surveyId);
		try {
			return Files.deleteIfExists(surveysDir.resolve(normalizedId + EXT));
		} catch (IOException e) {
			throw new PersistenceException("survey " + surveyId, e.getMessage());
		}
	}

	private void ensureDir() throws PersistenceException {
		try {
			Files.createDirectories(surveysDir);
		} catch (IOException e) {
			throw new PersistenceException("surveys dir", e.getMessage());
		}
	}
}
