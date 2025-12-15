package app.controller;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.Answer;
import Response.SurveyResponse;
import Survey.LocalPersistence;
import Survey.Survey;
import user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ResponseController {
    private final LocalPersistence persistence;

    public ResponseController(LocalPersistence persistence) {
        this.persistence = persistence;
    }

    public SurveyResponse buildResponse(Survey survey, User respondent, List<Answer> answers)
        throws NullArgumentException, InvalidArgumentException {
        return new SurveyResponse(
            UUID.randomUUID().toString(),
            survey.getId(),
            respondent.getId(),
            LocalDateTime.now().toString(),
            answers
        );
    }

    public void saveResponse(SurveyResponse response) throws PersistenceException {
        persistence.saveResponse(response);
    }

    public List<SurveyResponse> listResponses(String surveyId) throws PersistenceException {
        return persistence.listResponsesBySurvey(surveyId);
    }

    public List<SurveyResponse> listAllResponses() {
        return persistence.listAllResponses();
    }

    public void removeResponsesBySurvey(String surveyId) throws PersistenceException {
        persistence.removeResponsesBySurvey(surveyId);
    }

    public List<SurveyResponse> listResponsesByUser(String userId) throws PersistenceException {
        return persistence.listResponsesByUser(userId);
    }

    public void deleteResponse(String responseId) throws PersistenceException {
        persistence.removeResponse(responseId);
    }

    public SurveyResponse loadResponse(String responseId) throws PersistenceException {
        return persistence.loadResponse(responseId);
    }

    public void updateResponse(SurveyResponse original, List<Answer> answers) throws PersistenceException {
        SurveyResponse updated = new SurveyResponse(
            original.getId(),
            original.getSurveyId(),
            original.getUserId(),
            LocalDateTime.now().toString(),
            answers
        );
        persistence.saveResponse(updated);
    }
}
