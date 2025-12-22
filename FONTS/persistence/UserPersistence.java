package persistence;

import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Iterator;
import user.RegisteredUser;

public class UserPersistence {

	private final Path userDataPath;

	public UserPersistence() {
		this(resolveDefaultUserDataPath());
	}

	private static Path resolveDefaultUserDataPath() {
		// When running from `FONTS` (CLI/make), ../DATA points to repo DATA/. no haria falta, siempre electron
		// When running from `FONTS/presentation` (Electron), we need ../../DATA.
		Path p1 = Path.of("..", "DATA", "userdata.json");
		try {
			Path parent1 = p1.getParent();
			if (Files.exists(p1) || (parent1 != null && Files.exists(parent1))) {
				return p1;
			}
		} catch (Exception ignored) {
			// fall through
		}
		return Path.of("..", "..", "DATA", "userdata.json");
	}

	public UserPersistence(Path userDataPath) {   // verificacion simple, eliminable
		if (userDataPath == null) {
			throw new IllegalArgumentException("userDataPath cannot be null");
		}
		this.userDataPath = userDataPath;
	}

	public synchronized void persistUser(RegisteredUser user) throws NullArgumentException, PersistenceException {
		if (user == null) {
			throw new NullArgumentException("registeredUser");
		}

		try {
			Path parent = userDataPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			String userJson = serialize(user);
			String newContent;

			if (!Files.exists(userDataPath) || Files.size(userDataPath) == 0) {
				newContent = wrapAsArray(userJson);
			} else {
				String current = Files.readString(userDataPath, StandardCharsets.UTF_8);
				newContent = appendUser(current, userJson);
			}

			Files.writeString(
					userDataPath,
					newContent,
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException io) {
			throw new PersistenceException("userdata.json", io.getMessage());
		}
	}

	/**
	 * Sobrescriu el fitxer amb tots els usuaris registrats actuals.
	 */
	public synchronized void persistAllUsers(Collection<RegisteredUser> users)
			throws NullArgumentException, PersistenceException {
		if (users == null) {
			throw new NullArgumentException("users");
		}

		try {
			Path parent = userDataPath.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			StringBuilder sb = new StringBuilder();
			sb.append("[\n");
			Iterator<RegisteredUser> it = users.iterator();
			while (it.hasNext()) {
				sb.append(serialize(it.next()));
				if (it.hasNext()) {
					sb.append(",\n");
				}
			}
			sb.append("\n]\n");

			    Files.writeString(
				    userDataPath,
					sb.toString(),
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING
			);
		} catch (IOException io) {
			throw new PersistenceException("userdata.json", io.getMessage());
		}
	}

	/**
	 * Carrega tots els usuaris registrats des del fitxer JSON.
	 * Si el fitxer no existeix, retorna una llista buida.
	 */
	public synchronized java.util.List<RegisteredUser> loadAllUsers() throws PersistenceException {
		try {
			if (!Files.exists(userDataPath)) {
				return new java.util.ArrayList<>();
			}
			String content = Files.readString(userDataPath, StandardCharsets.UTF_8);
			if (content == null || content.trim().isEmpty()) {
				return new java.util.ArrayList<>();
			}
			String trimmed = content.trim();
			if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
				throw new PersistenceException("userdata.json", "Formato JSON no válido");
			}

			java.util.List<String> objects = extractJsonObjects(trimmed);
			java.util.LinkedHashMap<String, RegisteredUser> byId = new java.util.LinkedHashMap<>();
			String now = java.time.LocalDateTime.now().toString();
			for (String obj : objects) {
				String id = readStringField(obj, "id");
				if (id == null || id.isBlank()) continue;
				String displayName = readStringField(obj, "displayName");
				String username = readStringField(obj, "username");
				String passwordHash = readStringField(obj, "passwordHash");
				RegisteredUser user = new RegisteredUser(
						id,
						displayName == null ? "" : displayName,
						now,
						username == null ? "" : username,
						passwordHash == null ? "" : passwordHash
				);
				// If duplicates exist, last one wins.
				byId.put(id, user);
			}
			return new java.util.ArrayList<>(byId.values());
		} catch (IOException io) {
			throw new PersistenceException("userdata.json", io.getMessage());
		}
	}

