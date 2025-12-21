package persistence;

import Exceptions.NullArgumentException;
import Exceptions.PersistenceException;
import Response.SurveyResponse;
import Survey.Survey;
import user.RegisteredUser;
import java.util.Collection;
import java.util.List;

/**
 * PersistenceDriver: punt d'entrada unificat per a totes les operacions de persistència.
 * Coordina UserPersistence, SurveyPersistence i ResponsePersistance.
 */
public class PersistenceDriver {
    
    private final UserPersistence userPersistence;
    private final SurveyPersistence surveyPersistence;
    private final ResponsePersistance responsePersistance;
    
    /**
     * Constructor per defecte amb configuració estàndard.
     */
    public PersistenceDriver() {
        this.userPersistence = new UserPersistence();
        this.surveyPersistence = new SurveyPersistence();
        this.responsePersistance = new ResponsePersistance();
    }
    
    /**
     * Constructor amb injecció de dependències (per a testing o configuració personalitzada).
     */
    public PersistenceDriver(UserPersistence userPersistence, 
                            SurveyPersistence surveyPersistence, 
                            ResponsePersistance responsePersistance) {
        if (userPersistence == null || surveyPersistence == null || responsePersistance == null) {
            throw new IllegalArgumentException("Persistence components cannot be null");
        }
        this.userPersistence = userPersistence;
        this.surveyPersistence = surveyPersistence;
        this.responsePersistance = responsePersistance;
    }
    
    // ==================== USER PERSISTENCE ====================
    
    /**
     * Desa un usuari registrat.
     */
    public void persistUser(RegisteredUser user) throws NullArgumentException, PersistenceException {
        userPersistence.persistUser(user);
    }
    
    /**
     * Desa una col·lecció completa d'usuaris (sobrescriu el fitxer).
     */
    public void persistAllUsers(Collection<RegisteredUser> users) 
            throws NullArgumentException, PersistenceException {
        userPersistence.persistAllUsers(users);
    }

    /**
     * Carrega tots els usuaris registrats des de persistència.
     */
    public List<RegisteredUser> loadAllUsers() throws PersistenceException {
        return userPersistence.loadAllUsers();
    }
    
    // ==================== SURVEY PERSISTENCE ====================
    
    /**
     * Desa una enquesta.
     */
    public void saveSurvey(Survey survey) throws NullArgumentException, PersistenceException {
        surveyPersistence.save(survey);
    }
    
    /**
     * Carrega una enquesta pel seu ID.
     */
    public Survey loadSurvey(String surveyId) throws NullArgumentException, PersistenceException {
        return surveyPersistence.load(surveyId);
    }
    
    /**
     * Carrega totes les enquestes disponibles.
     */
    public List<Survey> loadAllSurveys() throws PersistenceException {
        return surveyPersistence.loadAll();
    }
    
    /**
     * Elimina una enquesta.
     * @return true si s'ha eliminat, false si no existia.
     */
    public boolean deleteSurvey(String surveyId) throws NullArgumentException, PersistenceException {
        return surveyPersistence.delete(surveyId);
    }
    
    // ==================== RESPONSE PERSISTENCE ====================
    
    /**
     * Desa totes les respostes d'una enquesta (sobrescriu).
     */
    public void saveAllResponses(String surveyId, List<SurveyResponse> responses) 
            throws NullArgumentException, PersistenceException {
        responsePersistance.saveAll(surveyId, responses);
    }
    
    /**
     * Afegeix una resposta a una enquesta.
     */
    public void appendResponse(String surveyId, SurveyResponse response) 
            throws NullArgumentException, PersistenceException {
        responsePersistance.append(surveyId, response);
    }
    
    /**
     * Carrega totes les respostes d'una enquesta.
     */
    public List<SurveyResponse> loadAllResponses(String surveyId) 
            throws NullArgumentException, PersistenceException {
        return responsePersistance.loadAll(surveyId);
    }
    
    /**
     * Elimina totes les respostes associades a una enquesta.
     * @return true si s'ha eliminat, false si no existia.
     */
    public boolean deleteResponses(String surveyId) throws NullArgumentException, PersistenceException {
        return responsePersistance.delete(surveyId);
    }
    
    // ==================== BATCH OPERATIONS ====================
    
    /**
     * Elimina completament una enquesta i totes les seves respostes.
     * @return true si s'ha eliminat l'enquesta, false si no existia.
     */
    public boolean deleteCompleteSurvey(String surveyId) 
            throws NullArgumentException, PersistenceException {
        responsePersistance.delete(surveyId); // Eliminar respostes (pot no existir)
        return surveyPersistence.delete(surveyId); // Eliminar enquesta
    }
}
