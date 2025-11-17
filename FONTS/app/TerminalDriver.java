package app;

import Exceptions.InvalidArgumentException;
import Exceptions.InvalidQuestionException;
import Exceptions.InvalidSurveyException;
import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.*;
import Survey.*;
import app.controller.*;
import importexport.*;
import user.*;

import java.io.IOException;
import java.util.*;

/**
 * Client en mode terminal que exposa totes les funcionalitats de gestió
 * d'enquestes, usuaris i respostes de la plataforma.
 */
public class TerminalDriver {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalPersistence persistence = new LocalPersistence();
    private final SurveySerializer surveySerializer = new TxtSurveySerializer();
    private final ResponseSerializer responseSerializer = new TxtResponseSerializer();
    private final UserController userController = new UserController();
    private final SurveyController surveyController = new SurveyController(persistence, surveySerializer);
    private final ResponseController responseController = new ResponseController(persistence);
    private final AnalyticsController analyticsController = new AnalyticsController();
    private static final double DEFAULT_QUESTION_WEIGHT = 1.0;
    private static final int DEFAULT_MAX_TEXT_LENGTH = 250;
    private static final int RECOMMENDED_TEXT_LENGTH = 50;
    private static final List<String> VALID_INIT_METHODS = List.of("kmeans++", "random");
    private static final List<String> VALID_DISTANCE_METRICS = List.of("euclidean");

    /**
     * Construeix el controlador de terminal inicialitzant els serveis requerits.
     */
    public TerminalDriver() {
    }

    /**
     * Punt d'entrada de la CLI.
     *
     * @param args paràmetres de línia d'ordres (no utilitzats).
     */
    public static void main(String[] args) {
        new TerminalDriver().start();
    }

    private void start() {
        System.out.println("========================================");
        System.out.println("  Sistema d'Enquestes - Interfície de Terminal");
        System.out.println("========================================\n");

        boolean exit = false;
        while (!exit) {
            showMenu();
            String choice = prompt("Selecciona una opció");
            switch (choice) {
                case "1" -> registerUser();
                case "2" -> login();
                case "3" -> logout();
                case "4" -> viewSurveyDetails();
                case "5" -> createSurvey();
                case "6" -> editSurvey();
                case "7" -> deleteSurvey();
                case "8" -> answerSurvey();
                case "9" -> viewRegisteredResponses();
                case "10" -> editOwnResponse();
                case "11" -> deleteOwnResponse();
                case "12" -> performAnalysis();
                case "13" -> importSurvey();
                case "14" -> importResponsesFromFile();
                case "15" -> exportSurveyToFile();
                case "16" -> exportResponsesToFile();
                case "0" -> exit = true;
                default -> System.out.println("Opció no vàlida. Torna-ho a intentar.");
            }
        }

        System.out.println("Fins aviat!");
    }

    private void showMenu() {
        System.out.println("\n-------------------");
        System.out.println("1) Registrar usuari");
        System.out.println("2) Iniciar sessió");
        System.out.println("3) Tancar sessió");
        System.out.println("4) Veure enquesta");
        System.out.println("5) Crear enquesta");
        System.out.println("6) Editar enquesta");
        System.out.println("7) Eliminar enquesta");
        System.out.println("8) Respondre enquesta");
        System.out.println("9) Veure respostes");
        System.out.println("10) Modificar resposta pròpia");
        System.out.println("11) Esborrar resposta pròpia");
        System.out.println("12) Realitzar anàlisi");
        System.out.println("13) Importar enquesta");
        System.out.println("14) Importar respostes");
        System.out.println("15) Exportar enquesta");
        System.out.println("16) Exportar respostes");
        System.out.println("0) Sortir");
    }

    private void registerUser() {
        System.out.println("\n== Registre d'usuari ==");
        String generatedId = UUID.randomUUID().toString();
        String displayName = promptNonEmpty("Nom per mostrar");
        String username = promptNonEmpty("Nom d'usuari");
        String password = promptNonEmpty("Contrasenya");

        User user = userController.register(generatedId, displayName, username, password);
        if (user != null) {
            System.out.println("Usuari registrat amb èxit: " + user.getDisplayName() +
                " (ID generat " + user.getId() + ")");
        } else {
            System.out.println("No s'ha pogut registrar l'usuari.");
        }
    }

