package importexport;

import Response.*;
import java.io.*;
import java.util.*;

import Survey.*;

/**
 * Implementació de {@link ResponseSerializer} per a un format de text (TXT).
 *
 * <p>Format esperat (escriptura):</p>
 * <ul>
 *   <li>Línia 1: camps de capçalera separats per comes: id,surveyId,userId,submittedAt</li>
 *   <li>Línies següents: una línia per resposta/answer amb prefixos de tipus
 *       i valors separats per comes. Tipus suportats: <code>ia</code> (int),
 *       <code>ta</code> (text), <code>sc</code> (single choice),
 *       <code>mc</code> (multiple choice).</li>
 * </ul>
 *
 * <p>L'aplicació intenta ser tolerant en l'escriptura i llença
 * {@link IOException} en llegir si el fitxer no compleix
 * el format esperat.</p>
 */
public class TxtResponseSerializer implements ResponseSerializer {

    /**
     * Escriu una llista de {@link SurveyResponse} al fitxer indicat.
     *
     * @param lresponse Llista de respostes a serialitzar. S’assumeix no buida
     *                  (la implementació actual utilitza el primer element
     *                  per escriure la capçalera).
     * @param path      Camí del fitxer de sortida.
     */
    @Override
    public void toFile(List<SurveyResponse> lresponse, String path) {
        if(!path.toLowerCase().endsWith(".txt")){
            path = path + ".txt";
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            // Primera línia: 012334,234,345,...
            writer.println(String.join(",",
                lresponse.get(0).getId(), //0
                lresponse.get(0).getSurveyId(), //1
                lresponse.get(0).getUserId(), //2
                lresponse.get(0).getSubmittedAt() //3
            ));
            for(SurveyResponse response: lresponse){
                for (Answer ans : response.getAnswers()) {
                    StringBuilder sb = new StringBuilder();
                    // Per cada línia de resposta tenim primer el tipus de pregunta i després:
                    // El ID de la pregunta i el valor, p. ex: ia,1,42
                    switch (ans.getType()) {
                        case INT -> {
                            if(!ans.isEmpty()){
                                IntAnswer a = (IntAnswer) ans;
                                sb.append("ia").append(",").append(a.getQuestionId()).append(",")
                                  .append(a.getValue());
                            }
                        }

                        case TEXT -> {
                            if(!ans.isEmpty()){
                                TextAnswer a = (TextAnswer) ans;
                                sb.append("ta").append(",").append(a.getQuestionId()).append(",")
                                  .append(a.getValue());
                            }
                        }

                        case SINGLE_CHOICE -> {
                            if(!ans.isEmpty()){
                                SingleChoiceAnswer a = (SingleChoiceAnswer) ans;
                                sb.append("sc").append(",").append(a.getQuestionId()).append(",")
                                  .append(a.getOptionId());
                            }
                        }

                        case MULTIPLE_CHOICE -> {
                            // Exemple: mc,questionId,opció1|opció2|opció3
                            if(!ans.isEmpty()){
                                MultipleChoiceAnswer a = (MultipleChoiceAnswer) ans;
                                sb.append("mc").append(",").append(a.getQuestionId()).append(",")
                                  .append(a.optionIdsCsv());
                            }
                        }
                    }

                    writer.println(sb.toString());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Llegeix una {@link SurveyResponse} des d'un fitxer en el format propi.
     *
     * @param path Camí del fitxer a llegir.
     * @return SurveyResponse reconstruïda a partir del fitxer.
     * @throws IOException Si el fitxer és buit o malformat.
     */
    @Override
    public SurveyResponse fromFile(String path) throws IOException {
        SurveyResponse response = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if(line == null) throw new IOException("File is empty");

            String[] surveyFields = line.split(",");
            if(surveyFields.length < 4) throw new IOException("Invalid header format");

            List<Answer>answers = new ArrayList<>();
            response  = new SurveyResponse(surveyFields[0],
                surveyFields[1], 
                surveyFields[2], 
                surveyFields[3], 
                answers);

            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");
                
                if(parts.length < 3) continue; // Salta línies malformades

                String type = parts[0];
                Answer a = null;

                switch(type){
                    case "ia" -> a = new IntAnswer(
                        Integer.parseInt(parts[1]), // Id de pregunta
                        Integer.parseInt(parts[2])  // Valor
                    );
                    
                    case "mc" -> a = new MultipleChoiceAnswer(
                        Integer.parseInt(parts[1]), // Id de pregunta
                        parts[2]
                    ); 

                    case "sc" -> a  = new SingleChoiceAnswer(
                        Integer.parseInt(parts[1]), // Id de pregunta
                        Integer.parseInt(parts[2])  // Índex de la opció
                    );

                    case "ta" -> a = new TextAnswer(
                        Integer.parseInt(parts[1]),
                        parts[2]
                    );
                }
                if(a != null){
                    response.addAnswer(a);
                }
            }

        }  catch (Exception e){
            throw new IOException("Error reading file: " + e.getMessage(), e);
        }
        return response;
    }
}
