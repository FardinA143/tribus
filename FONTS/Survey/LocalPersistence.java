package Survey;

import Response.SurveyResponse;
import Exceptions.PersistenceException;
import java.util.*;

public class LocalPersistence {
    private Map<String, Survey> surveys;
    private Map<String, SurveyResponse> responses;

    public LocalPersistence() {
        this.surveys = new HashMap<>();
        this.responses = new HashMap<>();
    }

    // Save a survey
    public void saveSurvey(Survey s) throws PersistenceException {
        if (s == null || s.getId() == null || s.getId().isEmpty()) {
            throw new PersistenceException("Invalid survey data.");
        }
        surveys.put(s.getId(), s);
    }

    // Load a survey by ID
    public Survey loadSurvey(String id) throws PersistenceException {
        Survey survey = surveys.get(id);
        if (survey == null) {
            throw new PersistenceException("Survey not found.");
        }
        return survey;
    }

    // List surveys by user
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

    // Remove a survey by ID
    public void removeSurvey(String id) throws PersistenceException {
        if (!surveys.containsKey(id)) {
            throw new PersistenceException("Survey not found.");
        }
        surveys.remove(id);
    }

    // Save a response
    public void saveResponse(SurveyResponse r) throws PersistenceException {
        if (r == null || r.getId() == null || r.getId().isEmpty()) {
            throw new PersistenceException("Invalid response data.");
        }
        responses.put(r.getId(), r);
    }

    // List responses by survey ID
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

    // Remove a response by ID
    public void removeResponse(String id) throws PersistenceException {
        if (!responses.containsKey(id)) {
            throw new PersistenceException("Response not found.");
        }
        responses.remove(id);
    }

    // Expose all surveys (read-only) to clients such as the CLI driver
    public Collection<Survey> listAllSurveys() {
        return Collections.unmodifiableCollection(new ArrayList<>(surveys.values()));
    }
}
