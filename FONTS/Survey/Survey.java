package Survey;

import java.util.*;
import Exceptions.*;

/**
 * Agregat principal que conté les metadades i el llistat ordenat de preguntes
 * que defineixen una enquesta.
 */
public class Survey {
    private String id; // unique survey_id, formed by
    private String title;
    private String description;
    private String createdBy;
    private int k;
    private String initMethod;
    private String distance;
    private String createdAt;
    private String updatedAt;
    private List<Question> questions;

    /**
     * Crea una enquesta amb la seva configuració de clustering.
     *
     * @param id          identificador únic
     * @param title       títol llegible
     * @param description descripció opcional
     * @param createdBy   autor o propietari
     * @param k           nombre de clústers sol·licitats
     * @param initMethod  estratègia d'inicialització
     * @param distance    mètrica de distància
     * @param createdAt   data de creació (ISO)
     * @param updatedAt   data de darrera actualització (ISO)
     * @throws InvalidSurveyException si falten l'id o el títol
     */
    public Survey(String id, String title, String description, String createdBy, int k, String initMethod, String distance, String createdAt, String updatedAt) throws InvalidSurveyException {
        if (id == null || id.isEmpty() || title == null || title.isEmpty()) {
            throw new InvalidSurveyException("Survey ID and title cannot be null or empty.");
        }
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdBy = createdBy;
        this.k = k;
        this.initMethod = initMethod;
        this.distance = distance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.questions = new ArrayList<>();
    }

    /**
     * Afegeix una pregunta a l'enquesta.
     *
        * @param question pregunta a afegir.
     * @throws InvalidQuestionException si el paràmetre és nul
     */
    public void addQuestion(Question question) throws InvalidQuestionException {
        if (question == null) {
            throw new InvalidQuestionException("no pot ser nul·la");
        }
        this.questions.add(question);
    }

    /**
     * Importa un conjunt de preguntes mantenint-ne l'ordre.
     *
     * @param questions col·lecció de preguntes a afegir
     * @throws InvalidQuestionException si la llista o algun element és nul
     */
    public void importQuestions(List<Question> questions) throws InvalidQuestionException {
        if (questions == null) {
            throw new InvalidQuestionException("la llista de preguntes no pot ser nul·la");
        }
        for (Question q : questions) {
            if (q == null) {
                throw new InvalidQuestionException("alguna pregunta de la llista és nul·la");
            }
            this.questions.add(q);
        }
    }

    /**
     * Elimina la pregunta amb l'identificador indicat.
     *
        * @param questionId identificador de la pregunta a eliminar.
     * @throws QuestionNotFoundException si no existeix
     */
    public void deleteQuestion(int questionId) throws QuestionNotFoundException {
        boolean removed = questions.removeIf(q -> q.getId() == questionId);
        if (!removed) {
            throw new QuestionNotFoundException(questionId);
        }
    }
    // Getters and setters

    /**
     * Recupera l'identificador únic de l'enquesta.
     *
     * @return l'identificador de l'enquesta
     */
    public String getId() {
        return id;
    }
    
    /**
     * Actualitza l'identificador únic de l'enquesta.
     *
     * @param id nou identificador
     * @throws InvalidSurveyException si és nul o buit
     */
    public void setId(String id) throws InvalidSurveyException {
        if (id == null || id.isEmpty()) {
            throw new InvalidSurveyException("Survey ID cannot be null or empty.");
        }
        this.id = id;
    }

    /**
     * Mostra el títol actual configurat.
     *
     * @return el títol actual
     */
    public String getTitle() {
        return title;
    }

    /**
     * Actualitza el títol mostrat de l'enquesta.
     *
     * @param title nou títol llegible.
     */
    public void setTitle(String title) { // used to modify too
        this.title = title;
    }

    /**
     * Retorna la descripció informativa.
     *
     * @return la descripció
     */
    public String getDescription() {
        return description;
    }

    /**
     * Defineix una nova descripció (pot ser buida).
     *
     * @param description text descriptiu opcional.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Indica qui ha creat l'enquesta.
     *
     * @return l'identificador de l'autor
     */
    public String getCreatedBy() {
        return createdBy;
    } // not really necessary

    /**
     * Mostra el nombre de clústers configurats.
     *
     * @return el nombre de clústers configurat
     */
    public int getK() {
        return k;
    }

    /**
     * Estableix un nou valor de k.
     *
     * @param k nombre de clústers.
     */
    public void setK(int k) {
        this.k = k;
    }

    /**
     * Indica la metodologia d'inicialització actual.
     *
     * @return la metodologia d'inicialització
     */
    public String getInitMethod() {
        return initMethod;
    }

    /**
     * Canvia la metodologia d'inicialització utilitzada pel clustering.
     *
     * @param initMethod nova metodologia.
     */
    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    /**
     * Mostra la mètrica de distància triada.
     *
     * @return la mètrica de distància configurada
     */
    public String getDistance() {
        return distance;
    }

    /**
     * Defineix una nova mètrica de distància.
     *
     * @param distance identificador de la mètrica.
     */
    public void setDistance(String distance) {
        this.distance = distance;
    }

    /**
     * Consulta la data de creació en format ISO.
     *
     * @return la data de creació (ISO)
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Actualitza la marca temporal de creació (per imports/proves).
     *
     * @param createdAt nova data de creació.
     */
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Retorna la darrera data d'actualització.
     *
     * @return la data de darrera actualització
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Estableix la data de darrera modificació.
     *
     * @param updatedAt nova data ISO.
     */
    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Obté la col·lecció ordenada de preguntes.
     *
     * @return la llista mutable de preguntes
     */
    public List<Question> getQuestions() {
        return questions;
    }

}
