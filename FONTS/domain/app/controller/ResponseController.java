package app.controller;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.Answer;
import Response.SurveyResponse;
import Survey.Survey;
import app.DomainDriver;
import persistence.PersistenceDriver;
import user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ResponseController {
    private final PersistenceDriver persistenceDriver;
    @SuppressWarnings("unused")
    private final DomainDriver domainDriver;

    public ResponseController(DomainDriver domainDriver) {
        this(domainDriver, new PersistenceDriver());
    }

    public ResponseController(DomainDriver domainDriver, PersistenceDriver persistenceDriver) {
        this.domainDriver = domainDriver;
        this.persistenceDriver = persistenceDriver;
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
        try {
            persistenceDriver.appendResponse(response.getSurveyId(), response);
        } catch (Exceptions.NullArgumentException | Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public List<SurveyResponse> listResponses(String surveyId) throws PersistenceException {
        try {
            return persistenceDriver.loadAllResponses(surveyId);
        } catch (Exceptions.NullArgumentException | Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public List<SurveyResponse> listAllResponses() {
        try {
            java.util.ArrayList<SurveyResponse> all = new java.util.ArrayList<>();
            for (Survey s : persistenceDriver.loadAllSurveys()) {
                all.addAll(persistenceDriver.loadAllResponses(s.getId()));
            }
            return all;
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    public void removeResponsesBySurvey(String surveyId) throws PersistenceException {
        try {
            persistenceDriver.deleteResponses(surveyId);
        } catch (Exceptions.NullArgumentException | Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public List<SurveyResponse> listResponsesByUser(String userId) throws PersistenceException {
        // No index on disk; scan persisted responses.
        try {
            java.util.ArrayList<SurveyResponse> result = new java.util.ArrayList<>();
            for (Survey s : persistenceDriver.loadAllSurveys()) {
                try {
                    for (SurveyResponse r : persistenceDriver.loadAllResponses(s.getId())) {
                        if (userId != null && userId.equals(r.getUserId())) {
                            result.add(r);
                        }
                    }
                } catch (Exceptions.NullArgumentException ignored) {
                    // Should not happen; survey id is not null.
                }
            }
            return result;
        } catch (Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public void deleteResponse(String responseId) throws PersistenceException {
        SurveyResponse found = loadResponse(responseId);
        try {
            List<SurveyResponse> current = persistenceDriver.loadAllResponses(found.getSurveyId());
            current.removeIf(r -> responseId.equals(r.getId()));
            persistenceDriver.saveAllResponses(found.getSurveyId(), current);
        } catch (Exceptions.NullArgumentException | Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public SurveyResponse loadResponse(String responseId) throws PersistenceException {
        if (responseId == null || responseId.isBlank()) {
            throw new PersistenceException("responseId inválido");
        }
        try {
            for (Survey s : persistenceDriver.loadAllSurveys()) {
                try {
                    for (SurveyResponse r : persistenceDriver.loadAllResponses(s.getId())) {
                        if (responseId.equals(r.getId())) return r;
                    }
                } catch (Exceptions.NullArgumentException ignored) {
                    // Should not happen; survey id is not null.
                }
            }
            throw new PersistenceException("Response not found.");
        } catch (Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public void updateResponse(SurveyResponse original, List<Answer> answers) throws PersistenceException {
        SurveyResponse updated = new SurveyResponse(
            original.getId(),
            original.getSurveyId(),
            original.getUserId(),
            LocalDateTime.now().toString(),
            answers
        );

        try {
            List<SurveyResponse> current = persistenceDriver.loadAllResponses(original.getSurveyId());
            boolean replaced = false;
            for (int i = 0; i < current.size(); i++) {
                if (original.getId().equals(current.get(i).getId())) {
                    current.set(i, updated);
                    replaced = true;
                    break;
                }
            }
            if (!replaced) current.add(updated);
            persistenceDriver.saveAllResponses(original.getSurveyId(), current);
        } catch (Exceptions.NullArgumentException | Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }
}
