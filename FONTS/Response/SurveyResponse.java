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
    private boolean emptyAnswer;
    private String submittedAt;

    private final List<Answer> answers;

    // Constructor principal: emptyAnswer calculat a partir de la llista d'answers
    public SurveyResponse(String id, String surveyId, String userId, String submittedAt, List<Answer> answers) {
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id no pot ser null o buit");
        if (surveyId == null || surveyId.isEmpty()) throw new IllegalArgumentException("surveyId no pot ser null o buit");
        if (userId == null || userId.isEmpty()) throw new IllegalArgumentException("userId no pot ser null o buit");

        this.id = id;
        this.surveyId = surveyId;
        this.userId = userId;
        this.submittedAt = submittedAt;

        this.answers = new ArrayList<>();
        if (answers != null && !answers.isEmpty()) {
            this.answers.addAll(answers);
            this.emptyAnswer = false;
        } else {
            this.emptyAnswer = true;
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
    public boolean isEmptyAnswer() { return emptyAnswer; }
    public String getSubmittedAt() { return submittedAt; }
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }

    // Afegir resposta: actualitza emptyAnswer
    public void addAnswer(Answer answer) {
        if (answer == null) return;
        this.answers.add(answer);
        this.emptyAnswer = false;
    }

    // Eliminar resposta per objecte
    public boolean removeAnswer(Answer answer) {
        if (answer == null) return false;
        boolean removed = this.answers.remove(answer);
        if (this.answers.isEmpty()) this.emptyAnswer = true;
        return removed;
    }

    // Eliminar resposta per id
    public boolean removeAnswerById(String answerId) {
        if (answerId == null || answerId.isEmpty()) return false;
        boolean removed = this.answers.removeIf(a -> answerId.equals(a.getId()));
        if (this.answers.isEmpty()) this.emptyAnswer = true;
        return removed;
    }


    // isComplete segons UML: hi ha respostes i cap d'elles és buida
    public boolean isComplete() {
        if (this.answers.isEmpty()) return false;
        for (Answer a : this.answers) {
            if (a == null || a.isEmpty()) return false;
        }
        return true;
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