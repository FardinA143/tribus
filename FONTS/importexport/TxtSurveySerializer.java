package importexport;

import java.io.*;
import java.util.*;

public class TxtSurveySerializer implements SurveySerializer {

    @Override
    public void toFile(Survey s, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {

            writer.println(String.join(",",
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getCreatedBy(),
                String.valueOf(s.getK()),
                s.getInitMethod(),
                String.valueOf(s.getDistance()),
                s.getCreatedAt(),
                s.getUpdatedAt()
            ));

            for (Question q : s.getQuestions()) {
                StringBuilder sb = new StringBuilder();

                if (q instanceof OpenIntQuestion oi) {
                    sb.append("oi,")
                      .append(oi.getMin()).append(",")
                      .append(oi.getMax()).append(",");
                } 
                else if (q instanceof MultipleChoiceQuestion mc) {
                    sb.append("mc,")
                      .append(mc.getMinChoices()).append(",")
                      .append(mc.getMaxChoices()).append(",");
                } 
                else if (q instanceof SingleChoiceQuestion) {
                    sb.append("sc,");
                } 
                else if (q instanceof OpenStringQuestion os) {
                    sb.append("os,")
                      .append(os.getMaxLength()).append(",")
                      .append(os.getPattern()).append(",");
                }

                // Campos comunes
                sb.append(q.getId()).append(",")
                  .append(q.getText()).append(",")
                  .append(q.isRequired()).append(",")
                  .append(q.getPosition()).append(",")
                  .append(q.getWeight());

                writer.println(sb.toString());
            }

        } 
    }

    @Override
    public Survey fromFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line == null) return null;

            String[] surveyFields = line.split(",");
            Survey survey = new Survey(
                surveyFields[0], // id
                surveyFields[1], // title
                surveyFields[2], // description
                surveyFields[3], // createdBy
                Integer.parseInt(surveyFields[4]), // k
                surveyFields[5], // initMethod
                Double.parseDouble(surveyFields[6]), // distance
                surveyFields[7], // createdAt
                surveyFields[8]  // updatedAt
            );

            List<Question> questions = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String type = parts[0];
                Question q = null;

                switch (type) {
                    case "oi" -> q = new OpenIntQuestion(
                        parts[3], parts[4],
                        Boolean.parseBoolean(parts[5]),
                        Integer.parseInt(parts[6]),
                        Double.parseDouble(parts[7]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                    );

                    case "mc" -> q = new MultipleChoiceQuestion(
                        parts[3], parts[4],
                        Boolean.parseBoolean(parts[5]),
                        Integer.parseInt(parts[6]),
                        Double.parseDouble(parts[7]),
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                    );

                    case "sc" -> q = new SingleChoiceQuestion(
                        parts[1], parts[2],
                        Boolean.parseBoolean(parts[3]),
                        Integer.parseInt(parts[4]),
                        Double.parseDouble(parts[5])
                    );

                    case "os" -> q = new OpenStringQuestion(
                        parts[3], parts[4],
                        Boolean.parseBoolean(parts[5]),
                        Integer.parseInt(parts[6]),
                        Double.parseDouble(parts[7]),
                        Integer.parseInt(parts[1]),
                        parts[2]
                    );
                }

                questions.add(q);
            }

            survey.setQuestions(questions);
            return survey;

        } 
    }
}
