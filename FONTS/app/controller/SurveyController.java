package app.controller;

import Exceptions.InvalidSurveyException;
import Exceptions.PersistenceException;
import Survey.LocalPersistence;
import Survey.Survey;
import importexport.SurveySerializer;
import importexport.TxtSurveySerializer;
import user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;

public class SurveyController {
    private final LocalPersistence persistence;
    private final SurveySerializer serializer;

    public SurveyController() {
        this(new LocalPersistence(), new TxtSurveySerializer());
    }

    public SurveyController(LocalPersistence persistence, SurveySerializer serializer) {
        this.persistence = persistence;
        this.serializer = serializer;
    }

    public Survey createSurvey(
        String id,
        String title,
        String description,
        User owner,
        int k,
        String initMethod,
        String distance
    ) throws InvalidSurveyException {
        String timestamp = LocalDateTime.now().toString();
        return new Survey(
            id,
            title,
            description,
            owner.getId(),
            k,
            initMethod,
            distance,
            timestamp,
            timestamp
        );
    }

    public void saveSurvey(Survey survey) throws PersistenceException {
        persistence.saveSurvey(survey);
    }

    public void deleteSurvey(String id) throws PersistenceException {
        persistence.removeSurvey(id);
        persistence.removeResponsesBySurvey(id);
    }

    public Survey editSurvey(String originalId,
                             String newId,
                             String title,
                             String description,
                             int k,
                             String initMethod,
                             String distance) throws PersistenceException, InvalidSurveyException {
        Survey survey = persistence.loadSurvey(originalId);
        String effectiveId = (newId == null || newId.isBlank()) ? originalId : newId;

        if (!originalId.equals(effectiveId)) {
            persistence.removeSurvey(originalId);
            survey.setId(effectiveId);
        }

        survey.setTitle(title);
        survey.setDescription(description);
        survey.setK(k);
        survey.setInitMethod(initMethod);
        survey.setDistance(distance);
        survey.setUpdatedAt(LocalDateTime.now().toString());

        persistence.saveSurvey(survey);
        return survey;
    }

    public Survey importSurvey(String path) throws IOException, PersistenceException {
        Survey survey = serializer.fromFile(path);
        persistence.saveSurvey(survey);
        return survey;
    }

    public Collection<Survey> listSurveys() {
        return persistence.listAllSurveys();
    }

    public Survey loadSurvey(String id) throws PersistenceException {
        return persistence.loadSurvey(id);
    }

    public SurveySerializer getSerializer() {
        return serializer;
    }

    public LocalPersistence getPersistence() {
        return persistence;
    }
}
