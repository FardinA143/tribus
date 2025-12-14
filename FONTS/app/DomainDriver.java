package app;

import Exceptions.PersistenceException;
import Response.*;
import Survey.*;
import app.controller.*;
import importexport.*;
import user.*;

import java.io.IOException;
import java.util.*;

/**
 * DomainDriver: driver principal de todo el dominio.
 * Orquesta la comunicación con Electron mediante stdin/stdout usando el protocolo pipe-delimited.
 * Protocolo: ACCION|ARG1|ARG2|...
 * Respuestas: JSON por línea.
 */
public class DomainDriver {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalPersistence persistence = new LocalPersistence();
    private final SurveySerializer surveySerializer = new TxtSurveySerializer();
    private final ResponseSerializer responseSerializer = new TxtResponseSerializer();
    
    // Controladores
    private final UserController userController;
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final AnalyticsController analyticsController;

    public DomainDriver() {
        this.userController = new UserController(this);
        this.surveyController = new SurveyController(persistence, surveySerializer);
        this.responseController = new ResponseController(this, persistence);
        this.analyticsController = new AnalyticsController(this);
    }

    public static void main(String[] args) {
        new DomainDriver().start();
    }

    public void start() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            System.err.println("JAVA RECIBIDO: " + line);
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            processCommand(line);
        }
    }

    private void processCommand(String commandLine) {
        String[] parts = commandLine.split("\\|", -1);
        String action = parts[0].trim().toUpperCase(Locale.ROOT);
        try {
            switch (action) {
                case "GET_SURVEYS" -> handleGetSurveys();
                case "GET_SURVEY" -> handleGetSurvey(parts);
                case "CREATE_SURVEY" -> handleCreateSurvey(parts);
                case "DELETE_SURVEY" -> handleDeleteSurvey(parts);
                case "LIST_RESPONSES" -> handleListResponses(parts);
                case "LOGIN" -> handleLogin(parts);
                case "LOGOUT" -> handleLogout();
                case "IMPORT_SURVEY" -> handleImportSurvey(parts);
                case "EXPORT_SURVEY" -> handleExportSurvey(parts);
                case "REGISTER" -> handleRegister(parts);
                case "ANSWER_SURVEY" -> handleAnswerSurvey(parts);
                case "EDIT_RESPONSE" -> handleEditResponse(parts);
                case "DELETE_RESPONSE" -> handleDeleteResponse(parts);
                case "IMPORT_RESPONSES" -> handleImportResponses(parts);
                case "EXPORT_RESPONSES" -> handleExportResponses(parts);
                case "PERFORM_ANALYSIS" -> handlePerformAnalysis(parts);
                default -> emitError("Comando desconocido: " + action);
            }
        } catch (Exception e) {
            emitError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }

    // ==================== SURVEYS ====================

    private void handleGetSurveys() {
        Collection<Survey> surveys = surveyController.listSurveys();
        System.out.println(toJsonSurveys(surveys));
    }

    private void handleGetSurvey(String[] parts) {
        if (parts.length < 2) {
            emitError("GET_SURVEY requiere ID");
            return;
        }
        String id = parts[1];
        try {
            Survey survey = surveyController.loadSurvey(id);
            System.out.println(surveyToJson(survey));
        } catch (PersistenceException e) {
            emitError(e.getMessage());
        }
    }

    private void handleCreateSurvey(String[] parts) {
        if (parts.length < 4) {
            emitError("CREATE_SURVEY requiere title|description|k");
            return;
        }
        try {
            ensureSessionAllowGuest();
            String title = parts[1];
            String description = parts[2];
            int k = Integer.parseInt(parts[3]);
            String surveyId = UUID.randomUUID().toString();
            User owner = userController.requireActiveUser();
            Survey survey = surveyController.createSurvey(surveyId, title, description, owner, k, "kmeans++", "euclidean");
            surveyController.saveSurvey(survey);
            System.out.println("{\"status\":\"ok\",\"id\":\"" + survey.getId() + "\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleDeleteSurvey(String[] parts) {
        if (parts.length < 2) { emitError("DELETE_SURVEY requiere ID"); return; }
        String id = parts[1];
        try {
            if (!ensureRegisteredSession()) { emitError("Requiere sesión registrada"); return; }
            surveyController.deleteSurvey(id);
            System.out.println("{\"status\":\"ok\"}");
        } catch (PersistenceException e) {
            emitError(e.getMessage());
        }
    }

    private void handleListResponses(String[] parts) {
        if (parts.length < 2) { emitError("LIST_RESPONSES requiere surveyId"); return; }
        String surveyId = parts[1];
        try {
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            System.out.println(toJsonResponses(responses));
        } catch (PersistenceException e) {
            emitError(e.getMessage());
        }
    }

    // ==================== AUTH ====================

    private void handleLogin(String[] parts) {
        if (parts.length < 3) { emitError("LOGIN requiere usuario|password"); return; }
        String username = parts[1];
        String password = parts[2];
        Sesion sesion = userController.login(username, password);
        if (sesion == null) {
            emitError("Credenciales inválidas");
            return;
        }
        System.out.println(userToJson(sesion.getUser()));
    }

    private void handleLogout() {
        if (!userController.hasActiveSession()) {
            emitError("No hay sesión activa");
            return;
        }
        userController.logout();
        System.out.println("{\"status\":\"ok\"}");
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 4) { emitError("REGISTER requiere username|name|password"); return; }
        try {
            String id = UUID.randomUUID().toString();
            String username = parts[1];
            String displayName = parts[2];
            String password = parts[3];

            User user = userController.register(id, displayName, username, password);
            if (user == null) {
                emitError("No se pudo registrar el usuario. El nombre de usuario ya podría existir.");
                return;
            }

            // After registration, log in automatically
            Sesion sesion = userController.login(username, password);
            if (sesion != null) {
                System.out.println(userToJson(sesion.getUser()));
            } else {
                emitError("Error al iniciar sesión automáticamente después del registro.");
            }
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    // ==================== RESPONSES ====================

    private void handleAnswerSurvey(String[] parts) {
        if (parts.length < 3) { emitError("ANSWER_SURVEY requiere surveyId|answers"); return; }
        String surveyId = parts[1];
        String answersStr = parts[2];
        try {
            ensureSessionAllowGuest();
            Survey survey = surveyController.loadSurvey(surveyId);
            User respondent = userController.requireActiveUser();
            List<Answer> answers = surveyController.parseAnswers(answersStr, survey);
            if (answers.isEmpty()) {
                emitError("No hay respuestas válidas");
                return;
            }
            SurveyResponse response = responseController.buildResponse(survey, respondent, answers);
            responseController.saveResponse(response);
            System.out.println("{\"status\":\"ok\",\"responseId\":\"" + response.getId() + "\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleEditResponse(String[] parts) {
        if (parts.length < 3) { emitError("EDIT_RESPONSE requiere responseId|answers"); return; }
        String responseId = parts[1];
        String answersStr = parts[2];
        try {
            if (!ensureRegisteredSession()) { emitError("Se requiere sesión registrada"); return; }
            SurveyResponse original = responseController.loadResponse(responseId);
            User current = userController.requireActiveUser();
            if (!current.getId().equals(original.getUserId())) { emitError("No autorizado a editar esta respuesta"); return; }
            Survey survey = surveyController.loadSurvey(original.getSurveyId());
            List<Answer> answers = surveyController.parseAnswers(answersStr, survey);
            responseController.updateResponse(original, answers);
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleDeleteResponse(String[] parts) {
        if (parts.length < 2) { emitError("DELETE_RESPONSE requiere responseId"); return; }
        String responseId = parts[1];
        try {
            if (!ensureRegisteredSession()) { emitError("Se requiere sesión registrada"); return; }
            SurveyResponse resp = responseController.loadResponse(responseId);
            User current = userController.requireActiveUser();
            if (!current.getId().equals(resp.getUserId())) { emitError("No autorizado a eliminar esta respuesta"); return; }
            responseController.deleteResponse(responseId);
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    // ==================== IMPORT/EXPORT ====================

    private void handleImportSurvey(String[] parts) {
        if (parts.length < 2) { emitError("IMPORT_SURVEY requiere ruta"); return; }
        String path = parts[1];
        try {
            Survey s = surveyController.importSurvey(path);
            System.out.println("{\"status\":\"ok\",\"id\":\"" + s.getId() + "\"}");
        } catch (IOException | PersistenceException e) {
            emitError(e.getMessage());
        }
    }

    private void handleExportSurvey(String[] parts) {
        if (parts.length < 3) { emitError("EXPORT_SURVEY requiere surveyId|path"); return; }
        String id = parts[1];
        String path = parts[2];
        try {
            Survey s = surveyController.loadSurvey(id);
            surveySerializer.toFile(s, path);
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleImportResponses(String[] parts) {
        if (parts.length < 2) { emitError("IMPORT_RESPONSES requiere path"); return; }
        String path = parts[1];
        try {
            List<SurveyResponse> responses = responseSerializer.fromFile(path);
            int imported = 0;
            for (SurveyResponse r : responses) {
                surveyController.loadSurvey(r.getSurveyId());
                responseController.saveResponse(r);
                imported++;
            }
            System.out.println("{\"status\":\"ok\",\"imported\":" + imported + "}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleExportResponses(String[] parts) {
        if (parts.length < 3) { emitError("EXPORT_RESPONSES requiere surveyId|path"); return; }
        String surveyId = parts[1];
        String path = parts[2];
        try {
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            responseSerializer.toFile(responses, path);
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    // ==================== ANALYTICS ====================

    private void handlePerformAnalysis(String[] parts) {
        if (parts.length < 2) { emitError("PERFORM_ANALYSIS requiere surveyId"); return; }
        String surveyId = parts[1];
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            AnalyticsResult result = analyticsController.analyzeSurvey(survey, responses);
            System.out.println(analyticsToJson(result));
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    // ==================== SESSION MANAGEMENT ====================

    private void ensureSessionAllowGuest() {
        if (!userController.hasActiveSession()) {
            userController.createGuestSession();
        }
    }

    private boolean ensureRegisteredSession() {
        if (!userController.hasActiveSession()) {
            return false;
        }
        User current = userController.getCurrentSession().getUser();
        return current instanceof RegisteredUser;
    }

    // ==================== JSON SERIALIZATION ====================

    private String userToJson(User user) {
        return "{\"id\":\"" + user.getId() + "\",\"username\":\"" + user.getDisplayName() + "\",\"name\":\"" + user.getDisplayName() + "\"}";
    }

    private String surveyToJson(Survey survey) {
        return "{\"id\":\"" + survey.getId() + "\",\"title\":\"" + survey.getTitle() + "\",\"description\":\"" + survey.getDescription() + "\"}";
    }

    private String toJsonSurveys(Collection<Survey> surveys) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Survey s : surveys) {
            if (!first) sb.append(",");
            sb.append(surveyToJson(s));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String toJsonResponses(List<SurveyResponse> responses) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (SurveyResponse r : responses) {
            if (!first) sb.append(",");
            sb.append("{\"id\":\"").append(r.getId()).append("\"}");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String analyticsToJson(AnalyticsResult result) {
        return "{\"status\":\"ok\",\"result\":\"" + result + "\"}";
    }

    private void emitError(String message) {
        System.out.println("{\"error\":\"" + message + "\"}");
    }

    // ==================== GETTERS ====================

    public UserController getUserController() {
        return userController;
    }

    public SurveyController getSurveyController() {
        return surveyController;
    }

    public ResponseController getResponseController() {
        return responseController;
    }

    public LocalPersistence getPersistence() {
        return persistence;
    }
}
