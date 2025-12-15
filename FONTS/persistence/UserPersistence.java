package persistence;

import user.AuthService.java;
import java.utils.*;

public class UserPersistence {

    private static final String FILE = "users.json";
    private Gson gson = new Gson();

    public void saveAll(Map<String, RegisteredUser> users) throws IOException {
        String json = gson.toJson(users);
        Files.write(Paths.get(FILE), json.getBytes());
    }

    public Map<String, RegisteredUser> loadAll() throws IOException {
        if (!Files.exists(Paths.get(FILE))) return new HashMap<>();
        String json = new String(Files.readAllBytes(Paths.get(FILE)));
        Type type = new TypeToken<Map<String, RegisteredUser>>() {}.getType();
        return gson.fromJson(json, type);
    }
}