	private java.util.List<String> extractJsonObjects(String jsonArray) {
		java.util.List<String> objs = new java.util.ArrayList<>();
		int start = -1;
		int depth = 0;
		boolean inString = false;
		boolean escape = false;
		for (int i = 0; i < jsonArray.length(); i++) {
			char c = jsonArray.charAt(i);
			if (escape) { escape = false; continue; }
			if (inString) {
				if (c == '\\') { escape = true; continue; }
				if (c == '"') { inString = false; }
				continue;
			}
			if (c == '"') { inString = true; continue; }
			if (c == '{') {
				if (depth == 0) start = i;
				depth++;
			} else if (c == '}') {
				depth--;
				if (depth == 0 && start != -1) {
					objs.add(jsonArray.substring(start, i + 1));
					start = -1;
				}
			}
		}
		return objs;
	}

	private String readStringField(String objJson, String key) {
		if (objJson == null || key == null) return null;
		String needle = "\"" + key + "\"";
		int k = objJson.indexOf(needle);
		if (k < 0) return null;
		int colon = objJson.indexOf(':', k + needle.length());
		if (colon < 0) return null;
		int i = colon + 1;
		while (i < objJson.length() && Character.isWhitespace(objJson.charAt(i))) i++;
		if (i >= objJson.length() || objJson.charAt(i) != '"') return null;
		i++;
		StringBuilder sb = new StringBuilder();
		boolean esc = false;
		for (; i < objJson.length(); i++) {
			char c = objJson.charAt(i);
			if (esc) {
				switch (c) {
					case '"' -> sb.append('"');
					case '\\' -> sb.append('\\');
					case '/' -> sb.append('/');
					case 'b' -> sb.append('\b');
					case 'f' -> sb.append('\f');
					case 'n' -> sb.append('\n');
					case 'r' -> sb.append('\r');
					case 't' -> sb.append('\t');
					case 'u' -> {
						if (i + 4 < objJson.length()) {
							String hex = objJson.substring(i + 1, i + 5);
							try { sb.append((char) Integer.parseInt(hex, 16)); } catch (Exception ignored) {}
							i += 4;
						}
					}
					default -> sb.append(c);
				}
				esc = false;
				continue;
			}
			if (c == '\\') { esc = true; continue; }
			if (c == '"') break;
			sb.append(c);
		}
		return sb.toString();
	}

	private String wrapAsArray(String userJson) {
		return "[\n" + userJson + "\n]\n";
	}

	private String appendUser(String currentContent, String userJson) throws PersistenceException {
		String trimmed = currentContent == null ? "" : currentContent.trim();
		if (trimmed.isEmpty() || "[]".equals(trimmed)) {
			return wrapAsArray(userJson);
		}
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			throw new PersistenceException("userdata.json", "Formato JSON no válido");
		}

		String body = trimmed.substring(1, trimmed.length() - 1).trim();
		StringBuilder sb = new StringBuilder();
		sb.append("[\n");
		if (!body.isEmpty()) {
			sb.append(body);
			if (!body.trim().endsWith(",")) {
				sb.append(",\n");
			} else {
				sb.append("\n");
			}
		}
		sb.append(userJson).append("\n]");
		return sb.toString();
	}

	private String serialize(RegisteredUser user) {
		StringBuilder sb = new StringBuilder();
		sb.append("  {");
		sb.append("\"id\":\"").append(escape(user.getId())).append("\",");
		sb.append("\"displayName\":\"").append(escape(user.getDisplayName())).append("\",");
		sb.append("\"username\":\"").append(escape(user.getUsername())).append("\",");
		sb.append("\"passwordHash\":\"").append(escape(user.getPasswordHash())).append("\"");
		sb.append("}");
		return sb.toString();
	}

	private String escape(String value) {  // lo pongo para evitar ataques maliciosos.
		if (value == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (char c : value.toCharArray()) {
			switch (c) {
				case '\\':
					sb.append("\\\\");
					break;
				case '"':
					sb.append("\\\"");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}
}
