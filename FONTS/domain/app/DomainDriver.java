package app;

import Exceptions.PersistenceException;
import Response.*;
import Survey.*;
import app.controller.*;
import importexport.*;
import persistence.PersistenceDriver;
import user.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final PersistenceDriver persistenceDriver = new PersistenceDriver();
    private final SurveySerializer surveySerializer = new TxtSurveySerializer();
    private final ResponseSerializer responseSerializer = new TxtResponseSerializer();
    
    // Controladores
    private final UserController userController;
    private final SurveyController surveyController;
    private final ResponseController responseController;
    private final AnalyticsController analyticsController;

    public DomainDriver() {
        this.userController = new UserController(this);
        this.surveyController = new SurveyController(persistenceDriver, surveySerializer);
        this.responseController = new ResponseController(this, persistenceDriver);
        this.analyticsController = new AnalyticsController();

        // Bootstrap persisted users (so LOGIN works after restart)
        try {
            List<RegisteredUser> users = persistenceDriver.loadAllUsers();
            userController.loadRegisteredUsers(users);
        } catch (Exception ignored) {
            // If persistence fails we still allow the app to run with empty users.
        }
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
                case "CREATE_SURVEY_FULL" -> handleCreateSurveyFull(parts);
                case "UPDATE_SURVEY_FULL" -> handleUpdateSurveyFull(parts);
                case "DELETE_SURVEY" -> handleDeleteSurvey(parts);
                case "LIST_RESPONSES" -> handleListResponses(parts);
                case "LOGIN" -> handleLogin(parts);
                case "LOGOUT" -> handleLogout();
                case "IMPORT_SURVEY" -> handleImportSurvey(parts);
                case "EXPORT_SURVEY" -> handleExportSurvey(parts);
                case "REGISTER" -> handleRegister(parts);
                case "DELETE_USER" -> handleDeleteUser(parts);
                case "UPDATE_USER" -> handleUpdateUser(parts);
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
        try {
            Collection<Survey> surveys = surveyController.listSurveys();
            System.out.println(toJsonSurveys(surveys));
        } catch (PersistenceException e) {
            emitError(e.getMessage());
        }
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
            String title = decode(parts[1]);
            String description = decode(parts[2]);
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

    /**
     * Frontend protocol:
     * CREATE_SURVEY_FULL|title|description|k|analysisMethod|questionsPayload
     */
    private void handleCreateSurveyFull(String[] parts) {
        if (parts.length < 6) {
            emitError("CREATE_SURVEY_FULL requiere title|description|k|analysisMethod|questions");
            return;
        }
        try {
            ensureSessionAllowGuest();
            String title = decode(parts[1]);
            String description = decode(parts[2]);
            int k = Integer.parseInt(parts[3]);
            String initMethod = decode(parts[4]);
            String questionsPayload = parts[5];

            String surveyId = UUID.randomUUID().toString();
            User owner = userController.requireActiveUser();
            Survey survey = surveyController.createSurvey(surveyId, title, description, owner, k, initMethod, "euclidean");
            importQuestionsFromPayload(survey, questionsPayload);
            surveyController.saveSurvey(survey);
            System.out.println("{\"status\":\"ok\",\"id\":\"" + survey.getId() + "\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    /**
     * Frontend protocol:
     * UPDATE_SURVEY_FULL|id|title|description|k|analysisMethod|questionsPayload
     */
    private void handleUpdateSurveyFull(String[] parts) {
        if (parts.length < 7) {
            emitError("UPDATE_SURVEY_FULL requiere id|title|description|k|analysisMethod|questions");
            return;
        }
        try {
            if (!ensureRegisteredSession()) {
                emitError("Requiere sesión registrada");
                return;
            }

            String id = parts[1];
            String title = decode(parts[2]);
            String description = decode(parts[3]);
            int k = Integer.parseInt(parts[4]);
            String initMethod = decode(parts[5]);
            String questionsPayload = parts[6];

            Survey survey = surveyController.loadSurvey(id);
            survey.setTitle(title);
            survey.setDescription(description);
            survey.setK(k);
            survey.setInitMethod(initMethod);
            survey.setUpdatedAt(LocalDateTime.now().toString());

            // Replace questions
            survey.getQuestions().clear();
            importQuestionsFromPayload(survey, questionsPayload);

            surveyController.saveSurvey(survey);
            System.out.println("{\"status\":\"ok\"}");
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
            responseController.removeResponsesBySurvey(id);
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
            System.out.println("{\"type\":\"responses\",\"surveyId\":\"" + escapeJson(surveyId) + "\",\"payload\":" + toJsonResponses(responses) + "}");
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
                // Persist users (so they survive restart)
                try {
                    persistenceDriver.persistAllUsers(userController.listRegisteredUsers());
                } catch (Exception ignored) {
                    // Persistence failure should not block UX; LOGIN already succeeded.
                }
                System.out.println(userToJson(sesion.getUser()));
            } else {
                emitError("Error al iniciar sesión automáticamente después del registro.");
            }
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleDeleteUser(String[] parts) {
        if (parts.length < 2) { emitError("DELETE_USER requiere id"); return; }
        try {
            if (!ensureRegisteredSession()) { emitError("Requiere sesión registrada"); return; }
            String id = parts[1];
            boolean ok = userController.deleteUser(id);
            if (!ok) { emitError("Usuario no encontrado"); return; }
            try {
                persistenceDriver.persistAllUsers(userController.listRegisteredUsers());
            } catch (Exception ignored) {
            }
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleUpdateUser(String[] parts) {
        // UPDATE_USER|<urlencoded json> (frontend legacy) OR UPDATE_USER|id|displayName|username|password
        try {
            if (!ensureRegisteredSession()) { emitError("Requiere sesión registrada"); return; }
            RegisteredUser updated = null;
            if (parts.length == 2) {
                // Best-effort minimal JSON field extraction (no external JSON lib allowed)
                String json = decode(parts[1]);
                String id = readJsonStringField(json, "id");
                String displayName = readJsonStringField(json, "name");
                String username = readJsonStringField(json, "username");
                String password = readJsonStringField(json, "password");
                updated = userController.updateUser(id, displayName, username, password);
            } else if (parts.length >= 5) {
                updated = userController.updateUser(parts[1], parts[2], parts[3], parts[4]);
            } else {
                emitError("UPDATE_USER requiere payload");
                return;
            }
            if (updated == null) { emitError("No se pudo actualizar el usuario"); return; }
            try {
                persistenceDriver.persistAllUsers(userController.listRegisteredUsers());
            } catch (Exception ignored) {
            }
            System.out.println("{\"status\":\"ok\"}");
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
            System.out.println(analyticsToJson(surveyId, result));
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
        String username;
        if (user instanceof RegisteredUser ru) {
            username = ru.getUsername();
        } else {
            username = user.getDisplayName();
        }
        return "{\"id\":\"" + escapeJson(user.getId()) + "\",\"username\":\"" + escapeJson(username) + "\",\"name\":\"" + escapeJson(user.getDisplayName()) + "\"}";
    }

    private String surveyToJson(Survey survey) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(survey.getId())).append("\"");
        sb.append(",\"authorId\":\"").append(escapeJson(survey.getCreatedBy())).append("\"");
        sb.append(",\"title\":\"").append(escapeJson(survey.getTitle())).append("\"");
        sb.append(",\"description\":\"").append(escapeJson(survey.getDescription())).append("\"");
        sb.append(",\"clusterSize\":").append(survey.getK());
        sb.append(",\"analysisMethod\":\"").append(escapeJson(survey.getInitMethod())).append("\"");
        sb.append(",\"createdAt\":").append(parseEpochMillisSafe(survey.getCreatedAt()));
        sb.append(",\"questions\":[");
        boolean first = true;
        for (Question q : survey.getQuestions()) {
            if (!first) sb.append(',');
            sb.append(questionToJson(q));
            first = false;
        }
        sb.append("]}");
        return sb.toString();
    }

    private String questionToJson(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(q.getId()).append("\"");
        sb.append(",\"title\":\"").append(escapeJson(q.getText())).append("\"");
        sb.append(",\"mandatory\":").append(q.isRequired() ? "true" : "false");
        sb.append(",\"type\":\"").append(escapeJson(mapQuestionType(q))).append("\"");
        if (q instanceof SingleChoiceQuestion sc) {
            sb.append(",\"options\":[");
            boolean first = true;
            for (ChoiceOption opt : sc.getOptions()) {
                if (!first) sb.append(',');
                sb.append("{\"id\":").append(opt.getId()).append(",\"label\":\"").append(escapeJson(opt.getLabel())).append("\"}");
                first = false;
            }
            sb.append("]");
        } else if (q instanceof MultipleChoiceQuestion mc) {
            sb.append(",\"options\":[");
            boolean first = true;
            for (ChoiceOption opt : mc.getOptions()) {
                if (!first) sb.append(',');
                sb.append("{\"id\":").append(opt.getId()).append(",\"label\":\"").append(escapeJson(opt.getLabel())).append("\"}");
                first = false;
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private String mapQuestionType(Question q) {
        if (q instanceof OpenIntQuestion) return "integer";
        if (q instanceof OpenStringQuestion) return "text";
        if (q instanceof SingleChoiceQuestion) return "single";
        if (q instanceof MultipleChoiceQuestion) return "multiple";
        return "text";
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
            sb.append(responseToJson(r));
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private String responseToJson(SurveyResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(r.getId())).append("\"");
        sb.append(",\"surveyId\":\"").append(escapeJson(r.getSurveyId())).append("\"");
        sb.append(",\"respondentId\":\"").append(escapeJson(r.getUserId())).append("\"");
        sb.append(",\"timestamp\":").append(parseEpochMillisSafe(r.getSubmittedAt()));
        sb.append(",\"answers\":").append(answersToJson(r.getAnswers()));
        sb.append("}");
        return sb.toString();
    }

    private String answersToJson(List<Answer> answers) {
        // Frontend expects: answers: Record<questionId, string|number|string[]|number[]>
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        if (answers != null) {
            for (Answer a : answers) {
                if (a == null) continue;
                if (!first) sb.append(',');
                String qid = String.valueOf(a.getQuestionId());
                sb.append("\"").append(escapeJson(qid)).append("\":");
                switch (a.getType()) {
                    case TEXT -> sb.append("\"").append(escapeJson(((TextAnswer) a).getValue())).append("\"");
                    case INT -> sb.append(((IntAnswer) a).getValue());
                    case SINGLE_CHOICE -> sb.append(((SingleChoiceAnswer) a).getOptionId());
                    case MULTIPLE_CHOICE -> {
                        List<Integer> ids = ((MultipleChoiceAnswer) a).getOptionIds();
                        sb.append('[');
                        if (ids != null) {
                            for (int i = 0; i < ids.size(); i++) {
                                if (i > 0) sb.append(',');
                                sb.append(ids.get(i));
                            }
                        }
                        sb.append(']');
                    }
                    default -> sb.append("null");
                }
                first = false;
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String analyticsToJson(String surveyId, AnalyticsResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"analysis\"");
        sb.append(",\"surveyId\":\"").append(escapeJson(surveyId)).append("\"");
        sb.append(",\"payload\":{");
        sb.append("\"clusters\":").append(result.getClusters());
        sb.append(",\"inertia\":").append(result.getInertia());
        sb.append(",\"averageSilhouette\":").append(result.getAverageSilhouette());
        sb.append(",\"clusterCounts\":{");

        boolean first = true;
        for (var entry : result.getClusterCounts().entrySet()) {
            if (!first) sb.append(',');
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
            first = false;
        }
        sb.append("}");
        sb.append("}}");
        return sb.toString();
    }

    private void emitError(String message) {
        System.out.println("{\"error\":\"" + message + "\"}");
    }

    // ==================== HELPERS ====================

    private String decode(String value) {
        try {
            return URLDecoder.decode(value == null ? "" : value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private long parseEpochMillisSafe(String iso) {
        if (iso == null || iso.isBlank()) return 0L;
        try {
            return LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception ignored) {
            return 0L;
        }
    }

    private void importQuestionsFromPayload(Survey survey, String encodedPayload) throws Exceptions.InvalidQuestionException {
        if (survey == null) return;
        if (encodedPayload == null) return;

        // payload is encodeURIComponent(specs.join(';;')) where each spec has fields joined with '~'
        // and some fields are additionally URL-encoded.
        String decoded = decode(encodedPayload);
        if (decoded.isBlank()) return;

        String[] specs = decoded.split(";;", -1);
        int qid = 1;
        int position = 1;
        for (String spec : specs) {
            if (spec == null || spec.isBlank()) continue;
            String[] fields = spec.split("~", -1);
            String type = fields.length > 0 ? decode(fields[0]) : "text";
            boolean required = fields.length > 1 && "1".equals(fields[1]);
            String title = fields.length > 2 ? decode(fields[2]) : "";
            String options = fields.length > 3 ? decode(fields[3]) : "";

            Question q;
            switch (type) {
                case "integer" -> q = new OpenIntQuestion(qid, title, required, position, 1.0, Integer.MIN_VALUE, Integer.MAX_VALUE);
                case "single" -> {
                    SingleChoiceQuestion sc = new SingleChoiceQuestion(qid, title, required, position, 1.0);
                    hydrateOptions(sc.getOptions(), options);
                    q = sc;
                }
                case "multiple" -> {
                    MultipleChoiceQuestion mc = new MultipleChoiceQuestion(qid, title, required, position, 1.0, 0, Integer.MAX_VALUE);
                    hydrateOptions(mc.getOptions(), options);
                    q = mc;
                }
                default -> q = new OpenStringQuestion(qid, title, required, position, 1.0, 2048);
            }
            survey.addQuestion(q);
            qid++;
            position++;
        }
    }

    private void hydrateOptions(List<ChoiceOption> target, String optJoined) {
        if (target == null) return;
        target.clear();
        if (optJoined == null || optJoined.isBlank()) return;
        String[] labels = optJoined.split(",", -1);
        int oid = 1;
        for (String label : labels) {
            String trimmed = label == null ? "" : label.trim();
            if (trimmed.isEmpty()) continue;
            target.add(new ChoiceOption(oid, trimmed));
            oid++;
        }
    }

    private String readJsonStringField(String json, String key) {
        if (json == null || key == null) return null;
        String needle = "\"" + key + "\"";
        int k = json.indexOf(needle);
        if (k < 0) return null;
        int colon = json.indexOf(':', k + needle.length());
        if (colon < 0) return null;
        int i = colon + 1;
        while (i < json.length() && Character.isWhitespace(json.charAt(i))) i++;
        if (i >= json.length() || json.charAt(i) != '"') return null;
        i++;
        StringBuilder sb = new StringBuilder();
        boolean esc = false;
        for (; i < json.length(); i++) {
            char c = json.charAt(i);
            if (esc) {
                sb.append(c);
                esc = false;
                continue;
            }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') break;
            sb.append(c);
        }
        return sb.toString();
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
