package Survey;

import java.util.*;
import Exceptions.*;

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

    public void addQuestion(Question question) throws InvalidQuestionException {
        if (question == null) {
            throw new InvalidQuestionException("Question cannot be null.");
        }
        this.questions.add(question);
    }

    public void importQuestions(List<Question> questions) throws InvalidQuestionException {
        if (questions == null) {
            throw new InvalidQuestionException("Questions list cannot be null.");
        }
        for (Question q : questions) {
            if (q == null) {
                throw new InvalidQuestionException("Question in the list cannot be null.");
            }
            this.questions.add(q);
        }
    }

    public void deleteQuestion(int questionId) throws QuestionNotFoundException {
        boolean removed = questions.removeIf(q -> q.getId() == questionId);
        if (!removed) {
            throw new QuestionNotFoundException("Question with ID " + questionId + " not found.");
        }
    }
    // Getters and setters
    public String getId() {
        return id;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) { // used to modify too
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedBy() {
        return createdBy;
    } // not really necessary

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public String getInitMethod() {
        return initMethod;
    }

    public void setInitMethod(String initMethod) {
        this.initMethod = initMethod;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Question> getQuestions() {
        return questions;
    }

}
