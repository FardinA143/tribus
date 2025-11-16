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

public class TerminalDriver {
    private final Scanner scanner = new Scanner(System.in);
    private final LocalPersistence persistence = new LocalPersistence();
    private final SurveySerializer surveySerializer = new TxtSurveySerializer();
    private final ResponseSerializer responseSerializer = new TxtResponseSerializer();
    private final UserController userController = new UserController();
    private final SurveyController surveyController = new SurveyController(persistence, surveySerializer);
    private final ResponseController responseController = new ResponseController(persistence);
    private final AnalyticsController analyticsController = new AnalyticsController();

    public static void main(String[] args) {
        new TerminalDriver().start();
    }

    private void start() {
        System.out.println("========================================");
        System.out.println("  Sistema d'Enquestes - Interfície de Terminal");
        System.out.println("========================================\n");

        boolean exit = false;
        while (!exit) {
            pageBreak();
            showMenu();
            String choice = prompt("Selecciona una opció");
            switch (choice) {
                case "1" -> registerUser();
                case "2" -> login();
                case "3" -> logout();
                case "4" -> createSurvey();
                case "5" -> answerSurvey();
                case "6" -> importSurvey();
                case "7" -> exportSurveyToFile();
                case "8" -> importResponsesFromFile();
                case "9" -> exportResponsesToFile();
                case "10" -> performAnalysis();
                case "11" -> viewSurveyDetails();
                case "12" -> viewRegisteredUsers();
                case "13" -> viewRegisteredResponses();
                case "14" -> editSurvey();
                case "15" -> deleteSurvey();
                case "0" -> exit = true;
                default -> System.out.println("Opció no vàlida. Torna-ho a intentar.");
            }
            if (!exit) {
                prompt("Prem Enter per continuar");
            }
        }

        System.out.println("Fins aviat!");
    }

    private void showMenu() {
        System.out.println("\n-------------------");
        System.out.println("1) Registrar usuari");
        System.out.println("2) Iniciar sessió");
        System.out.println("3) Tancar sessió");
        System.out.println("4) Crear enquesta");
        System.out.println("5) Respondre enquesta");
        System.out.println("6) Importar enquesta des d'arxiu");
        System.out.println("7) Exportar enquesta a arxiu");
        System.out.println("8) Importar respostes des d'arxiu");
        System.out.println("9) Exportar respostes a arxiu");
        System.out.println("10) Realitzar anàlisi");
        System.out.println("11) Veure enquesta");
        System.out.println("12) Veure usuaris registrats");
        System.out.println("13) Veure respostes registrades");
        System.out.println("14) Editar enquesta");
        System.out.println("15) Eliminar enquesta");
        System.out.println("0) Sortir");
    }

