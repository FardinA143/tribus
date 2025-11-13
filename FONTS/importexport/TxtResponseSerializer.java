package importexport;

import Response.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import Exceptions.NotValidFileException;
import Survey.*;

public class TxtResponseSerializer implements ResponseSerializer {

    public void toFile(List<SurveyResponse> lresponse, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            //Primera linea imprime: 012334,234,345,
            writer.println(String.join(",",
                lresponse.get(0).getId(), //0
                lresponse.get(0).getSurveyId(), //1
                lresponse.get(0).getUserId(), //2
                lresponse.get(0).getSubmittedAt() //3
            ));
            for(SurveyResponse response: lresponse){
                for (Answer ans : response.getAnswers()) {
    StringBuilder sb = new StringBuilder();
    //Para cada linea de respuestas, tenemos primero el tipo de pregunta y despues:  El ID de la pregunta y el valor, ej:
    //(sc,mc,ia,ta) tiene indice 4
    switch (ans.getType()) {
        case INT -> {
            if(!ans.isEmpty()){
            IntAnswer a = (IntAnswer) ans;
              sb.append("ia").append(a.getQuestionId()).append(",")
              .append(a.getValue());
            }
        }

        case TEXT -> {
            if(!ans.isEmpty()){
            TextAnswer a = (TextAnswer) ans;
            sb.append("ta").append(a.getQuestionId()).append(",")
              .append(a.getValue());
            }
        }

        case SINGLE_CHOISE -> {
            if(!ans.isEmpty()){
            SingleChoiceAnswer a = (SingleChoiceAnswer) ans;
            sb.append("sc").append(a.getQuestionId()).append(",")
              .append(a.getOptionId());
            }
        }

        case MULTIPLE_CHOISE -> {
            // Ejemplo: multiple,questionId,opción1|opción2|opción3
            MultipleChoiceAnswer a = (MultipleChoiceAnswer) ans;
            sb.append("mc").append(a.getQuestionId()).append(",")
            .append(a.optionIdsCsv());
        }
    }

    writer.println(sb.toString());
}
}

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SurveyResponse fromFile(String path) throws NotValidFileException {
        SurveyResponse response = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if(line == null) throw new NotValidFileException();

            String[] surveyFields = line.split(",");
            if(surveyFields.length < 5) throw new NotValidFileException();

            List<Answer>answers = new ArrayList<>();
            response  = new SurveyResponse(surveyFields[0],
                surveyFields[1], 
                surveyFields[2], 
                Boolean.parseBoolean(surveyFields[3]),
                surveyFields[4], 
                answers);

            while((line = reader.readLine()) != null){
                String[] parts = line.split(",");

                String type = parts[4];
                Answer a = null;

                switch(type){
                    case "ia" -> a = new IntAnswer(
                        Integer.parseInt(parts[5]), //QuestionId
                        Integer.parseInt(parts[6]) //Value
                        );
                    
                    case "mc" -> a = new MultipleChoiceAnswer(
                        Integer.parseInt(parts[5]), //QuestionId
                        parts[6]
                        ); 

                    case "sc" -> a  = new SingleChoiceAnswer(
                        Integer.parseInt(parts[5]), //QuestionId
                        Integer.parseInt(parts[6]) //ChoiceIndex
                    );

                    case "ta" -> a = new TextAnswer(
                        Integer.parseInt(parts[5]),
                        parts[6]
                    );
                }
                if(a != null){
                    response.addAnswer(a);
                }
            }

        }  catch (Exception e){
            throw new NotValidFileException();
        }
        return response;
    }
}
