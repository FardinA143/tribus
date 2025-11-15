package Encoder;

import java.util.List;
import Survey.Survey;
import Response.SurveyResponse;


/**
 * Interfície per a la codificació de respostes d'enquestes.
 * El seu propòsit és transformar les dades qualitatives i quantitatives de les
 * respostes d'una enquesta en una matriu numèrica (double[][]) que pugui ser
 * utilitzada en els nostres algoritmes de clustering.
 */
public interface IEncoder {

    /**
     * "Aprèn" el vocabulari i el domini de les preguntes de l'enquesta.
     * Per exemple, identifica totes les opcions úniques de preguntes categòriques
     * i els rangs (min/max) de les numèriques.
     *
     * @param survey L'enquesta que defineix les preguntes.
     * @param allResponses Una llista de totes les respostes per "aprendre"
     * els dominis de les preguntes obertes (ex. min/max de OpenInt).
     */
    void fit(Survey survey, List<SurveyResponse> allResponses);

    /**
     * Transforma una llista de respostes en una matriu numèrica, utilitzant el
     * vocabulari i dominis apresos prèviament en el mètode fit().
     *
     * @param responsesToTransform La llista de respostes a codificar.
     * @return Una matriu double[][], on cada fila és una resposta i cada
     * columna és una característica (feature) numèrica.
     * @throws IllegalStateException Si es crida abans que el mètode fit()
     * hagi estat executat.
     */
    double[][] transform(List<SurveyResponse> responsesToTransform);

    /**
     * Mètode de conveniència que executa fit() i transform() en un sol pas.
     *
     * @param survey L'enquesta que defineix les preguntes.
     * @param allResponses La llista completa de respostes per aprendre i transformar.
     * @return La matriu numèrica resultant.
     */
    default double[][] fitTransform(Survey survey, List<SurveyResponse> allResponses) {
        fit(survey, allResponses);
        return transform(allResponses);
    }

    /**
     * Obtiene los nombres de las características (columnas) generadas por el
     * codificador. El orden de los nombres se corresponde con el orden de las
     * columnas en la matriz devuelta por transform().
     *
     * @return Una lista inmutable de los nombres de las características.
     */
    List<String> getFeatureNames();
}