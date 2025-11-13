package Junit;

import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RegisteredUserTest {

    private RegisteredUser registeredUser;

    @Before
    public void setUp() {
        registeredUser = new RegisteredUser("r456", "UsuarioReg", 
                                            "2024-01-01T10:00:00", 
                                            "user_login", "hash_pass");
    }

    @Test
    public void testRegisteredUserConstructorAndGetters() {
        // Verifica todos los getters, incluyendo los heredados
        assertEquals("r456", registeredUser.getId());
        assertEquals("UsuarioReg", registeredUser.getDisplayName());
        assertEquals("user_login", registeredUser.getUsername());
        assertEquals("hash_pass", registeredUser.getPasswordHash());
    }

    @Test
    public void testChangeUsername() {
        registeredUser.changeUsername("nuevo_login");
        assertEquals("nuevo_login", registeredUser.getUsername());
    }

    @Test
    public void testChangePassword() {
        registeredUser.changePassword("nuevo_hash");
        assertEquals("nuevo_hash", registeredUser.getPasswordHash());
    }

    @Test
    public void testChangeDisplayNameInherited() {
        // Prueba el setter heredado
        registeredUser.changeDisplayName("Nombre Cambiado");
        assertEquals("Nombre Cambiado", registeredUser.getDisplayName());
    }
} 