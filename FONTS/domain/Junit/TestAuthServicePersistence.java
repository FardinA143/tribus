package Junit;

import Exceptions.PersistenceException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import user.AuthService;
import user.RegisteredUser;
import persistence.UserPersistence;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * Tests d'integració per comprovar la persistència a userdata.json després
 * d'operacions de registre, actualització i eliminació.
 */
public class TestAuthServicePersistence {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void RegisterUpdateDeleteShouldReflectOnDisk() throws IOException, PersistenceException {
        Path dataFile = tmp.newFile("userdata.json").toPath();
        UserPersistence persistence = new UserPersistence(dataFile);
        AuthService authService = new AuthService(persistence);

        // Registre
        RegisteredUser user = authService.register("u1", "User 1", "user1", "pass1");
        assertNotNull(user);
        String content = Files.readString(dataFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("\"id\":\"u1\""));
        assertTrue(content.contains("\"username\":\"user1\""));

        // Actualització
        authService.updateUser("u1", "User 1 Updated", "user1_new", "pass2");
        content = Files.readString(dataFile, StandardCharsets.UTF_8);
        assertFalse(content.contains("\"username\":\"user1\""));
        assertTrue(content.contains("\"username\":\"user1_new\""));
        assertTrue(content.contains("\"displayName\":\"User 1 Updated\""));

        // Eliminació
        boolean deleted = authService.deleteUser("u1");
        assertTrue(deleted);
        content = Files.readString(dataFile, StandardCharsets.UTF_8);
        String normalized = content.replaceAll("\\s", "");
        assertEquals("[]", normalized);
    }
}
