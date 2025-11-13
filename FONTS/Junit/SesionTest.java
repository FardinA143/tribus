package Junit;

import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SesionTest {

    private Sesion sesion;
    private User testUser;

    @Before
    public void setUp() {
        testUser = new RegisteredUser("u1", "Test User", "2024-01-01", "testuser", "hash");
        sesion = new Sesion(testUser);
    }

    @Test
    public void testSessionCreation() {
        assertEquals("El usuario de la sesión no es el correcto", testUser, sesion.getUser());
        assertTrue("La sesión debe iniciar activa", sesion.isActive());
        assertNotNull("El ID de sesión no debe ser nulo", sesion.getSessionId());
        assertNotNull("El tiempo de acceso no debe ser nulo", sesion.getLastAccessTime());
    }

    @Test
    public void testSessionClose() {
        assertTrue(sesion.isActive());  
        sesion.close();
        assertFalse("La sesión debe estar inactiva después de close()", sesion.isActive());
    }

    @Test
    public void testUpdateLastAccessTime() throws InterruptedException {
        String time1 = sesion.getLastAccessTime();

        // Esperamos 10ms para garantizar que LocalDateTime.now() 
        // capture un valor de tiempo diferente.
        Thread.sleep(10); 

        sesion.updateLastAccessTime();
        String time2 = sesion.getLastAccessTime();

        // Verificamos que el tiempo realmente se actualizó
        assertNotEquals("El tiempo de acceso no se actualizó", time1, time2);
    }
}