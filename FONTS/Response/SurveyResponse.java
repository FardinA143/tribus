// java
package Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
  SurveyResponse: agrupa les respostes d'una submissió.
  emptyAnswer = true  -> NO hi ha respostes (llista buida)
  emptyAnswer = false -> Hi ha almenys una resposta
*/
public class SurveyResponse {
    private final String id;
    private final String surveyId;
    private final String userId;

    private String submittedAt;

    private final List<Answer> answers;

    // Constructor principal: emptyAnswer calculat a partir de la llista d'answers
    public SurveyResponse(String id, String surveyId, String userId, String submittedAt, List<Answer> answers) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id no pot ser null o buit");
        }
        if (surveyId == null || surveyId.isEmpty()) {
            throw new IllegalArgumentException("surveyId no pot ser null o buit");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId no pot ser null o buit");
        }

        this.id = id;
        this.surveyId = surveyId;
        this.userId = userId;
        this.submittedAt = submittedAt;

        this.answers = new ArrayList<>();
        if (answers != null && !answers.isEmpty()) {
            this.answers.addAll(answers);
            
        }
    }

    // Constructor alternatiu senzill (sense answers)
    public SurveyResponse(String id, String surveyId, String userId) {
        this(id, surveyId, userId, null, null);
    }

    // Getters
    public String getId() { return id; }
    public String getSurveyId() { return surveyId; }
    public String getUserId() { return userId; }
    public String getSubmittedAt() { return submittedAt; }
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }

    // Afegir resposta: actualitza emptyAnswer
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    // Eliminar resposta per objecte
    public void removeAnswer(Answer answer) {
        this.answers.remove(answer);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyResponse)) return false;
        SurveyResponse that = (SurveyResponse) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}