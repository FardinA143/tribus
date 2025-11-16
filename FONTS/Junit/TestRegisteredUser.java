package Junit;

import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe RegisteredUser.
 * Verifica la correcta creació, la recuperació d'atributs i la modificació
 * del nom d'usuari i la contrasenya.
 */
public class TestRegisteredUser {

    private RegisteredUser registeredUser;

    @Before
    public void setUp() {
        registeredUser = new RegisteredUser("r456", "UsuarioReg", 
                                            "2024-01-01T10:00:00", 
                                            "user_login", "hash_pass");
    }

    /**
     * Comprova que el constructor inicialitza correctament tots els camps,
     * incloent-hi els heretats de User.
     */
    @Test
    public void testRegisteredUserConstructorAndGetters() {
        // Verifica tots els getters, incloent-hi els heretats
        assertEquals("r456", registeredUser.getId());
        assertEquals("UsuarioReg", registeredUser.getDisplayName());
        assertEquals("user_login", registeredUser.getUsername());
        assertEquals("hash_pass", registeredUser.getPasswordHash());
    }

    /**
     * Comprova que el nom d'usuari (username) es pot canviar correctament.
     */
    @Test
    public void testChangeUsername() {
        registeredUser.changeUsername("nuevo_login");
        assertEquals("nuevo_login", registeredUser.getUsername());
    }

    /**
     * Comprova que el hash de la contrasenya es pot canviar correctament.
     */
    @Test
    public void testChangePassword() {
        registeredUser.changePassword("nuevo_hash");
        assertEquals("nuevo_hash", registeredUser.getPasswordHash());
    }

    /**
     * Comprova que el nom visible (displayName) es pot canviar correctament
     * a través del mètode heretat.
     */
    @Test
    public void testChangeDisplayNameInherited() {
        // Prueba el setter heredado
        registeredUser.changeDisplayName("Nombre Cambiado");
        assertEquals("Nombre Cambiado", registeredUser.getDisplayName());
    }
}