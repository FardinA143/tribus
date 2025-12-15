package importexport;

import Response.SurveyResponse;

import java.io.IOException;
import java.util.*;

/**
 * Interfície per serialitzar i deserialitzar respostes d’enquestes.
 *
 * <p>Les implementacions convertiran entre objectes {@link Response.SurveyResponse}
 * i la seva representació en fitxer txt. S’espera que
 * {@code toFile} escrigui diverses respostes al camí indicat i que
 * {@code fromFile} reconstrueixi totes les respostes presents al fitxer.</p>
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
     * Llegeix i deserialitza totes les respostes contingudes en el fitxer indicat.
     *
     * @param path Camí del fitxer d’entrada.
     * @return Llista de {@link SurveyResponse} llegides del fitxer.
     * @throws IOException Si el fitxer és buit, està malformat
     *                     o no compleix el format esperat.
     */
    List<SurveyResponse> fromFile(String path) throws IOException;
}
