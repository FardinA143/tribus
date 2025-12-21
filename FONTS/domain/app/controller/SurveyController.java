package app.controller;

import Exceptions.InvalidSurveyException;
import Exceptions.PersistenceException;
import Response.Answer;
import Response.IntAnswer;
import Response.MultipleChoiceAnswer;
import Response.SingleChoiceAnswer;
import Response.TextAnswer;
import Survey.Survey;
import Survey.Question;
import Survey.OpenStringQuestion;
import Survey.OpenIntQuestion;
import Survey.SingleChoiceQuestion;
import Survey.MultipleChoiceQuestion;
import importexport.SurveySerializer;
import importexport.TxtSurveySerializer;
import persistence.PersistenceDriver;
import user.User;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SurveyController {
    private final PersistenceDriver persistence;
    private final SurveySerializer serializer;

    public SurveyController() {
        this(new PersistenceDriver(), new TxtSurveySerializer());
    }

    public SurveyController(PersistenceDriver persistence, SurveySerializer serializer) {
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

        if (survey == null || survey.getQuestions() == null) {
            throw new PersistenceException("L'enquesta ha de tenir com a mínim una pregunta obligatòria");
        }

        boolean hasRequired = false;
        for (Question q : survey.getQuestions()) {
            if (q != null && q.isRequired()) {
                hasRequired = true;
                break;
            }
        }
        if (!hasRequired) {
            throw new PersistenceException("L'enquesta ha de tenir com a mínim una pregunta obligatòria");
        }

        try {
            persistence.saveSurvey(survey);
        } catch (Exceptions.NullArgumentException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public void deleteSurvey(String id) throws PersistenceException {
        try {
            persistence.deleteSurvey(id);
        } catch (Exceptions.NullArgumentException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public Survey editSurvey(String originalId,
                             String newId,
                             String title,
                             String description,
                             int k,
                             String initMethod,
                             String distance) throws PersistenceException, InvalidSurveyException {
        Survey survey;
        try {
            survey = persistence.loadSurvey(originalId);
        } catch (Exceptions.NullArgumentException e) {
            throw new PersistenceException(e.getMessage());
        }
        String effectiveId = (newId == null || newId.isBlank()) ? originalId : newId;

        if (!originalId.equals(effectiveId)) {
            try {
                persistence.deleteSurvey(originalId);
            } catch (Exceptions.NullArgumentException e) {
                throw new PersistenceException(e.getMessage());
            }
            survey.setId(effectiveId);
        }

        survey.setTitle(title);
        survey.setDescription(description);
        survey.setK(k);
        survey.setInitMethod(initMethod);
        survey.setDistance(distance);
        survey.setUpdatedAt(LocalDateTime.now().toString());

        try {
            persistence.saveSurvey(survey);
        } catch (Exceptions.NullArgumentException e) {
            throw new PersistenceException(e.getMessage());
        }
        return survey;
    }

    public Survey importSurvey(String path) throws IOException, PersistenceException {
        Survey survey = serializer.fromFile(path);

        // Reutilitza la validació i el desat estandard.
        saveSurvey(survey);
        return survey;
    }

    public Collection<Survey> listSurveys() throws PersistenceException {
        try {
            return persistence.loadAllSurveys();
        } catch (Exceptions.PersistenceException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public Survey loadSurvey(String id) throws PersistenceException {

        try {
            return persistence.loadSurvey(id);
        } catch (Exceptions.NullArgumentException e) {
            throw new PersistenceException(e.getMessage());
        }
    }

    public SurveySerializer getSerializer() {
        return serializer;
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