    private void login() {
        if (userController.hasActiveSession()) {
            System.out.println("Ja existeix una sessió activa per " + userController.getCurrentSession().getUser().getDisplayName());
            return;
        }
        System.out.println("\n== Inici de sessió ==");
        String username = promptNonEmpty("Nom d'usuari");
        String password = promptNonEmpty("Contrasenya");
        Sesion sesion = userController.login(username, password);
        if (sesion != null) {
            System.out.println("Sessió iniciada. Benvingut, " + sesion.getUser().getDisplayName() + "!");
        } else {
            System.out.println("Credencials invàlides o usuari inexistent.");
        }
    }

    private void logout() {
        if (!userController.hasActiveSession()) {
            System.out.println("No hi ha cap sessió activa.");
            return;
        }
        if (!promptBoolean("Segur que vols tancar la sessió? (s/N)", false)) {
            System.out.println("Operació cancel·lada.");
            return;
        }
        userController.logout();
        System.out.println("Sessió tancada correctament.");
    }

    private void createSurvey() {
        if (!ensureSession()) return;
        System.out.println("\n== Crear enquesta ==");
        try {
            String surveyId = UUID.randomUUID().toString();
            String title = promptNonEmpty("Títol");
            String description = promptOptional("Descripció", "");
            int k = promptInt("Nombre de clústers (k)", 2);
            String initMethod = promptFromList("Mètode d'inicialització", VALID_INIT_METHODS, "kmeans++");
            String distance = promptFromList("Mètrica de distància", VALID_DISTANCE_METRICS, "euclidean");
            User owner = userController.requireActiveUser();

            Survey survey = surveyController.createSurvey(
                surveyId,
                title,
                description,
                owner,
                k,
                initMethod,
                distance
            );

            int position = 1;
            while (true) {
                if (!promptBoolean("Vols afegir una pregunta? (s/n)", position == 1)) break;
                Question question = buildQuestion(position);
                if (question != null) {
                    survey.addQuestion(question);
                    position++;
                }
            }

            surveyController.saveSurvey(survey);
            System.out.println("Enquesta guardada amb ID " + survey.getId() +
                " i " + survey.getQuestions().size() + " preguntes.");
        } catch (InvalidSurveyException | InvalidQuestionException | PersistenceException e) {
            System.out.println("Error al crear l'enquesta: " + e.getMessage());
        }
    }

    private Question buildQuestion(int position) {
        System.out.println("\n-- Configuració de pregunta #" + position + " --");
        int id = position;
        String text = promptNonEmpty("Text de la pregunta");
        boolean required = promptBoolean("És obligatòria? (s/n)", true);

        while (true) {
            String type = prompt("Tipus [text|numero|single|multi]").toLowerCase();
            switch (type) {
                case "text" -> {
                    System.out.println("Longitud màxima suportada pel sistema: " + DEFAULT_MAX_TEXT_LENGTH + " caràcters.");
                    System.out.println("Pots establir una longitud menor (recomanat <= " + RECOMMENDED_TEXT_LENGTH + ").");
                    int maxLength = promptBoundedInt("Longitud màxima permesa", DEFAULT_MAX_TEXT_LENGTH, 1, DEFAULT_MAX_TEXT_LENGTH);
                    return new OpenStringQuestion(id, text, required, position, DEFAULT_QUESTION_WEIGHT, maxLength);
                }
                case "numero" -> {
                    int min = promptInt("Valor mínim", 0);
                    int max = promptInt("Valor màxim", min + 10);
                    if (max < min) {
                        System.out.println("El màxim ha de ser major o igual que el mínim.");
                        continue;
                    }
                    return new OpenIntQuestion(id, text, required, position, DEFAULT_QUESTION_WEIGHT, min, max);
                }
                case "single" -> {
                    SingleChoiceQuestion sc = new SingleChoiceQuestion(id, text, required, position, DEFAULT_QUESTION_WEIGHT);
                    addChoiceOptions(sc.getOptions(), "opció", 2, 8);
                    return sc;
                }
                case "multi" -> {
                    int minChoices = promptInt("Seleccions mínimes", 1);
                    int maxChoices = promptInt("Seleccions màximes", Math.max(minChoices, 2));
                    if (maxChoices < minChoices) {
                        System.out.println("El màxim ha de ser major o igual que el mínim.");
                        continue;
                    }
                    MultipleChoiceQuestion mc = new MultipleChoiceQuestion(id, text, required, position, DEFAULT_QUESTION_WEIGHT, minChoices, maxChoices);
                    int requiredOptions = Math.max(2, maxChoices);
                    addChoiceOptions(mc.getOptions(), "opció", requiredOptions, 8);
                    return mc;
                }
                default -> System.out.println("Tipus no reconegut. Torna-ho a intentar.");
            }
        }
    }

