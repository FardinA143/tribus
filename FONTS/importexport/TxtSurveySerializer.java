package importexport;

import java.io.*;
import java.util.*;
import Survey.*;
import Exceptions.*;

public class TxtSurveySerializer implements SurveySerializer {
    //Pre: Asumimos que el Survey tiene todos los campos correctos 
    //Post: Escribe a un archivo una encuesta con sus preguntas
    public void toFile(Survey s, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            //Linea 1, Campos de la encuesta
            //Ejemplo: 1234934,"Enquesta de Pixar", "Una enquesta..", "Pol", 1, "yes", "23", "españa", "21-10-22"
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

            // Siguientes líneas: las preguntas
            //Ejemplo: 1,"Quien eres?", 0,2,oi(en el caso de OpenInt), 1,3
            for (Question q : s.getQuestions()) {
                StringBuilder sb = new StringBuilder();
                //Campos comunes
                sb.append(q.getId()).append(",") //0
                  .append(q.getText()).append(",") //1
                  .append(q.isRequired()).append(",") //2 
                  .append(q.getPosition()).append(",") //3
                  .append(q.getWeight()); //4
                  //Campos específicos
                if (q instanceof OpenIntQuestion oi) {
                    sb.append("oi").append(oi.getMin()).append(",")
                      .append(oi.getMax()).append(",");
                } 
                else if (q instanceof MultipleChoiceQuestion mc) {
                    sb.append("mc").append(mc.getMinChoices()).append(",")
                      .append(mc.getMaxChoices()).append(",");
                } 
                else if (q instanceof OpenStringQuestion os) {
                    sb.append("os").append(os.getMaxLength()).append(",");
                }
                else sb.append("sc").append(",");
                writer.println(sb.toString());
            }

        } catch (IOException e) {
            System.err.println("Error writing survey to file: " + e.getMessage());
        }
    }
    public Survey fromFile(String path) throws NotValidFileException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line == null) throw new NotValidFileException();

            String[] surveyFields = line.split(",");
            if (surveyFields.length < 9) {
                throw new NotValidFileException(); 
            }

            Survey survey = new Survey(
                surveyFields[0], // id
                surveyFields[1], // title
                surveyFields[2], // description
                surveyFields[3], // createdBy
                Integer.parseInt(surveyFields[4]), // k
                surveyFields[5], // initMethod
                surveyFields[6], // distance (String)
                surveyFields[7], // createdAt
                surveyFields[8]  // updatedAt
            );

            List<Question> questions = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                String type = parts[5];
                Question q = null;


                switch (type) {
                    case "oi" -> q = new OpenIntQuestion(
                        Integer.parseInt(parts[0]), //id
                        parts[1], //text
                        Boolean.parseBoolean(parts[2]), //required
                        Integer.parseInt(parts[3]), //position
                        Double.parseDouble(parts[4]), //weight
                        Integer.parseInt(parts[6]), //min
                        Integer.parseInt(parts[7]) //max
                    );

                    case "mc" -> q = new MultipleChoiceQuestion(
                        Integer.parseInt(parts[0]), //id 
                        parts[1], //text
                        Boolean.parseBoolean(parts[2]), //required
                        Integer.parseInt(parts[3]), //position
                        Double.parseDouble(parts[4]), //weight
                        Integer.parseInt(parts[6]), //minChoices
                        Integer.parseInt(parts[7]) //maxChoices
                    );

                    case "sc" -> q = new SingleChoiceQuestion(
                        Integer.parseInt(parts[0]), //id
                        parts[1], //text
                        Boolean.parseBoolean(parts[2]), //required
                        Integer.parseInt(parts[3]), //position
                        Double.parseDouble(parts[4]) //weight
                    );

                    case "os" -> q = new OpenStringQuestion(
                        Integer.parseInt(parts[0]),  //id
                        parts[1], //text
                        Boolean.parseBoolean(parts[2]), //required
                        Integer.parseInt(parts[3]), //position
                        Double.parseDouble(parts[4]), //weight
                        Integer.parseInt(parts[6]) //maxLength
                    );
                }

                if (q != null) {
                    questions.add(q);
                }
            }

            // Cargar preguntas dentro de Survey
            survey.importQuestions(questions);
            return survey;

        } catch (IOException e) {
            throw new NotValidFileException();
        }  catch (Exception e) {
            throw new NotValidFileException();
        }
    }
}
