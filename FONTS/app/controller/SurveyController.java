package app.controller;

import Exceptions.InvalidSurveyException;
import Exceptions.PersistenceException;
import Response.*;
import Survey.LocalPersistence;
import Survey.Survey;
import importexport.SurveySerializer;
import importexport.TxtSurveySerializer;
import app.DomainDriver;
import user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SurveyController {
    private final LocalPersistence persistence;
    private final SurveySerializer serializer;
    private final DomainDriver domainDriver;

    public SurveyController(DomainDriver domainDriver) {
        this(domainDriver, new LocalPersistence(), new TxtSurveySerializer());
    }

    public SurveyController(DomainDriver domainDriver, LocalPersistence persistence, SurveySerializer serializer) {
        this.domainDriver = domainDriver;
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

    /**
     * Parsea una cadena de respuestas según las preguntas de la encuesta.
     * Formato: "qid:val;qid:val1,val2;qid:val"
     */
    public List<Answer> parseAnswers(String answersStr, Survey survey) {
        List<Answer> answers = new ArrayList<>();
        if (answersStr == null || answersStr.isBlank()) return answers;
        
        String[] pairs = answersStr.split(";");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;
            
            String[] kv = trimmed.split(":", 2);
            if (kv.length < 2) continue;
            
            try {
                int questionId = Integer.parseInt(kv[0].trim());
                String value = kv[1].trim();
                
                Question q = survey.getQuestions().stream()
                    .filter(x -> x.getId() == questionId)
                    .findFirst()
                    .orElse(null);
                    
                if (q == null) continue;
                
                if (q instanceof OpenStringQuestion) {
                    answers.add(new TextAnswer(questionId, value));
                } else if (q instanceof OpenIntQuestion) {
                    int intValue = Integer.parseInt(value);
                    answers.add(new IntAnswer(questionId, intValue));
                } else if (q instanceof SingleChoiceQuestion) {
                    int optionId = Integer.parseInt(value);
                    answers.add(new SingleChoiceAnswer(questionId, optionId));
                } else if (q instanceof MultipleChoiceQuestion) {
                    String normalized = value.replace('|', ',');
                    answers.add(new MultipleChoiceAnswer(questionId, normalized));
                }
            } catch (Exception ignored) {
                // Skip invalid answers
            }
        }
        return answers;
    }
}
