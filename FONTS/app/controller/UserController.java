package app.controller;

import user.AuthService;
import user.RegisteredUser;
import user.Sesion;
import user.User;

import java.util.Collection;

public class UserController {
    private final AuthService authService;
    private Sesion currentSession;

    public UserController() {
        this(new AuthService());
    }

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Crea una sessió efímera per a un usuari convidat (guest).
     * Retorna la sessió creada i l'estableix com a sessió actual.
     */
    public Sesion createGuestSession() {
        String id = "guest-" + Long.toHexString(System.nanoTime());
        String display = "Convidat-" + id.substring(Math.max(0, id.length() - 6));
        user.GuestUser guest = new user.GuestUser(id, display);
        currentSession = new Sesion(guest);
        return currentSession;
    }

    public User register(String id, String displayName, String username, String password) {
        return authService.register(id, displayName, username, password);
    }

    public Sesion login(String username, String password) {
        currentSession = authService.login(username, password);
        return currentSession;
    }

    public void logout() {
        if (currentSession != null) {
            authService.logout(currentSession);
            currentSession = null;
        }
    }

    public boolean hasActiveSession() {
        return currentSession != null && currentSession.isActive();
    }

    public Sesion getCurrentSession() {
        return currentSession;
    }

    public User requireActiveUser() {
        if (!hasActiveSession()) {
            throw new IllegalStateException("No hay ninguna sesión activa");
        }
        refreshSession();
        return currentSession.getUser();
    }

    public void refreshSession() {
        if (hasActiveSession()) {
            currentSession.updateLastAccessTime();
        }
    }

    public Collection<RegisteredUser> listRegisteredUsers() {
        return authService.listRegisteredUsers();
    }
}
