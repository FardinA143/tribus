package importexport;

import Exceptions.NotValidFileException;
import Survey.Survey;

/**
 * Interfaz para serializar y deserializar encuestas ({@link Survey.Survey}).
 *
 * <p>Permite convertir una {@link Survey} a un fichero y volver a construirla
 * desde ese fichero. El formato concreto queda a cargo de la implementación
 * (por ejemplo, texto plano - {@link TxtSurveySerializer}).</p>
 */
public interface SurveySerializer {
    /**
     * Serializa la encuesta proporcionada en la ruta indicada.
     *
     * @param s    Encuesta a serializar.
     * @param path Ruta del fichero de salida.
     */
    void toFile(Survey s, String path);

    /**
     * Lee una encuesta desde el fichero indicado y la devuelve como objeto
     * {@link Survey}.
     *
     * @param path Ruta del fichero de entrada.
     * @return La {@link Survey} leída desde el fichero.
     * @throws NotValidFileException Si el fichero está vacío, incompleto o
     *                               malformado.
     */
    Survey fromFile(String path) throws NotValidFileException;
}