    private void addChoiceOptions(List<ChoiceOption> options, String label, int minOptions, int maxOptions) {
        int minAllowed = Math.max(2, minOptions);
        int maxAllowed = Math.max(minAllowed, maxOptions);
        if (maxAllowed > 8) {
            maxAllowed = 8;
        }
        if (minAllowed > maxAllowed) {
            minAllowed = maxAllowed;
        }
        int defaultValue = Math.min(Math.max(minAllowed, minOptions), maxAllowed);
        int count = promptBoundedInt("Nombre d'opcions", defaultValue, minAllowed, maxAllowed);
        for (int i = 0; i < count; i++) {
            int optId = i + 1;
            String optLabel = promptNonEmpty("  Text de " + label + " " + (i + 1));
            options.add(new ChoiceOption(optId, optLabel));
        }
    }

    private void importSurvey() {
        if (!ensureSession()) return;
        System.out.println("\n== Importar enquesta ==");
        String path = promptNonEmpty("Ruta de l'arxiu .txt");
        try {
            Survey survey = surveyController.importSurvey(path);
            System.out.println("Enquesta importada amb ID " + survey.getId());
        } catch (IOException | PersistenceException e) {
            System.out.println("No s'ha pogut importar l'enquesta: " + e.getMessage());
        }
    }

    private void importResponsesFromFile() {
        if (!ensureSession()) return;
        System.out.println("\n== Importar respostes ==");
        String path = promptNonEmpty("Ruta de l'arxiu .txt");
        try {
            List<SurveyResponse> responses = responseSerializer.fromFile(path);
            if (responses.isEmpty()) {
                System.out.println("L'arxiu no contenia respostes vàlides.");
                return;
            }
            int imported = 0;
            for (SurveyResponse response : responses) {
                surveyController.loadSurvey(response.getSurveyId());
                responseController.saveResponse(response);
                imported++;
            }
            System.out.println("S'han importat " + imported + " resposta(es) correctament.");
        } catch (IOException e) {
            System.out.println("L'arxiu no té un format vàlid: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("No s'ha pogut guardar la resposta importada: " + e.getMessage());
        }
    }

    private void exportSurveyToFile() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades per exportar.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a exportar");
        if (selected == null) return;
        String path = promptNonEmpty("Ruta destí de l'arxiu .txt");
        try {
            Survey survey = surveyController.loadSurvey(selected.getId());
            surveySerializer.toFile(survey, path);
            System.out.println("Enquesta exportada correctament a " + path);
        } catch (PersistenceException e) {
            System.out.println("No s'ha pogut exportar l'enquesta: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error en escriure l'arxiu: " + e.getMessage());
        }
    }

