package importexport;

import Exceptions.NotValidFileException;
import Response.SurveyResponse;
import java.util.*;

/**
 * Interfaz para serializar y deserializar respuestas de encuestas.
 *
 * <p>Las implementaciones convertirán entre objetos {@link Response.SurveyResponse}
 * y su representación en fichero (por ejemplo, texto plano). Se espera que
 * {@code toFile} escriba una o varias respuestas en la ruta indicada y que
 * {@code fromFile} reconstruya un {@link Response.SurveyResponse} desde un
 * fichero.</p>
 *
 * @see importexport.TxtResponseSerializer
 */
public interface ResponseSerializer {
    /**
     * Serializa una lista de respuestas de encuesta a un fichero.
     *
     * @param s    Lista de {@link SurveyResponse} a serializar (no nula).
     * @param path Ruta del fichero de salida donde se escribirá la representación.
     */
    void toFile(List<SurveyResponse> s, String path);

    /**
     * Lee y deserializa una respuesta desde el fichero indicado.
     *
     * @param path Ruta del fichero de entrada.
     * @return El {@link SurveyResponse} leído desde el fichero.
     * @throws NotValidFileException Si el fichero está vacío, malformado o no
     *                               cumple el formato esperado.
     */
    SurveyResponse fromFile(String path) throws NotValidFileException;
}

