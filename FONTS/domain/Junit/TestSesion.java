package Junit;

import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe Sesion, que gestiona l'estat d'una sessió d'usuari.
 */
public class TestSesion {

    private Sesion sesion;
    private User testUser;

    @Before
    public void setUp() {
        testUser = new RegisteredUser("u1", "Test User", "2024-01-01", "testuser", "hash");
        sesion = new Sesion(testUser);
    }

    /**
     * Comprova que la sessió es crea correctament amb l'usuari, un ID,
     * l'estat actiu i el temps d'accés inicial.
     */
    @Test
    public void testSessionCreation() {
        assertEquals("El usuario de la sesión no es el correcto", testUser, sesion.getUser());
        assertTrue("La sesión debe iniciar activa", sesion.isActive());
        assertNotNull("El ID de sesión no debe ser nulo", sesion.getSessionId());
        assertNotNull("El tiempo de acceso no debe ser nulo", sesion.getLastAccessTime());
    }

    /**
     * Comprova que la sessió es tanca (es desactiva) correctament.
     */
    @Test
    public void testSessionClose() {
        assertTrue(sesion.isActive());  
        sesion.close();
        assertFalse("La sesión debe estar inactiva después de close()", sesion.isActive());
    }

    /**
     * Comprova que el temps de l'últim accés s'actualitza correctament
     * i és diferent al temps inicial.
     */
    @Test
    public void testUpdateLastAccessTime() throws InterruptedException {
        String time1 = sesion.getLastAccessTime();

        // Esperem 10ms per garantir que LocalDateTime.now() 
        // capture un valor de temps diferent.
        Thread.sleep(10); 

        sesion.updateLastAccessTime();
        String time2 = sesion.getLastAccessTime();

        // Verifiquem que el temps realment es va actualitzar
        assertNotEquals("El tiempo de acceso no se actualizó", time1, time2);
    }
}