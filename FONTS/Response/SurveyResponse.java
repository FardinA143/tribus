// java
package Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SurveyResponse: Agrupa les respostes d'una única submissió a una enquesta.
 * Aquesta classe representa el vector de respostes d'un individu (Xi) 
 * que s'utilitzarà posteriorment per als algorismes de clustering.
 *
 * NOTA: La validació d'arguments (id, surveyId, userId) llença IllegalArgumentException si són nulls o buits.
 */
public class SurveyResponse {
    
    /** Identificador únic de la submissió de la resposta. Clau d'identitat. */
    private final String id;
    
    /** Identificador de l'enquesta a la qual pertany la resposta. */
    private final String surveyId;
    
    /** Identificador de l'usuari (o persona) que va respondre l'enquesta. */
    private final String userId;

    /** Data i hora de la submissió de l'enquesta. */
    private String submittedAt;

    /** Llista de les respostes individuals (Answer) a les preguntes. */
    private final List<Answer> answers;

    /**
     * Constructor principal de SurveyResponse.
     * @param id Identificador únic de la resposta.
     * @param surveyId ID de l'enquesta.
     * @param userId ID de l'usuari que va respondre.
     * @param submittedAt Data de submissió (pot ser null).
     * @param answers Llista inicial de respostes (pot ser null o buida).
     * @throws IllegalArgumentException si id, surveyId o userId són nulls o buits.
     */
    public SurveyResponse(String id, String surveyId, String userId, String submittedAt, List<Answer> answers) {
        // Validacions de precondició: els IDs fonamentals no poden ser nulls o buits.
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
        // Afegim les respostes si la llista no és null i no està buida.
        if (answers != null && !answers.isEmpty()) {
            this.answers.addAll(answers);
            
        }
    }

    /**
     * Constructor alternatiu senzill per crear una SurveyResponse sense respostes inicials.
     * @param id Identificador únic de la resposta.
     * @param surveyId ID de l'enquesta.
     * @param userId ID de l'usuari.
     */
    public SurveyResponse(String id, String surveyId, String userId) {
        this(id, surveyId, userId, null, null);
    }

    /**
     * Retorna l'identificador únic de la resposta.
     * @return L'ID de la resposta.
     */
    public String getId() { return id; }
    
    /**
     * Retorna l'identificador de l'enquesta.
     * @return L'ID de l'enquesta.
     */
    public String getSurveyId() { return surveyId; }
    
    /**
     * Retorna l'identificador de l'usuari.
     * @return L'ID de l'usuari.
     */
    public String getUserId() { return userId; }
    
    /**
     * Retorna la data de submissió.
     * @return La data de submissió.
     */
    public String getSubmittedAt() { return submittedAt; }
    
    /**
     * Retorna una còpia de la llista de respostes.
     * @return Llista de les respostes (Answers).
     */
    public List<Answer> getAnswers() { return new ArrayList<>(answers); }

    /**
     * Afegeix una resposta individual a la llista.
     * @param answer La resposta a afegir.
     */
    public void addAnswer(Answer answer) {
        this.answers.add(answer);
    }

    /**
     * Elimina una resposta de la llista per referència a l'objecte.
     * @param answer La resposta a eliminar.
     */
    public void removeAnswer(Answer answer) {
        this.answers.remove(answer);
    }

    /**
     * Compara si dos objectes SurveyResponse són iguals. 
     * La igualtat es basa únicament en la clau primària: l'ID de la resposta.
     * @param o L'objecte a comparar.
     * @return Cert si els IDs coincideixen, fals altrament.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SurveyResponse)) return false;
        SurveyResponse that = (SurveyResponse) o;
        // La clau d'igualtat és únicament l'ID.
        return id.equals(that.id);
    }

    /**
     * Retorna el codi hash de l'objecte, basat únicament en l'ID.
     * @return El codi hash.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}