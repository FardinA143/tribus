package Enquesta;

import Response.SurveyResponse;
import java.util.*;

public class LocalPersistence {
    private Map<String, Survey> surveys;
    private Map<String, SurveyResponse> responses;

    public LocalPersistence() {
        this.surveys = new HashMap<>();
        this.responses = new HashMap<>();
    }

    // Save a survey
    public void saveSurvey(Survey s) {
        surveys.put(s.getId(), s);
    }

    // Load a survey by ID
    public Survey loadSurvey(String id) {
        return surveys.get(id);
    }

    // List surveys by user
    public List<Survey> listSurveysByUser(String userId) {
        List<Survey> result = new ArrayList<>();
        for (Survey survey : surveys.values()) {
            if (survey.getCreatedBy().equals(userId)) {
                result.add(survey);
            }
        }
        return result;
    }

    // Remove a survey by ID
    public void removeSurvey(String id) {
        surveys.remove(id);
    }

    // Save a response
    public void saveResponse(SurveyResponse r) {
        responses.put(r.getId(), r);
    }

    // List responses by survey ID
    public List<SurveyResponse> listResponsesBySurvey(String surveyId) {
        List<SurveyResponse> result = new ArrayList<>();
        for (SurveyResponse response : responses.values()) {
            if (response.getSurveyId().equals(surveyId)) {
                result.add(response);
            }
        }
        return result;
    }

    // Remove a response by ID
    public void removeResponse(String id) {
        responses.remove(id);
    }
}
