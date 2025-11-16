package Junit;
import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe AuthService, que gestiona el registre,
 * l'inici de sessió i el tancament de sessió d'usuaris.
 */
public class TestAuthService {

    private AuthService authService;

    @Before
    public void setUp() {
        authService = new AuthService();
    }

    /**
     * Comprova que el registre d'un nou usuari és exitós i que les dades
     * s'emmagatzemen correctament (incloent-hi el hash de contrasenya).
     */
    @Test
    public void RegisterSuccessfulTest() {
        RegisteredUser user = authService.register("u1", "Nuevo Usuario", "new_user", "pass123");
        assertNotNull("El usuario registrado no debería ser nulo", user);
        assertEquals("El username no coincide", "new_user", user.getUsername());
        assertEquals("El ID no coincide", "u1", user.getId());
        assertNotEquals("La contraseña no debe guardarse en texto plano", 
                        "pass123", user.getPasswordHash());
    }

    /**
     * Comprova que el registre falla quan l'usuari intenta utilitzar
     * un nom d'usuari ja existent.
     */
    @Test
    public void RegisterUsernameTakenTest() {
        authService.register("u1", "User 1", "user_taken", "pass1");
        RegisteredUser user2 = authService.register("u2", "User 2", "user_taken", "pass2");
        assertNull("El segundo usuario debería ser nulo por username duplicado", user2);
    }

    /**
     * Comprova que l'inici de sessió és exitós amb credencials correctes.
     */
    @Test
    public void LoginSuccessfulTest() {
        RegisteredUser user = authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("user_login", "pass123");
        assertNotNull("La sesión no debería ser nula", sesion);
        assertTrue("La sesión debería estar activa", sesion.isActive());
        assertNotNull("La sesión debe tener un ID", sesion.getSessionId());
        assertEquals("La sesión no pertenece al usuario correcto", 
                     user.getId(), sesion.getUser().getId());
    }

    /**
     * Comprova que l'inici de sessió falla amb un nom d'usuari inexistent.
     */
    @Test
    public void LoginInvalidUsernameTest() {
        authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("usuario_inexistente", "pass123");
        assertNull("La sesión debe ser nula para un username inválido", sesion);
    }

    /**
     * Comprova que l'inici de sessió falla amb una contrasenya incorrecta.
     */
    @Test
    public void LoginInvalidPasswordTest() {
        authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("user_login", "wrong_pass");
        assertNull("La sesión debe ser nula para una contraseña inválida", sesion);
    }

    /**
     * Comprova que el tancament de sessió (logout) desactiva la sessió correctament.
     */
    @Test
    public void LogoutTest() {
        authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("user_login", "pass123");
        assertNotNull(sesion);
        assertTrue(sesion.isActive());
        authService.logout(sesion);
        assertFalse("La sesión debería estar inactiva después del logout", sesion.isActive());
    }
}