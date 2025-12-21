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
			Files.list(responsesDir)
				.filter(Files::isRegularFile)
				.forEach(p -> {
					try {
						String name = p.getFileName().toString();
						String base = normalizeId(name);
						if (base == null || base.isBlank()) return;
						Path target = responsesDir.resolve(base + EXT);
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

	private List<SurveyResponse> normalizeResponsesSurveyId(String normalizedSurveyId, List<SurveyResponse> responses) {
		if (responses == null) return new ArrayList<>();
		List<SurveyResponse> out = new ArrayList<>(responses.size());
		for (SurveyResponse r : responses) {
			if (r == null) continue;
			String rid = r.getId();
			String uid = r.getUserId();
			String submittedAt = r.getSubmittedAt();
			List<Response.Answer> answers = r.getAnswers();
			String sid = normalizeId(r.getSurveyId());
			String effectiveSid = (sid == null || sid.isBlank()) ? normalizedSurveyId : sid;
			if (!effectiveSid.equals(r.getSurveyId())) {
				try {
					out.add(new SurveyResponse(rid, effectiveSid, uid, submittedAt, answers));
				} catch (Exception ignored) {
					// If rebuilding fails, keep original.
					out.add(r);
				}
			} else {
				out.add(r);
			}
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
		migrateFilenamesBestEffort();
		String normalizedId = normalizeId(surveyId);
		if (normalizedId == null || normalizedId.isBlank()) {
			throw new PersistenceException("responses", "surveyId invàlid");
		}
		Path target = responsesDir.resolve(normalizedId + EXT);
		try {
			List<SurveyResponse> normalizedResponses = normalizeResponsesSurveyId(normalizedId, responses);
			serializer.toFile(normalizedResponses, target.toString());
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
		migrateFilenamesBestEffort();
		String normalizedId = normalizeId(surveyId);
		Path target = responsesDir.resolve(normalizedId + EXT);
		if (!Files.exists(target)) {
			return new ArrayList<>();
		}
		try {
			List<SurveyResponse> res = serializer.fromFile(target.toString());
			List<SurveyResponse> normalized = normalizeResponsesSurveyId(normalizedId, res);
			// Persistim la correcció si hem hagut de reconstruir.
			if (normalized.size() == res.size()) {
				boolean changed = false;
				for (int i = 0; i < res.size(); i++) {
					if (!res.get(i).getSurveyId().equals(normalized.get(i).getSurveyId())) {
						changed = true;
						break;
					}
				}
				if (changed) {
					try {
						serializer.toFile(normalized, target.toString());
					} catch (Exception ignored) {
						// best-effort only
					}
				}
			}
			return normalized;
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
			migrateFilenamesBestEffort();
			String normalizedId = normalizeId(surveyId);
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
