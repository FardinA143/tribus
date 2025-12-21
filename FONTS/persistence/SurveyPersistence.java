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

	private static String normalizeId(String id) {
		if (id == null) return null;
		String out = id.trim();
		boolean changed;
		do {
			changed = false;
			if (out.endsWith(EXT)) {
				out = out.substring(0, out.length() - EXT.length());
				changed = true;
			}
			// Best-effort migration from older .txt naming.
			if (out.endsWith(".txt")) {
				out = out.substring(0, out.length() - ".txt".length());
				changed = true;
			}
		} while (changed);
		return out;
	}

	private void migrateFilenamesBestEffort() {
		try {
			ensureDir();
			Files.list(surveysDir)
				.filter(Files::isRegularFile)
				.forEach(p -> {
					try {
						String name = p.getFileName().toString();
						String base = normalizeId(name);
						if (base == null || base.isBlank()) return;
						Path target = surveysDir.resolve(base + EXT);
						if (p.equals(target)) return;
						if (Files.exists(target)) {
							Files.deleteIfExists(p);
							return;
						}
						Files.move(p, target);
					} catch (Exception ignored) {
						// best-effort only
					}
				});
		} catch (Exception ignored) {
			// best-effort only
		}
	}

	/** Desa una enquesta en un fitxer (id.tbs) sota la carpeta configurada. */
	public synchronized void save(Survey survey) throws NullArgumentException, PersistenceException {
		if (survey == null) {
			throw new NullArgumentException("survey");
		}
		ensureDir();
		migrateFilenamesBestEffort();
		String normalizedId = normalizeId(survey.getId());
		if (normalizedId == null || normalizedId.isBlank()) {
			throw new PersistenceException("survey", "ID invàlid");
		}
		if (!normalizedId.equals(survey.getId())) {
			try {
				survey.setId(normalizedId);
			} catch (Exception ignored) {
				// If we can't update the object id, still proceed with normalized filename.
			}
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
		migrateFilenamesBestEffort();
		String normalizedId = normalizeId(surveyId);
		Path target = surveysDir.resolve(normalizedId + EXT);
		if (!Files.exists(target)) {
			throw new PersistenceException("survey " + normalizedId, "Fitxer no trobat");
		}
		try {
			Survey s = serializer.fromFile(target.toString());
			// Fix corrupted IDs inside file (best-effort).
			try {
				String idIn = normalizeId(s.getId());
				if (idIn != null && !idIn.isBlank() && !idIn.equals(s.getId())) {
					s.setId(idIn);
					Path fixed = surveysDir.resolve(idIn + EXT);
					if (!fixed.equals(target)) {
						serializer.toFile(s, fixed.toString());
						Files.deleteIfExists(target);
					}
				}
			} catch (Exception ignored) {
				// best-effort only
			}
			return s;
		} catch (IOException e) {
			throw new PersistenceException("survey " + normalizedId, e.getMessage());
		}
	}

	/** Llista totes les enquestes disponibles carregant cada fitxer .tbs. */
	public synchronized List<Survey> loadAll() throws PersistenceException {
		ensureDir();
		migrateFilenamesBestEffort();
		List<Survey> surveys = new ArrayList<>();
		try {
			Files.list(surveysDir)
				.filter(p -> p.getFileName().toString().endsWith(EXT))
				.forEach(p -> {
				try {
					Survey s = serializer.fromFile(p.toString());
					try {
						String idIn = normalizeId(s.getId());
						if (idIn != null && !idIn.isBlank() && !idIn.equals(s.getId())) {
							s.setId(idIn);
							Path fixed = surveysDir.resolve(idIn + EXT);
							if (!fixed.equals(p)) {
								serializer.toFile(s, fixed.toString());
								Files.deleteIfExists(p);
							}
						}
					} catch (Exception ignored2) {
						// best-effort only
					}
					surveys.add(s);
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
		try {
			migrateFilenamesBestEffort();
			String normalizedId = normalizeId(surveyId);
			boolean deleted = Files.deleteIfExists(surveysDir.resolve(normalizedId + EXT));
			return deleted;
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
