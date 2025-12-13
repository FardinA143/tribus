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

	private final Path surveysDir;
	private final SurveySerializer serializer;

	public SurveyPersistence() {
		this(Path.of("..", "DATA", "surveys"), new TxtSurveySerializer());
	}

	public SurveyPersistence(Path surveysDir, SurveySerializer serializer) {
		if (surveysDir == null || serializer == null) {
			throw new IllegalArgumentException("surveysDir and serializer cannot be null");
		}
		this.surveysDir = surveysDir;
		this.serializer = serializer;
	}

	/** Desa una enquesta en un fitxer (id.txt) sota la carpeta configurada. */
	public synchronized void save(Survey survey) throws NullArgumentException, PersistenceException {
		if (survey == null) {
			throw new NullArgumentException("survey");
		}
		ensureDir();
		Path target = surveysDir.resolve(survey.getId() + ".txt");
		try {
			serializer.toFile(survey, target.toString());
		} catch (Exception e) {
			throw new PersistenceException("survey " + survey.getId(), e.getMessage());
		}
	}

	/** Carrega una enquesta pel seu identificador. */
	public synchronized Survey load(String surveyId) throws NullArgumentException, PersistenceException {
		if (surveyId == null) {
			throw new NullArgumentException("surveyId");
		}
		Path target = surveysDir.resolve(surveyId + ".txt");
		if (!Files.exists(target)) {
			throw new PersistenceException("survey " + surveyId, "Fitxer no trobat");
		}
		try {
			return serializer.fromFile(target.toString());
		} catch (IOException e) {
			throw new PersistenceException("survey " + surveyId, e.getMessage());
		}
	}

	/** Llista totes les enquestes disponibles carregant cada fitxer .txt. */
	public synchronized List<Survey> loadAll() throws PersistenceException {
		ensureDir();
		List<Survey> surveys = new ArrayList<>();
		try {
			Files.list(surveysDir)
				.filter(p -> p.getFileName().toString().endsWith(".txt"))
				.forEach(p -> {
					try {
						surveys.add(serializer.fromFile(p.toString()));
					} catch (IOException ignored) {
						// Skip malformed files; could be logged if needed
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
		Path target = surveysDir.resolve(surveyId + ".txt");
		try {
			return Files.deleteIfExists(target);
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
