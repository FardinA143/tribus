package response;

import java.util.ArrayList;
import java.util.List;

/*Aquest objecte agrupa totes les respostes (Answer) que l’usuari ha 
donat a aquella enquesta en una mateixa submissió. Si fa mes submissions t
tindrem mes surveyResponse*/

public class SurveyResponse  {
    private String id; 
    private String surveyId; 
    private String userId; 
    private boolean emptyAnswer; 
    private String  submittedAt; 

    private final List<Answer> answers; 

    //constructors
    public SurveyResponse(
             String id, 
             String surveyId,
             String userId, 
             boolean emptyAnswer, 
             String  submittedAt, 

             List<Answer> answers) {

            /*comprovem que no siguin camps buits ni nulls */
        


        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("id no pot ser null o buit");
        }
        if (surveyId == null || surveyId.isEmpty()) {
            throw new IllegalArgumentException("surveyId no pot ser null o buit");
        }
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId no pot ser null o buit");
        }

        /*this.id → el camp (atribut) de la instància.
        id → el paràmetre del mètode/constructor (amb el mateix nom). */

        this.id = id;  //no pot ser null 
        this.surveyId = surveyId; 
        this.userId = userId; 
        this.emptyAnswer = false; //fins que no es contesti
        this.submittedAt = submittedAt; 

        this.answers = new ArrayList<>(); 

        if (answers != null) {
            this.answers.addAll(answers); 
        }
    }

    public String getId() {
        return id;
    }

    public String getSurveyId() {
        return surveyId; 
    }

    public String getUserId()  {
        return userId; 
    }

    public boolean isEmptyAnswer() {
      return emptyAnswer; 
    }

    

    public String getSubmittedAt() {
       return submittedAt; 
    }



    //afegim una resposta a la List
    public void addAnswer(Answer answer) {
        //si null 
        if (answer == null) return;
        //en cas de haver resposta la afegim 
        this.answers.add(answer);
        this.emptyAnswer = true;
    }
    
    //borrem una resposta de la List

    public boolean removeAnswer(Answer answer) {
        //si null retorna false (cap que borrar)
        if (answer == null) return false;
        //en cas de haver resposta la afegim 
        this.emptyAnswer = true;
        boolean removed = this.answers.remove(answer);

        //si la llista queda buida: 
        if (this.answers.isEmpty()) {
            return false; 
        }
        return removed; 
    }

    public boolean isComplete() {
        if (this.answers.isEmpty()) {
            return false; 
        }   
        for (Answer answer : this.answers) {
            if (answer == null || !answer.isComplete()) {   
                return false; 
            }

        }   
        return true; 


    }
    



}




