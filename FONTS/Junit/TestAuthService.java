package Junit;
import user.*;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestAuthService {

    private AuthService authService;

    @Before
    public void setUp() {
        authService = new AuthService();
    }

    @Test
    public void RegisterSuccessfulTest() {
        RegisteredUser user = authService.register("u1", "Nuevo Usuario", "new_user", "pass123");
        assertNotNull("El usuario registrado no debería ser nulo", user);
        assertEquals("El username no coincide", "new_user", user.getUsername());
        assertEquals("El ID no coincide", "u1", user.getId());
        assertNotEquals("La contraseña no debe guardarse en texto plano", 
                        "pass123", user.getPasswordHash());
    }

    @Test
    public void RegisterUsernameTakenTest() {
        authService.register("u1", "User 1", "user_taken", "pass1");
        RegisteredUser user2 = authService.register("u2", "User 2", "user_taken", "pass2");
        assertNull("El segundo usuario debería ser nulo por username duplicado", user2);
    }

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

    @Test
    public void LoginInvalidUsernameTest() {
        authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("usuario_inexistente", "pass123");
        assertNull("La sesión debe ser nula para un username inválido", sesion);
    }

    @Test
    public void LoginInvalidPasswordTest() {
        authService.register("u1", "User 1", "user_login", "pass123");
        Sesion sesion = authService.login("user_login", "wrong_pass");
        assertNull("La sesión debe ser nula para una contraseña inválida", sesion);
    }

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