    private void exportResponsesToFile() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta de la qual vols exportar les respostes");
        if (selected == null) return;
        try {
            List<SurveyResponse> responses = responseController.listResponses(selected.getId());
            if (responses.isEmpty()) {
                System.out.println("L'enquesta no té respostes registrades.");
                return;
            }
            String path = promptNonEmpty("Ruta destí de l'arxiu .txt");
            responseSerializer.toFile(responses, path);
            System.out.println("Respostes exportades correctament a " + path);
        } catch (PersistenceException e) {
            System.out.println("No s'han pogut obtenir les respostes: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error en escriure l'arxiu: " + e.getMessage());
        }
    }

    private void answerSurvey() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes disponibles.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a respondre");
        if (selected == null) return;
        try {
            Survey survey = surveyController.loadSurvey(selected.getId());
            if (survey.getQuestions().isEmpty()) {
                System.out.println("L'enquesta no té preguntes configurades.");
                return;
            }
            List<Answer> answers = new ArrayList<>();
            for (Question question : survey.getQuestions()) {
                Answer answer = askAnswer(question);
                if (answer != null) {
                    answers.add(answer);
                }
            }
            if (answers.isEmpty()) {
                System.out.println("No s'han generat respostes. Operació cancel·lada.");
                return;
            }
            User respondent = userController.requireActiveUser();
            SurveyResponse response = responseController.buildResponse(survey, respondent, answers);
            responseController.saveResponse(response);
            System.out.println("Resposta registrada amb ID " + response.getId());
        } catch (PersistenceException | NullArgumentException | InvalidArgumentException e) {
            System.out.println("Error en guardar la resposta: " + e.getMessage());
        }
    }

    private Answer askAnswer(Question question) throws NullArgumentException, InvalidArgumentException {
        System.out.println("\nPregunta: " + question.getText());
        System.out.println("Tipus: " + question.getClass().getSimpleName());
        if (!question.isRequired()) {
            System.out.println("(Pots deixar en blanc per ometre)");
        }

        if (question instanceof OpenStringQuestion) {
            while (true) {
                String value = prompt("Resposta (text)");
                if (value.isEmpty() && !question.isRequired()) return null;
                if (value.isEmpty()) {
                    System.out.println("La resposta és obligatòria.");
                    continue;
                }
                return new TextAnswer(question.getId(), value);
            }
        }
        if (question instanceof OpenIntQuestion intQuestion) {
            while (true) {
                String value = prompt("Resposta numèrica (" + intQuestion.getMin() + " - " + intQuestion.getMax() + ")");
                if (value.isEmpty() && !question.isRequired()) return null;
                try {
                    int parsed = Integer.parseInt(value);
                    if (parsed < intQuestion.getMin() || parsed > intQuestion.getMax()) {
                        System.out.println("Valor fora de rang.");
                        continue;
                    }
                    return new IntAnswer(question.getId(), parsed);
                } catch (NumberFormatException ex) {
                    System.out.println("Introdueix un número vàlid.");
                }
            }
        }
        if (question instanceof SingleChoiceQuestion singleQuestion) {
            showOptions(singleQuestion.getOptions());
            while (true) {
                String value = prompt("ID de l'opció seleccionada");
                if (value.isEmpty() && !question.isRequired()) return null;
                try {
                    int optionId = Integer.parseInt(value);
                    boolean exists = singleQuestion.getOptions().stream().anyMatch(o -> o.getId() == optionId);
                    if (!exists) {
                        System.out.println("Opció inexistent.");
                        continue;
                    }
                    return new SingleChoiceAnswer(question.getId(), optionId);
                } catch (NumberFormatException ex) {
                    System.out.println("Introdueix un número vàlid.");
                }
            }
        }
        if (question instanceof MultipleChoiceQuestion multipleQuestion) {
            showOptions(multipleQuestion.getOptions());
            System.out.println("Has de seleccionar entre " + multipleQuestion.getMinChoices() + " i " + multipleQuestion.getMaxChoices() + " opcions (IDs separats per comes).");
            while (true) {
                String value = prompt("Selecció");
                if (value.isEmpty() && !question.isRequired()) return null;
                List<Integer> selections = parseSelection(value);
                if (selections.size() < multipleQuestion.getMinChoices() || selections.size() > multipleQuestion.getMaxChoices()) {
                    System.out.println("Quantitat invàlida d'opcions.");
                    continue;
                }
                boolean allExist = selections.stream().allMatch(id -> multipleQuestion.getOptions().stream().anyMatch(o -> o.getId() == id));
                if (!allExist) {
                    System.out.println("Alguna opción no existe. Revise los IDs.");
                    continue;
                }
                return new MultipleChoiceAnswer(question.getId(), selections);
            }
        }
        System.out.println("Tipo de pregunta no soportado, se omitirá.");
        return null;
    }

    private void performAnalysis() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a analitzar");
        if (selected == null) return;
        try {
            Survey survey = surveyController.loadSurvey(selected.getId());
            List<SurveyResponse> responses = responseController.listResponses(selected.getId());
            if (responses.size() < 2) {
                System.out.println("Es requereixen almenys 2 respostes per analitzar.");
                return;
            }

            AnalyticsResult result = analyticsController.analyzeSurvey(survey, responses);

            System.out.println("\n== Resultat de l'anàlisi ==");
            System.out.println("Enquesta: " + survey.getTitle());
            System.out.println("Clústers (k): " + result.getClusters());
            result.getClusterCounts().forEach((cluster, total) ->
                System.out.println("  Clúster " + cluster + ": " + total + " respostes")
            );
            System.out.printf("Inèrcia (SSE): %.4f%n", result.getInertia());
            System.out.printf("Silhouette mitjana: %.4f%n", result.getAverageSilhouette());
        } catch (PersistenceException e) {
            System.out.println("No s'ha pogut executar l'anàlisi: " + e.getMessage());
        }
    }

    private void viewSurveyDetails() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a visualitzar");
        if (selected == null) return;
        try {
            Survey survey = surveyController.loadSurvey(selected.getId());
            System.out.println("\n== Detall de l'enquesta ==");
            System.out.println("ID: " + survey.getId());
            System.out.println("Títol: " + survey.getTitle());
            System.out.println("Descripció: " + survey.getDescription());
            System.out.println("Preguntes configurades: " + survey.getQuestions().size());

            if (survey.getQuestions().isEmpty()) {
                System.out.println("L'enquesta no té preguntes definides.");
                return;
            }

            survey.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getPosition))
                .forEach(this::printQuestionDetails);
        } catch (PersistenceException e) {
            System.out.println("No s'ha pogut carregar l'enquesta: " + e.getMessage());
        }
    }

    private void printQuestionDetails(Question question) {
        System.out.println("\nPregunta #" + question.getPosition() + " (ID " + question.getId() + ")");
        System.out.println("Text: " + question.getText());
        System.out.println("Tipus: " + question.getClass().getSimpleName());
        System.out.println("Obligatòria: " + (question.isRequired() ? "Sí" : "No"));

        if (question instanceof OpenIntQuestion intQuestion) {
            System.out.println("Camp: número enter entre " + intQuestion.getMin() + " i " + intQuestion.getMax());
        } else if (question instanceof OpenStringQuestion stringQuestion) {
            System.out.println("Camp: text (màx " + stringQuestion.getMaxLength() + " caràcters)");
        } else if (question instanceof SingleChoiceQuestion singleQuestion) {
            System.out.println("Camp: seleccionar una única opció.");
            showOptions(singleQuestion.getOptions());
        } else if (question instanceof MultipleChoiceQuestion multipleQuestion) {
            System.out.println("Camp: seleccionar entre " + multipleQuestion.getMinChoices() + " i " + multipleQuestion.getMaxChoices() + " opcions.");
            showOptions(multipleQuestion.getOptions());
        } else {
            System.out.println("Camp: tipus de pregunta no suportat.");
        }
    }

    private void viewRegisteredResponses() {
        if (!ensureSession()) return;
        List<SurveyResponse> responses = responseController.listAllResponses();
        if (responses.isEmpty()) {
            System.out.println("No s'han registrat respostes encara.");
            return;
        }

        System.out.println("\n== Respostes registrades ==");
        Map<String, String> userNames = buildUserDisplayMap();
        Map<String, String> surveyTitles = new HashMap<>();
        for (SurveyResponse response : responses) {
            String surveyTitle = surveyTitles.computeIfAbsent(response.getSurveyId(), this::resolveSurveyTitle);
            String userDisplay = userNames.getOrDefault(response.getUserId(), response.getUserId());
            System.out.println("\nResposta ID: " + response.getId());
            System.out.println("Enquesta: " + surveyTitle + " (" + response.getSurveyId() + ")");
            System.out.println("Usuari: " + userDisplay + " (" + response.getUserId() + ")");
            System.out.println("Data: " + (response.getSubmittedAt() == null ? "N/A" : response.getSubmittedAt()));
            System.out.println("Respostes:");

            Map<Integer, String> questionTexts = buildQuestionTextMap(response.getSurveyId());
            if (response.getAnswers().isEmpty()) {
                System.out.println("  (Sense respostes registrades)");
                continue;
            }
            response.getAnswers().forEach(answer -> {
                String qText = questionTexts.get(answer.getQuestionId());
                System.out.println("  Pregunta " + answer.getQuestionId() + (qText != null ? " - " + qText : "") + " -> " + formatAnswer(answer));
            });
        }
    }

    private void editOwnResponse() {
        if (!ensureSession()) return;
        User currentUser = userController.requireActiveUser();
        try {
            List<SurveyResponse> responses = responseController.listResponsesByUser(currentUser.getId());
            if (responses.isEmpty()) {
                System.out.println("Encara no tens respostes per modificar.");
                return;
            }
            SurveyResponse selected = selectResponse(responses, "la resposta a modificar");
            if (selected == null) return;
            Survey survey = surveyController.loadSurvey(selected.getSurveyId());
            System.out.println("Modificant la resposta per a l'enquesta " + survey.getTitle());

            List<Answer> newAnswers = new ArrayList<>();
            for (Question question : survey.getQuestions()) {
                Answer answer = askAnswer(question);
                if (answer != null) {
                    newAnswers.add(answer);
                }
            }
            if (newAnswers.isEmpty()) {
                System.out.println("No s'han capturat respostes. Operació cancel·lada.");
                return;
            }
            responseController.updateResponse(selected, newAnswers);
            System.out.println("Resposta actualitzada correctament.");
        } catch (PersistenceException | NullArgumentException | InvalidArgumentException e) {
            System.out.println("No s'ha pogut modificar la resposta: " + e.getMessage());
        }
    }

    private void deleteOwnResponse() {
        if (!ensureSession()) return;
        User currentUser = userController.requireActiveUser();
        try {
            List<SurveyResponse> responses = responseController.listResponsesByUser(currentUser.getId());
            if (responses.isEmpty()) {
                System.out.println("Encara no tens respostes per eliminar.");
                return;
            }
            SurveyResponse selected = selectResponse(responses, "la resposta a eliminar");
            if (selected == null) return;
            if (!promptBoolean("Confirmes que vols eliminar la resposta? (s/n)", false)) {
                System.out.println("Operació cancel·lada.");
                return;
            }
            responseController.deleteResponse(selected.getId());
            System.out.println("Resposta eliminada correctament.");
        } catch (PersistenceException e) {
            System.out.println("No s'ha pogut eliminar la resposta: " + e.getMessage());
        }
    }

    private void editSurvey() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades per editar.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a editar");
        if (selected == null) return;
        try {
            Survey survey = surveyController.loadSurvey(selected.getId());
            User currentUser = userController.requireActiveUser();
            if (!currentUser.getId().equals(survey.getCreatedBy())) {
                System.out.println("Només l'usuari creador pot editar aquesta enquesta.");
                return;
            }
            System.out.println("\n== Editant enquesta ==");
            System.out.println("Deixa en blanc per mantenir el valor actual.");

            String newTitle = promptOptional("Nou títol", survey.getTitle());
            String newDescription = promptOptional("Nova descripció", survey.getDescription());
            int newK = promptInt("Nombre de clústers (k)", survey.getK());
            String newInitMethod = promptFromList("Mètode d'inicialització", VALID_INIT_METHODS, survey.getInitMethod());
            String newDistance = promptFromList("Mètrica de distància", VALID_DISTANCE_METRICS, survey.getDistance());

            String originalId = survey.getId();
            Survey updatedSurvey = surveyController.editSurvey(selected.getId(), selected.getId(), newTitle, newDescription, newK, newInitMethod, newDistance);
            System.out.println("Enquesta editada correctament.");

            if (promptBoolean("Vols modificar preguntes? (vigila, totes les respostes seran esborrades) (s/n)", false)) {
                try {
                    responseController.removeResponsesBySurvey(originalId);
                    if (!originalId.equals(updatedSurvey.getId())) {
                        responseController.removeResponsesBySurvey(updatedSurvey.getId());
                    }
                } catch (PersistenceException e) {
                    System.out.println("No s'han pogut eliminar les respostes existents: " + e.getMessage());
                    return;
                }

                updatedSurvey.getQuestions().clear();
                int position = 1;
                while (promptBoolean("Vols afegir una pregunta? (s/n)", position == 1)) {
                    Question question = buildQuestion(position);
                    if (question == null) {
                        continue;
                    }
                    try {
                        updatedSurvey.addQuestion(question);
                        position++;
                    } catch (InvalidQuestionException e) {
                        System.out.println("Pregunta rebutjada: " + e.getMessage());
                    }
                }

                try {
                    surveyController.saveSurvey(updatedSurvey);
                    System.out.println("Preguntes actualitzades. Totes les respostes anteriors han estat eliminades.");
                } catch (PersistenceException e) {
                    System.out.println("No s'han pogut guardar les noves preguntes: " + e.getMessage());
                }
            }
        } catch (PersistenceException | InvalidSurveyException e) {
            System.out.println("Error en editar l'enquesta: " + e.getMessage());
        }
    }

    private void deleteSurvey() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades per eliminar.");
            return;
        }
        Survey selected = selectSurvey(surveys, "l'enquesta a eliminar");
        if (selected == null) return;
        if (!promptBoolean("Estàs segur que vols eliminar l'enquesta i totes les seves respostes? (s/n)", false)) {
            System.out.println("Operació cancel·lada.");
            return;
        }
        try {
            surveyController.deleteSurvey(selected.getId());
            System.out.println("Enquesta eliminada correctament.");
        } catch (PersistenceException e) {
            System.out.println("Error en eliminar l'enquesta: " + e.getMessage());
        }
    }

    private Map<Integer, String> buildQuestionTextMap(String surveyId) {
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            Map<Integer, String> map = new HashMap<>();
            for (Question question : survey.getQuestions()) {
                map.put(question.getId(), question.getText());
            }
            return map;
        } catch (PersistenceException e) {
            return Collections.emptyMap();
        }
    }

    private String formatAnswer(Answer answer) {
        if (answer instanceof TextAnswer text) {
            return text.getValue();
        }
        if (answer instanceof IntAnswer number) {
            return String.valueOf(number.getValue());
        }
        if (answer instanceof SingleChoiceAnswer single) {
            return "Opció única -> " + single.getOptionId();
        }
        if (answer instanceof MultipleChoiceAnswer multi) {
            return "Opcions -> " + multi.getOptionIds();
        }
        return answer.toString();
    }

    private Map<String, String> buildUserDisplayMap() {
        Map<String, String> names = new HashMap<>();
        for (RegisteredUser user : userController.listRegisteredUsers()) {
            names.put(user.getId(), user.getDisplayName());
        }
        return names;
    }

    private String resolveSurveyTitle(String surveyId) {
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            return survey.getTitle();
        } catch (PersistenceException e) {
            return surveyId;
        }
    }

    private SurveyResponse selectResponse(List<SurveyResponse> responses, String contextLabel) {
        if (responses.isEmpty()) {
            return null;
        }
        Map<String, String> surveyTitles = new HashMap<>();
        System.out.println("\nRespostes disponibles:");
        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse response = responses.get(i);
            String surveyTitle = surveyTitles.computeIfAbsent(response.getSurveyId(), this::resolveSurveyTitle);
            String submittedAt = response.getSubmittedAt() == null ? "N/A" : response.getSubmittedAt();
            System.out.println(" " + (i + 1) + ") " + surveyTitle + " | " + submittedAt + " -> ID " + response.getId());
        }
        int index = promptIndex("Selecciona " + contextLabel, 1, responses.size());
        return responses.get(index - 1);
    }

    private Survey selectSurvey(Collection<Survey> surveys, String contextLabel) {
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades.");
            return null;
        }
        List<Survey> ordered = new ArrayList<>(surveys);
        System.out.println("\nEnquestes disponibles:");
        for (int i = 0; i < ordered.size(); i++) {
            Survey survey = ordered.get(i);
            System.out.println(" " + (i + 1) + ") " + survey.getTitle() + " [" + survey.getId() + "]");
        }
        int selected = promptIndex("Selecciona " + contextLabel, 1, ordered.size());
        return ordered.get(selected - 1);
    }

    private boolean ensureSession() {
        if (!userController.hasActiveSession()) {
            System.out.println("Has d'iniciar sessió per aquesta acció.");
            return false;
        }
        userController.refreshSession();
        return true;
    }

    private void showOptions(List<ChoiceOption> options) {
        System.out.println("Opcions disponibles:");
        options.forEach(o -> System.out.println("  [" + o.getId() + "] " + o.getLabel()));
    }

    private List<Integer> parseSelection(String value) {
        String[] tokens = value.split(",");
        List<Integer> selections = new ArrayList<>();
        for (String token : tokens) {
            try {
                int id = Integer.parseInt(token.trim());
                if (!selections.contains(id)) {
                    selections.add(id);
                }
            } catch (NumberFormatException ex) {
                System.out.println("Valor ignorado: " + token);
            }
        }
        return selections;
    }

    private String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private String promptNonEmpty(String label) {
        while (true) {
            String value = prompt(label);
            if (!value.isEmpty()) return value;
            System.out.println("El valor no pot estar buit.");
        }
    }

    private String promptOptional(String label, String defaultValue) {
        String value = prompt(label + " [" + defaultValue + "]");
        return value.isEmpty() ? defaultValue : value;
    }

    private int promptInt(String label, int defaultValue) {
        while (true) {
            String value = prompt(label + " [" + defaultValue + "]");
            if (value.isEmpty()) return defaultValue;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                System.out.println("Introdueix un número enter vàlid.");
            }
        }
    }

    private int promptBoundedInt(String label, int defaultValue, int minInclusive, int maxInclusive) {
        while (true) {
            int value = promptInt(label, defaultValue);
            if (value >= minInclusive && value <= maxInclusive) {
                return value;
            }
            System.out.println("Introdueix un valor entre " + minInclusive + " i " + maxInclusive + ".");
        }
    }

    private String promptFromList(String label, List<String> allowed, String defaultValue) {
        String normalizedDefault = normalizeOption(defaultValue);
        if (!allowed.contains(normalizedDefault)) {
            normalizedDefault = allowed.get(0);
        }
        String options = String.join(", ", allowed);
        while (true) {
            String value = prompt(label + " [" + normalizedDefault + "] (opcions: " + options + ")");
            if (value.isEmpty()) {
                return normalizedDefault;
            }
            String normalized = normalizeOption(value);
            if (allowed.contains(normalized)) {
                return normalized;
            }
            System.out.println("Opció no vàlida. Valides: " + options + ".");
        }
    }

    private String normalizeOption(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private int promptIndex(String label, int minInclusive, int maxInclusive) {
        while (true) {
            String value = prompt(label + " [" + minInclusive + "-" + maxInclusive + "]");
            if (value.isEmpty()) return minInclusive;
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= minInclusive && parsed <= maxInclusive) {
                    return parsed;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Introdueix un valor entre " + minInclusive + " i " + maxInclusive + ".");
        }
    }

    private boolean promptBoolean(String label, boolean defaultValue) {
        while (true) {
            String value = prompt(label).trim().toLowerCase(Locale.ROOT);
            if (value.isEmpty()) return defaultValue;
            if (value.equals("s") || value.equals("si") || value.equals("sí") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("Respon amb 's' o 'n'.");
        }
    }
}
