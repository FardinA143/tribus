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
 * Driver pensado para integrarse con una app Electron mediante stdin/stdout.
 * Protocolo simple por líneas: ACCION|ARG1|ARG2|...
 * Cada respuesta se emite como una única línea JSON.
 */
public class ElectronDriver {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalPersistence persistence = new LocalPersistence();
    private final SurveySerializer surveySerializer = new TxtSurveySerializer();
    private final ResponseSerializer responseSerializer = new TxtResponseSerializer();
    private final UserController userController = new UserController();
    private final SurveyController surveyController = new SurveyController(persistence, surveySerializer);
    private final ResponseController responseController = new ResponseController(persistence);
    private final AnalyticsController analyticsController = new AnalyticsController();

    public static void main(String[] args) {
        new ElectronDriver().start();
    }

    public void start() {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            // --- INICIO DEBUG ---
            System.err.println("JAVA RECIBIDO: " + line);
            // --- FIN DEBUG ---
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
        // CREATE_SURVEY|title|description|k
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
        // EXPORT_SURVEY|surveyId|path
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

    private void handleRegister(String[] parts) {
        // REGISTER|username|name|password
        if (parts.length < 4) { emitError("REGISTER requiere username|name|password"); return; }
        try {
            String id = UUID.randomUUID().toString();
            String username = parts[1];
            String displayName = parts[2]; // 'name' from UI is 'displayName' in backend
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

    private void handleAnswerSurvey(String[] parts) {
        // ANSWER_SURVEY|surveyId|q1:val1;q2:val2;...
        if (parts.length < 3) { emitError("ANSWER_SURVEY requiere surveyId|answers"); return; }
        String surveyId = parts[1];
        String answersStr = parts[2];
        try {
            ensureSessionAllowGuest();
            Survey survey = surveyController.loadSurvey(surveyId);
            User respondent = userController.requireActiveUser();
            List<Answer> answers = parseAnswers(answersStr, survey);
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
        // EDIT_RESPONSE|responseId|q1:val1;...
        if (parts.length < 3) { emitError("EDIT_RESPONSE requiere responseId|answers"); return; }
        String responseId = parts[1];
        String answersStr = parts[2];
        try {
            if (!ensureRegisteredSession()) { emitError("Se requiere sesión registrada"); return; }
            SurveyResponse original = responseController.loadResponse(responseId);
            User current = userController.requireActiveUser();
            if (!current.getId().equals(original.getUserId())) { emitError("No autorizado a editar esta respuesta"); return; }
            Survey survey = surveyController.loadSurvey(original.getSurveyId());
            List<Answer> answers = parseAnswers(answersStr, survey);
            responseController.updateResponse(original, answers);
            System.out.println("{\"status\":\"ok\"}");
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    private void handleDeleteResponse(String[] parts) {
        // DELETE_RESPONSE|responseId
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

    private void handleImportResponses(String[] parts) {
        // IMPORT_RESPONSES|path
        if (parts.length < 2) { emitError("IMPORT_RESPONSES requiere path"); return; }
        String path = parts[1];
        try {
            List<SurveyResponse> responses = responseSerializer.fromFile(path);
            int imported = 0;
            for (SurveyResponse r : responses) {
                // Ensure survey loaded and then save
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
        // EXPORT_RESPONSES|surveyId|path
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

    private void handlePerformAnalysis(String[] parts) {
        // PERFORM_ANALYSIS|surveyId
        if (parts.length < 2) { emitError("PERFORM_ANALYSIS requiere surveyId"); return; }
        String surveyId = parts[1];
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            if (responses.size() < 2) { emitError("Se requieren al menos 2 respuestas para analizar"); return; }
            AnalyticsResult result = analyticsController.analyzeSurvey(survey, responses);
            // Build JSON simple
            StringBuilder sb = new StringBuilder();
            sb.append("{\"k\":").append(result.getClusters());
            sb.append(",\"inertia\":").append(result.getInertia());
            sb.append(",\"averageSilhouette\":").append(result.getAverageSilhouette());
            sb.append(",\"counts\":{");
            int i = 0;
            for (var entry : result.getClusterCounts().entrySet()) {
                sb.append('"').append(entry.getKey()).append("\":").append(entry.getValue());
                if (i < result.getClusterCounts().size() - 1) sb.append(',');
                i++;
            }
            sb.append("}}");
            System.out.println(sb.toString());
        } catch (Exception e) {
            emitError(e.getMessage());
        }
    }

    /**
     * Parse answer string into Answer objects using survey question definitions.
     * Format: "qid:val;qid:val1,val2;qid:val". Multiple choice values may be comma-separated.
     */
    private List<Answer> parseAnswers(String answersStr, Survey survey) {
        List<Answer> answers = new ArrayList<>();
        if (answersStr == null || answersStr.isBlank()) return answers;
        String[] parts = answersStr.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) continue;
            String[] kv = trimmed.split(":", 2);
            if (kv.length < 2) continue;
            try {
                int qid = Integer.parseInt(kv[0].trim());
                String val = kv[1].trim();
                Question q = survey.getQuestions().stream().filter(x -> x.getId() == qid).findFirst().orElse(null);
                if (q == null) continue;
                if (q instanceof OpenStringQuestion) {
                    answers.add(new TextAnswer(qid, val));
                } else if (q instanceof OpenIntQuestion) {
                    int v = Integer.parseInt(val);
                    answers.add(new IntAnswer(qid, v));
                } else if (q instanceof SingleChoiceQuestion) {
                    int opt = Integer.parseInt(val);
                    answers.add(new SingleChoiceAnswer(qid, opt));
                } else if (q instanceof MultipleChoiceQuestion) {
                    // allow comma or pipe separated
                    String normalized = val.replace('|', ',');
                    answers.add(new MultipleChoiceAnswer(qid, normalized));
                }
            } catch (Exception ignored) {
            }
        }
        return answers;
    }

    private boolean ensureSessionAllowGuest() {
        if (!userController.hasActiveSession()) {
            userController.createGuestSession();
            return true;
        }
        userController.refreshSession();
        return true;
    }

    private boolean ensureRegisteredSession() {
        if (!userController.hasActiveSession()) return false;
        if (!(userController.getCurrentSession().getUser() instanceof RegisteredUser)) return false;
        userController.refreshSession();
        return true;
    }

    private void emitError(String msg) {
        if (msg == null) msg = "error";
        msg = msg.replace("\"", "\\\"");
        System.out.println("{\"error\":\"" + msg + "\"}");
    }

    // Simple JSON helpers
    private String toJsonSurveys(Collection<Survey> surveys) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int i = 0;
        for (Survey s : surveys) {
            sb.append(surveyToJson(s));
            if (i < surveys.size() - 1) sb.append(",");
            i++;
        }
        sb.append("]");
        return sb.toString();
    }

    private String surveyToJson(Survey s) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escape(s.getId())).append("\"");
        sb.append(",\"title\":\"").append(escape(s.getTitle())).append("\"");
        sb.append(",\"description\":\"").append(escape(s.getDescription())).append("\"");
        sb.append(",\"k\":").append(s.getK());
        sb.append(",\"questions\":[");
        int j = 0;
        for (Question q : s.getQuestions()) {
            sb.append(questionToJson(q));
            if (j < s.getQuestions().size() - 1) sb.append(",");
            j++;
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String questionToJson(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":").append(q.getId());
        sb.append(",\"position\":").append(q.getPosition());
        sb.append(",\"text\":\"").append(escape(q.getText())).append("\"");
        sb.append(",\"type\":\"").append(q.getClass().getSimpleName()).append("\"");
        if (q instanceof OpenStringQuestion osq) {
            sb.append(",\"maxLength\":").append(osq.getMaxLength());
        } else if (q instanceof OpenIntQuestion iq) {
            sb.append(",\"min\":").append(iq.getMin()).append(",\"max\":").append(iq.getMax());
        } else if (q instanceof SingleChoiceQuestion sc) {
            sb.append(",\"options\":[");
            int i = 0;
            for (ChoiceOption o : sc.getOptions()) {
                sb.append("{\"id\":").append(o.getId()).append(",\"label\":\"").append(escape(o.getLabel())).append("\"}");
                if (i < sc.getOptions().size() - 1) sb.append(",");
                i++;
            }
            sb.append("]");
        } else if (q instanceof MultipleChoiceQuestion mc) {
            sb.append(",\"minChoices\":").append(mc.getMinChoices()).append(",\"maxChoices\":").append(mc.getMaxChoices());
            sb.append(",\"options\":[");
            int i = 0;
            for (ChoiceOption o : mc.getOptions()) {
                sb.append("{\"id\":").append(o.getId()).append(",\"label\":\"").append(escape(o.getLabel())).append("\"}");
                if (i < mc.getOptions().size() - 1) sb.append(",");
                i++;
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    private String toJsonResponses(List<SurveyResponse> responses) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse r = responses.get(i);
            sb.append(responseToJson(r));
            if (i < responses.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String responseToJson(SurveyResponse r) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escape(r.getId())).append("\"");
        sb.append(",\"surveyId\":\"").append(escape(r.getSurveyId())).append("\"");
        sb.append(",\"userId\":\"").append(escape(r.getUserId())).append("\"");
        sb.append(",\"answers\":[");
        for (int i = 0; i < r.getAnswers().size(); i++) {
            Answer a = r.getAnswers().get(i);
            sb.append(answerToJson(a));
            if (i < r.getAnswers().size() - 1) sb.append(",");
        }
        sb.append("]");
        sb.append("}");
        return sb.toString();
    }

    private String answerToJson(Answer a) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"questionId\":").append(a.getQuestionId());
        if (a instanceof TextAnswer t) {
            sb.append(",\"text\":\"").append(escape(t.getValue())).append("\"");
        } else if (a instanceof IntAnswer n) {
            sb.append(",\"value\":").append(n.getValue());
        } else if (a instanceof SingleChoiceAnswer s) {
            sb.append(",\"optionId\":").append(s.getOptionId());
        } else if (a instanceof MultipleChoiceAnswer m) {
            sb.append(",\"optionIds\":").append(m.getOptionIds());
        }
        sb.append("}");
        return sb.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String userToJson(User u) {
        if (u == null) return "null";
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escape(u.getId())).append("\"");
        // Corregido: Asumo que el nombre de usuario se obtiene del ID o un campo similar si getUsername() no existe.
        // Deberías ajustar esto a tu modelo de User.
        sb.append(",\"username\":\"").append(escape(u.getId())).append("\"");
        sb.append(",\"name\":\"").append(escape(u.getDisplayName())).append("\"");
        // No se incluye la contraseña en la respuesta por seguridad.
        sb.append("}");
        return sb.toString();
    }
}
