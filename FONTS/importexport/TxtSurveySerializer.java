package importexport;

import java.io.*;
import java.util.*;
import Survey.*;
import Exceptions.*;

/**
 * Implementación de {@link SurveySerializer} que permite exportar e importar
 * encuestas en formato de texto plano (TXT), utilizando un formato basado en
 * líneas y valores separados por comas.
 *
 * <p>El archivo generado contiene:</p>
 * <ul>
 *   <li><b>Línea 1:</b> Los campos principales de la encuesta.</li>
 *   <li><b>Líneas siguientes:</b> Una línea por cada pregunta, con formato
 *       dependiente del tipo de pregunta.</li>
 * </ul>
 *
 * <p>Soporta los siguientes tipos de preguntas:</p>
 * <ul>
 *   <li>{@link OpenIntQuestion}</li>
 *   <li>{@link MultipleChoiceQuestion}</li>
 *   <li>{@link OpenStringQuestion}</li>
 *   <li>{@link SingleChoiceQuestion}</li>
 * </ul>
 */
public class TxtSurveySerializer implements SurveySerializer {

    /**
     * Escribe una encuesta en un archivo de texto siguiendo el formato
     * establecido. La primera línea contiene los campos de la encuesta y las
     * siguientes cada una de sus preguntas.
     *
     * @param s     Encuesta que se desea serializar.
     * @param path  Ruta del archivo donde se guardará la encuesta.
     */
    @Override
    public void toFile(Survey s, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {

            // Línea 1: datos base de la encuesta
            writer.println(String.join(",",
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getCreatedBy(),
                String.valueOf(s.getK()),
                s.getInitMethod(),
                s.getDistance(),
                s.getCreatedAt(),
                s.getUpdatedAt()
            ));

            // Siguientes líneas: preguntas de la encuesta
            for (Question q : s.getQuestions()) {
                StringBuilder sb = new StringBuilder();

                // Campos comunes
                sb.append(q.getId()).append(",")
                  .append(q.getText()).append(",")
                  .append(q.isRequired()).append(",")
                  .append(q.getPosition()).append(",")
                  .append(q.getWeight());

                // Campos específicos según tipo
                if (q instanceof OpenIntQuestion oi) {
                    sb.append(",oi,")
                      .append(oi.getMin()).append(",")
                      .append(oi.getMax());
                } 
                else if (q instanceof MultipleChoiceQuestion mc) {
                    sb.append(",mc,")
                      .append(mc.getMinChoices()).append(",")
                      .append(mc.getMaxChoices());
                } 
                else if (q instanceof OpenStringQuestion os) {
                    sb.append(",os,")
                      .append(os.getMaxLength());
                }
                else {
                    sb.append(",sc");
                }

                writer.println(sb.toString());
            }

        } catch (IOException e) {
            System.err.println("Error writing survey to file: " + e.getMessage());
        }
    }

    /**
     * Lee una encuesta desde un archivo de texto en formato propio y construye
     * un objeto {@link Survey} con sus preguntas.
     *
     * @param path Ruta del archivo que contiene la encuesta.
     * @return Un objeto {@link Survey} cargado desde el archivo.
     * @throws NotValidFileException Si el archivo está vacío, incompleto,
     *                               malformado o algún dato no puede parsearse.
     */
    @Override
    public Survey fromFile(String path) throws NotValidFileException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine();
            if (line == null) throw new NotValidFileException();

            // Campos principales de Survey
            String[] surveyFields = line.split(",");
            if (surveyFields.length < 9) throw new NotValidFileException();

            Survey survey = new Survey(
                surveyFields[0],
                surveyFields[1],
                surveyFields[2],
                surveyFields[3],
                Integer.parseInt(surveyFields[4]),
                surveyFields[5],
                surveyFields[6],
                surveyFields[7],
                surveyFields[8]
            );

            List<Question> questions = new ArrayList<>();

            // Leer preguntas
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                Question q = null;
                String type = parts[5];

                switch (type) {
                    case "oi" -> q = new OpenIntQuestion(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]),
                        Double.parseDouble(parts[4]),
                        Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7])
                    );

                    case "mc" -> q = new MultipleChoiceQuestion(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]),
                        Double.parseDouble(parts[4]),
                        Integer.parseInt(parts[6]),
                        Integer.parseInt(parts[7])
                    );

                    case "sc" -> q = new SingleChoiceQuestion(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]),
                        Double.parseDouble(parts[4])
                    );

                    case "os" -> q = new OpenStringQuestion(
                        Integer.parseInt(parts[0]),
                        parts[1],
                        Boolean.parseBoolean(parts[2]),
                        Integer.parseInt(parts[3]),
                        Double.parseDouble(parts[4]),
                        Integer.parseInt(parts[6])
                    );
                }

                if (q != null) questions.add(q);
            }

            survey.importQuestions(questions);
            return survey;

        } catch (IOException e) {
            throw new NotValidFileException();
        } catch (Exception e) {
            throw new NotValidFileException();
        }
    }
}
