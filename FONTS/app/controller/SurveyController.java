package app.controller;

import Exceptions.NotValidFileException;
import Exceptions.PersistenceException;
import Survey.InvalidSurveyException;
import Survey.LocalPersistence;
import Survey.Survey;
import importexport.SurveySerializer;
import importexport.TxtSurveySerializer;
import user.User;

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

    public Survey importSurvey(String path) throws NotValidFileException, PersistenceException {
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
