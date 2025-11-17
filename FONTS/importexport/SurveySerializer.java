package importexport;

import java.io.IOException;

import Survey.*;

/**
 * Interfície per serialitzar i deserialitzar enquestes ({@link Survey.Survey}).
 *
 * <p>Permet convertir una {@link Survey} en un fitxer i tornar-la a construir
 * des d’aquest fitxer. S'espera que {@code toFile} escrigui el "header" amb els atributs de {@link Survey}
 * i les diverses preguntes que tenen amb un identificador del tipus de pregunta</p>
 * 
 * @see importexport.SurveySerializer
 * 
 */


public interface SurveySerializer {

    /**
     * Serialitza l’enquesta proporcionada al camí indicat.
     *
     * @param s    Enquesta a serialitzar.
     * @param path Camí del fitxer de sortida.
     */
    void toFile(Survey s, String path);

    /**
     * Llegeix una enquesta des del fitxer indicat i la retorna com a objecte
     * {@link Survey}.
     *
     * @param path Camí del fitxer d’entrada.
     * @return La {@link Survey} llegida del fitxer.
     * @throws IOException Si el fitxer és buit, incomplet o
     *                               malformat.
     */
    Survey fromFile(String path) throws IOException;
}
