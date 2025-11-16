package Survey;

import Exceptions.PersistenceException;
import Response.SurveyResponse;
import java.util.*;

/**
 * Emmagatzematge en memòria utilitzat per la CLI per gestionar enquestes i respostes
 * sense dependre d'una base de dades externa.
 */
public class LocalPersistence {
    private Map<String, Survey> surveys;
    private Map<String, SurveyResponse> responses;

    public LocalPersistence() {
        this.surveys = new HashMap<>();
        this.responses = new HashMap<>();
    }

    /**
     * Desa o actualitza una enquesta al magatzem local.
     */
    public void saveSurvey(Survey s) throws PersistenceException {
        if (s == null || s.getId() == null || s.getId().isEmpty()) {
            throw new PersistenceException("Invalid survey data.");
        }
        surveys.put(s.getId(), s);
    }

    /**
     * Recupera una enquesta pel seu identificador.
     */
    public Survey loadSurvey(String id) throws PersistenceException {
        Survey survey = surveys.get(id);
        if (survey == null) {
            throw new PersistenceException("Survey not found.");
        }
        return survey;
    }

    /**
     * Retorna totes les enquestes creades per un usuari concret.
     */
    public List<Survey> listSurveysByUser(String userId) throws PersistenceException {
        if (userId == null || userId.isEmpty()) {
            throw new PersistenceException("Invalid user ID.");
        }
        List<Survey> result = new ArrayList<>();
        for (Survey survey : surveys.values()) {
            if (survey.getCreatedBy().equals(userId)) {
                result.add(survey);
            }
        }
        return result;
    }

    /**
     * Elimina una enquesta pel seu ID.
     */
    public void removeSurvey(String id) throws PersistenceException {
        if (!surveys.containsKey(id)) {
            throw new PersistenceException("Survey not found.");
        }
        surveys.remove(id);
    }

    /**
     * Desa o actualitza una resposta d'usuari.
     */
    public void saveResponse(SurveyResponse r) throws PersistenceException {
        if (r == null || r.getId() == null || r.getId().isEmpty()) {
            throw new PersistenceException("Invalid response data.");
        }
        responses.put(r.getId(), r);
    }

    /**
     * Llista les respostes associades a l'enquesta indicada.
     */
    public List<SurveyResponse> listResponsesBySurvey(String surveyId) throws PersistenceException {
        if (surveyId == null || surveyId.isEmpty()) {
            throw new PersistenceException("Invalid survey ID.");
        }
        List<SurveyResponse> result = new ArrayList<>();
        for (SurveyResponse response : responses.values()) {
            if (response.getSurveyId().equals(surveyId)) {
                result.add(response);
            }
        }
        return result;
    }

    /**
     * Elimina una resposta pel seu identificador.
     */
    public void removeResponse(String id) throws PersistenceException {
        if (!responses.containsKey(id)) {
            throw new PersistenceException("Response not found.");
        }
        responses.remove(id);
    }

    /**
     * Elimina totes les respostes associades a una enquesta.
     */
    public void removeResponsesBySurvey(String surveyId) {
        if (surveyId == null || surveyId.isEmpty()) {
            return;
        }
        responses.values().removeIf(response -> response.getSurveyId().equals(surveyId));
    }

    /**
     * @return col·lecció no modificable amb totes les enquestes emmagatzemades
     */
    public Collection<Survey> listAllSurveys() {
        return Collections.unmodifiableCollection(new ArrayList<>(surveys.values()));
    }

    /**
     * @return llista no modificable de totes les respostes guardades
     */
    public List<SurveyResponse> listAllResponses() {
        return Collections.unmodifiableList(new ArrayList<>(responses.values()));
    }
}