    private void registerUser() {
        System.out.println("\n== Registre d'usuari ==");
        String idInput = promptOptional("ID (enter per autogenerar)", UUID.randomUUID().toString());
        String displayName = promptNonEmpty("Nom per mostrar");
        String username = promptNonEmpty("Nom d'usuari");
        String password = promptNonEmpty("Contrasenya");

        User user = userController.register(idInput, displayName, username, password);
        if (user != null) {
            System.out.println("Usuari registrat amb èxit: " + user.getDisplayName());
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
        userController.logout();
        System.out.println("Sessió tancada correctament.");
    }

    private void createSurvey() {
        if (!ensureSession()) return;
        System.out.println("\n== Crear enquesta ==");
        try {
            String surveyId = promptOptional("ID d'enquesta (enter per autogenerar)", UUID.randomUUID().toString());
            String title = promptNonEmpty("Títol");
            String description = promptOptional("Descripció", "");
            int k = promptInt("Nombre de clústers (k)", 2);
            String initMethod = promptOptional("Mètode d'inicialització", "kmeans++");
            String distance = promptOptional("Mètrica de distància", "euclidean");
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
            System.out.println("Enquesta guardada amb " + survey.getQuestions().size() + " preguntes.");
        } catch (InvalidSurveyException | InvalidQuestionException | PersistenceException e) {
            System.out.println("Error al crear l'enquesta: " + e.getMessage());
        }
    }

    private Question buildQuestion(int position) {
        System.out.println("\n-- Configuració de pregunta #" + position + " --");
        int id = promptInt("ID intern", position);
        String text = promptNonEmpty("Text de la pregunta");
        boolean required = promptBoolean("És obligatòria? (s/n)", true);
        double weight = promptDouble("Pes (per defecte 1.0)", 1.0);

        while (true) {
            String type = prompt("Tipus [text|numero|single|multi]").toLowerCase();
            switch (type) {
                case "text" -> {
                    int maxLength = promptInt("Longitud màxima", 280);
                    return new OpenStringQuestion(id, text, required, position, weight, maxLength);
                }
                case "numero" -> {
                    int min = promptInt("Valor mínim", 0);
                    int max = promptInt("Valor màxim", min + 10);
                    if (max < min) {
                        System.out.println("El màxim ha de ser major o igual que el mínim.");
                        continue;
                    }
                    return new OpenIntQuestion(id, text, required, position, weight, min, max);
                }
                case "single" -> {
                    SingleChoiceQuestion sc = new SingleChoiceQuestion(id, text, required, position, weight);
                    addChoiceOptions(sc.getOptions(), "opció", 2);
                    return sc;
                }
                case "multi" -> {
                    int minChoices = promptInt("Seleccions mínimes", 1);
                    int maxChoices = promptInt("Seleccions màximes", Math.max(minChoices, 2));
                    if (maxChoices < minChoices) {
                        System.out.println("El màxim ha de ser major o igual que el mínim.");
                        continue;
                    }
                    MultipleChoiceQuestion mc = new MultipleChoiceQuestion(id, text, required, position, weight, minChoices, maxChoices);
                    addChoiceOptions(mc.getOptions(), "opció", maxChoices);
                    return mc;
                }
                default -> System.out.println("Tipus no reconegut. Torna-ho a intentar.");
            }
        }
    }

    private void addChoiceOptions(List<ChoiceOption> options, String label, int minimum) {
        int count = Math.max(minimum, promptInt("Nombre d'opcions", minimum));
        for (int i = 0; i < count; i++) {
            int optId = promptInt("  ID de " + label + " " + (i + 1), i + 1);
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
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a exportar");
        String path = promptNonEmpty("Ruta destí de l'arxiu .txt");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
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
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta les respostes de la qual vols exportar");
        try {
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
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
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a respondre");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
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
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a analitzar");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
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

        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a visualitzar");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
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

    private void viewRegisteredUsers() {
        if (!ensureSession()) return;
        Collection<RegisteredUser> users = userController.listRegisteredUsers();
        if (users.isEmpty()) {
            System.out.println("Encara no hi ha usuaris registrats.");
            return;
        }

        System.out.println("\n== Usuaris registrats ==");
        users.forEach(user -> System.out.println(" - ID: " + user.getId() + ", Usuari: " + user.getUsername() + ", Nom: " + user.getDisplayName()));
    }

    private void viewRegisteredResponses() {
        if (!ensureSession()) return;
        List<SurveyResponse> responses = responseController.listAllResponses();
        if (responses.isEmpty()) {
            System.out.println("No s'han registrat respostes encara.");
            return;
        }

        System.out.println("\n== Respostes registrades ==");
        for (SurveyResponse response : responses) {
            System.out.println("\nResposta ID: " + response.getId());
            System.out.println("Enquesta: " + response.getSurveyId());
            System.out.println("Usuari: " + response.getUserId());
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

    private void editSurvey() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hi ha enquestes registrades per editar.");
            return;
        }
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a editar");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            System.out.println("\n== Editant enquesta ==");
            System.out.println("Deixa en blanc per mantenir el valor actual.");

            String newTitle = promptOptional("Nou títol", survey.getTitle());
            String newDescription = promptOptional("Nova descripció", survey.getDescription());
            String newId = promptOptional("Nou ID (enter per mantenir)", survey.getId());
            int newK = promptInt("Nombre de clústers (k)", survey.getK());
            String newInitMethod = promptOptional("Mètode d'inicialització", survey.getInitMethod());
            String newDistance = promptOptional("Mètrica de distància", survey.getDistance());

            surveyController.editSurvey(surveyId, newId, newTitle, newDescription, newK, newInitMethod, newDistance);
            System.out.println("Enquesta editada correctament.");
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
        System.out.println("\nEnquestes disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de l'enquesta a eliminar");
        if (!promptBoolean("Estàs segur que vols eliminar l'enquesta i totes les seves respostes? (s/n)", false)) {
            System.out.println("Operació cancel·lada.");
            return;
        }
        try {
            surveyController.deleteSurvey(surveyId);
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

    private boolean ensureSession() {
        if (!userController.hasActiveSession()) {
            System.out.println("Has d'iniciar sessió per aquesta acció.");
            return false;
        }
        userController.refreshSession();
        return true;
    }

    private void pageBreak() {
        for (int i = 0; i < 40; i++) {
            System.out.println();
        }
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

    private double promptDouble(String label, double defaultValue) {
        while (true) {
            String value = prompt(label + " [" + defaultValue + "]");
            if (value.isEmpty()) return defaultValue;
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {
                System.out.println("Introdueix un número vàlid.");
            }
        }
    }

    private boolean promptBoolean(String label, boolean defaultValue) {
        while (true) {
            String hint = defaultValue ? "s" : "n";
            String value = prompt(label + " [" + hint + "]").toLowerCase();
            if (value.isEmpty()) return defaultValue;
            if (value.startsWith("s")) return true;
            if (value.startsWith("n")) return false;
            System.out.println("Respon amb 's' o 'n'.");
        }
    }
}
