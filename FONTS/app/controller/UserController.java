package app.controller;

import user.AuthService;
import user.Sesion;
import user.User;

public class UserController {
    private final AuthService authService;
    private Sesion currentSession;

    public UserController() {
        this(new AuthService());
    }

    public UserController(AuthService authService) {
        this.authService = authService;
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
}
