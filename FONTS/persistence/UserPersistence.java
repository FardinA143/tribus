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
		this(Path.of("..", "DATA", "userdata.json"));
	}

	public UserPersistence(Path userDataPath) {
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

	private String escape(String value) {
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
