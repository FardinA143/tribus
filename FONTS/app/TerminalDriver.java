package app;

import Exceptions.InvalidArgumentException;
import Exceptions.InvalidQuestionException;
import Exceptions.InvalidSurveyException;
import Exceptions.NullArgumentException;
import Exceptions.NotValidFileException;
import Exceptions.PersistenceException;
import Response.*;
import Survey.*;
import app.controller.*;
import importexport.*;
import user.*;

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
        System.out.println("  Sistema de Encuestas - Terminal UI");
        System.out.println("========================================\n");

        boolean exit = false;
        while (!exit) {
            pageBreak();
            showMenu();
            String choice = prompt("Seleccione una opción");
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
                case "0" -> exit = true;
                default -> System.out.println("Opción no válida. Intente nuevamente.");
            }
            if (!exit) {
                prompt("Presione Enter para continuar");
            }
        }

        System.out.println("¡Hasta pronto!");
    }

    private void showMenu() {
        System.out.println("\n-------------------");
        System.out.println("1) Registrar usuario");
        System.out.println("2) Iniciar sesión");
        System.out.println("3) Cerrar sesión");
        System.out.println("4) Crear encuesta");
        System.out.println("5) Responder encuesta");
        System.out.println("6) Importar encuesta desde archivo");
        System.out.println("7) Exportar encuesta a archivo");
        System.out.println("8) Importar respuestas desde archivo");
        System.out.println("9) Exportar respuestas a archivo");
        System.out.println("10) Realizar análisis");
        System.out.println("0) Salir");
    }

    private void registerUser() {
        System.out.println("\n== Registro de usuario ==");
        String idInput = promptOptional("ID (enter para autogenerar)", UUID.randomUUID().toString());
        String displayName = promptNonEmpty("Nombre para mostrar");
        String username = promptNonEmpty("Nombre de usuario");
        String password = promptNonEmpty("Contraseña");

        User user = userController.register(idInput, displayName, username, password);
        if (user != null) {
            System.out.println("Usuario registrado con éxito: " + user.getDisplayName());
        } else {
            System.out.println("No se pudo registrar al usuario.");
        }
    }

    private void login() {
        if (userController.hasActiveSession()) {
            System.out.println("Ya existe una sesión activa para " + userController.getCurrentSession().getUser().getDisplayName());
            return;
        }
        System.out.println("\n== Inicio de sesión ==");
        String username = promptNonEmpty("Nombre de usuario");
        String password = promptNonEmpty("Contraseña");
        Sesion sesion = userController.login(username, password);
        if (sesion != null) {
            System.out.println("Sesión iniciada. Bienvenido, " + sesion.getUser().getDisplayName() + "!");
        } else {
            System.out.println("Credenciales inválidas o usuario inexistente.");
        }
    }

    private void logout() {
        if (!userController.hasActiveSession()) {
            System.out.println("No hay ninguna sesión activa.");
            return;
        }
        userController.logout();
        System.out.println("Sesión cerrada correctamente.");
    }

    private void createSurvey() {
        if (!ensureSession()) return;
        System.out.println("\n== Crear encuesta ==");
        try {
            String surveyId = promptOptional("ID de encuesta (enter para autogenerar)", UUID.randomUUID().toString());
            String title = promptNonEmpty("Título");
            String description = promptOptional("Descripción", "");
            int k = promptInt("Número de clusters (k)", 2);
            String initMethod = promptOptional("Método de inicialización", "kmeans++");
            String distance = promptOptional("Métrica de distancia", "euclidean");
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
                if (!promptBoolean("¿Desea agregar una pregunta? (s/n)", position == 1)) break;
                Question question = buildQuestion(position);
                if (question != null) {
                    survey.addQuestion(question);
                    position++;
                }
            }

            surveyController.saveSurvey(survey);
            System.out.println("Encuesta guardada con " + survey.getQuestions().size() + " preguntas.");
        } catch (InvalidSurveyException | InvalidQuestionException | PersistenceException e) {
            System.out.println("Error al crear la encuesta: " + e.getMessage());
        }
    }

    private Question buildQuestion(int position) {
        System.out.println("\n-- Configuración de pregunta #" + position + " --");
        int id = promptInt("ID interno", position);
        String text = promptNonEmpty("Texto de la pregunta");
        boolean required = promptBoolean("¿Es obligatoria? (s/n)", true);
        double weight = promptDouble("Peso (default 1.0)", 1.0);

        while (true) {
            String type = prompt("Tipo [texto|numero|single|multi]").toLowerCase();
            switch (type) {
                case "texto" -> {
                    int maxLength = promptInt("Longitud máxima", 280);
                    return new OpenStringQuestion(id, text, required, position, weight, maxLength);
                }
                case "numero" -> {
                    int min = promptInt("Valor mínimo", 0);
                    int max = promptInt("Valor máximo", min + 10);
                    if (max < min) {
                        System.out.println("El máximo debe ser mayor o igual que el mínimo.");
                        continue;
                    }
                    return new OpenIntQuestion(id, text, required, position, weight, min, max);
                }
                case "single" -> {
                    SingleChoiceQuestion sc = new SingleChoiceQuestion(id, text, required, position, weight);
                    addChoiceOptions(sc.getOptions(), "opción", 2);
                    return sc;
                }
                case "multi" -> {
                    int minChoices = promptInt("Selecciones mínimas", 1);
                    int maxChoices = promptInt("Selecciones máximas", Math.max(minChoices, 2));
                    if (maxChoices < minChoices) {
                        System.out.println("El máximo debe ser mayor o igual que el mínimo.");
                        continue;
                    }
                    MultipleChoiceQuestion mc = new MultipleChoiceQuestion(id, text, required, position, weight, minChoices, maxChoices);
                    addChoiceOptions(mc.getOptions(), "opción", maxChoices);
                    return mc;
                }
                default -> System.out.println("Tipo no reconocido. Intente nuevamente.");
            }
        }
    }

    private void addChoiceOptions(List<ChoiceOption> options, String label, int minimum) {
        int count = Math.max(minimum, promptInt("Número de opciones", minimum));
        for (int i = 0; i < count; i++) {
            int optId = promptInt("  ID de " + label + " " + (i + 1), i + 1);
            String optLabel = promptNonEmpty("  Texto de " + label + " " + (i + 1));
            options.add(new ChoiceOption(optId, optLabel));
        }
    }

    private void importSurvey() {
        if (!ensureSession()) return;
        System.out.println("\n== Importar encuesta ==");
        String path = promptNonEmpty("Ruta del archivo .txt");
        try {
            Survey survey = surveyController.importSurvey(path);
            System.out.println("Encuesta importada con ID " + survey.getId());
        } catch (NotValidFileException | PersistenceException e) {
            System.out.println("No se pudo importar la encuesta: " + e.getMessage());
        }
    }

    private void importResponsesFromFile() {
        if (!ensureSession()) return;
        System.out.println("\n== Importar respuestas ==");
        String path = promptNonEmpty("Ruta del archivo .txt");
        try {
            SurveyResponse response = responseSerializer.fromFile(path);
            surveyController.loadSurvey(response.getSurveyId());
            responseController.saveResponse(response);
            System.out.println("Respuesta importada con ID " + response.getId());
        } catch (NotValidFileException e) {
            System.out.println("El archivo no tiene un formato válido: " + e.getMessage());
        } catch (PersistenceException e) {
            System.out.println("No se pudo guardar la respuesta importada: " + e.getMessage());
        }
    }

    private void exportSurveyToFile() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hay encuestas registradas para exportar.");
            return;
        }
        System.out.println("\nEncuestas disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de la encuesta a exportar");
        String path = promptNonEmpty("Ruta destino del archivo .txt");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            surveySerializer.toFile(survey, path);
            System.out.println("Encuesta exportada correctamente a " + path);
        } catch (PersistenceException e) {
            System.out.println("No se pudo exportar la encuesta: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error al escribir el archivo: " + e.getMessage());
        }
    }

    private void exportResponsesToFile() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hay encuestas registradas.");
            return;
        }
        System.out.println("\nEncuestas disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de la encuesta cuyas respuestas desea exportar");
        try {
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            if (responses.isEmpty()) {
                System.out.println("La encuesta no tiene respuestas registradas.");
                return;
            }
            String path = promptNonEmpty("Ruta destino del archivo .txt");
            responseSerializer.toFile(responses, path);
            System.out.println("Respuestas exportadas correctamente a " + path);
        } catch (PersistenceException e) {
            System.out.println("No se pudieron obtener las respuestas: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error al escribir el archivo: " + e.getMessage());
        }
    }

    private void answerSurvey() {
        if (!ensureSession()) return;
        Collection<Survey> surveys = surveyController.listSurveys();
        if (surveys.isEmpty()) {
            System.out.println("No hay encuestas disponibles.");
            return;
        }
        System.out.println("\nEncuestas disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de la encuesta a responder");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            if (survey.getQuestions().isEmpty()) {
                System.out.println("La encuesta no tiene preguntas configuradas.");
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
                System.out.println("No se generaron respuestas. Operación cancelada.");
                return;
            }
            User respondent = userController.requireActiveUser();
            SurveyResponse response = responseController.buildResponse(survey, respondent, answers);
            responseController.saveResponse(response);
            System.out.println("Respuesta registrada con ID " + response.getId());
        } catch (PersistenceException | NullArgumentException | InvalidArgumentException e) {
            System.out.println("Error al guardar la respuesta: " + e.getMessage());
        }
    }

    private Answer askAnswer(Question question) throws NullArgumentException, InvalidArgumentException {
        System.out.println("\nPregunta: " + question.getText());
        System.out.println("Tipo: " + question.getClass().getSimpleName());
        if (!question.isRequired()) {
            System.out.println("(Puede dejar en blanco para omitir)");
        }

        if (question instanceof OpenStringQuestion) {
            while (true) {
                String value = prompt("Respuesta (texto)");
                if (value.isEmpty() && !question.isRequired()) return null;
                if (value.isEmpty()) {
                    System.out.println("La respuesta es obligatoria.");
                    continue;
                }
                return new TextAnswer(question.getId(), value);
            }
        }
        if (question instanceof OpenIntQuestion intQuestion) {
            while (true) {
                String value = prompt("Respuesta numérica (" + intQuestion.getMin() + " - " + intQuestion.getMax() + ")");
                if (value.isEmpty() && !question.isRequired()) return null;
                try {
                    int parsed = Integer.parseInt(value);
                    if (parsed < intQuestion.getMin() || parsed > intQuestion.getMax()) {
                        System.out.println("Valor fuera de rango.");
                        continue;
                    }
                    return new IntAnswer(question.getId(), parsed);
                } catch (NumberFormatException ex) {
                    System.out.println("Ingrese un número válido.");
                }
            }
        }
        if (question instanceof SingleChoiceQuestion singleQuestion) {
            showOptions(singleQuestion.getOptions());
            while (true) {
                String value = prompt("ID de la opción seleccionada");
                if (value.isEmpty() && !question.isRequired()) return null;
                try {
                    int optionId = Integer.parseInt(value);
                    boolean exists = singleQuestion.getOptions().stream().anyMatch(o -> o.getId() == optionId);
                    if (!exists) {
                        System.out.println("Opción inexistente.");
                        continue;
                    }
                    return new SingleChoiceAnswer(question.getId(), optionId);
                } catch (NumberFormatException ex) {
                    System.out.println("Ingrese un número válido.");
                }
            }
        }
        if (question instanceof MultipleChoiceQuestion multipleQuestion) {
            showOptions(multipleQuestion.getOptions());
            System.out.println("Debe seleccionar entre " + multipleQuestion.getMinChoices() + " y " + multipleQuestion.getMaxChoices() + " opciones (IDs separados por comas).");
            while (true) {
                String value = prompt("Selección");
                if (value.isEmpty() && !question.isRequired()) return null;
                List<Integer> selections = parseSelection(value);
                if (selections.size() < multipleQuestion.getMinChoices() || selections.size() > multipleQuestion.getMaxChoices()) {
                    System.out.println("Cantidad inválida de opciones.");
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
            System.out.println("No hay encuestas registradas.");
            return;
        }
        System.out.println("\nEncuestas disponibles:");
        surveys.forEach(s -> System.out.println(" - " + s.getId() + " :: " + s.getTitle()));
        String surveyId = promptNonEmpty("ID de la encuesta a analizar");
        try {
            Survey survey = surveyController.loadSurvey(surveyId);
            List<SurveyResponse> responses = responseController.listResponses(surveyId);
            if (responses.size() < 2) {
                System.out.println("Se requieren al menos 2 respuestas para analizar.");
                return;
            }

            AnalyticsResult result = analyticsController.analyzeSurvey(survey, responses);

            System.out.println("\n== Resultado del análisis ==");
            System.out.println("Encuesta: " + survey.getTitle());
            System.out.println("Clusteres (k): " + result.getClusters());
            result.getClusterCounts().forEach((cluster, total) ->
                System.out.println("  Cluster " + cluster + ": " + total + " respuestas")
            );
            System.out.printf("Inercia (SSE): %.4f%n", result.getInertia());
            System.out.printf("Silhouette promedio: %.4f%n", result.getAverageSilhouette());
        } catch (PersistenceException e) {
            System.out.println("No se pudo ejecutar el análisis: " + e.getMessage());
        }
    }

    private boolean ensureSession() {
        if (!userController.hasActiveSession()) {
            System.out.println("Debe iniciar sesión para esta acción.");
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
        System.out.println("Opciones disponibles:");
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
            System.out.println("El valor no puede estar vacío.");
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
                System.out.println("Ingrese un número entero válido.");
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
                System.out.println("Ingrese un número válido.");
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
            System.out.println("Responda con 's' o 'n'.");
        }
    }
}
