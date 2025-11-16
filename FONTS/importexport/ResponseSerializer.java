package importexport;

import Exceptions.NotValidFileException;
import Response.SurveyResponse;
import java.util.*;

/**
 * Interfície per serialitzar i deserialitzar respostes d’enquestes.
 *
 * <p>Les implementacions convertiran entre objectes {@link Response.SurveyResponse}
 * i la seva representació en fitxer txt S’espera que
 * {@code toFile} escrigui diverses respostes al camí indicat i que
 * {@code fromFile} reconstrueixi un {@link Response.SurveyResponse} a partir
 * d’un fitxer.</p>
 *
 * @see importexport.TxtResponseSerializer
 */
public interface ResponseSerializer {

    /**
     * Serialitza una llista de respostes d’enquesta a un fitxer.
     *
     * @param s    Llista de {@link SurveyResponse} a serialitzar (no nul·la).
     * @param path Camí del fitxer de sortida on s’escriurà la representació.
     */
    void toFile(List<SurveyResponse> s, String path);

    /**
     * Llegeix i deserialitza una resposta des del fitxer indicat.
     *
     * @param path Camí del fitxer d’entrada.
     * @return El {@link SurveyResponse} llegit del fitxer.
     * @throws NotValidFileException Si el fitxer és buit, està malformat
     *                               o no compleix el format esperat.
     */
    SurveyResponse fromFile(String path) throws NotValidFileException;
}
