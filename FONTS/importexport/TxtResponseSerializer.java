package importexport;

import java.io.*;
import java.util.*;

public class TxtResponseSerializer implements ResponseSerializer {

    @Override
    public void toFile(SurveyResponse response, String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            
            writer.println(String.join(",",
                response.getId(),
                response.getSurvey().getId(),
                response.getSubmittedAt()
            ));

            for (Answer ans : response.getAnswers()) {
                StringBuilder sb = new StringBuilder();
                
                if (ans instanceof OpenIntAnswer oi) {
                    sb.append("oi,")
                      .append(oi.getId()).append(",")
                      .append(oi.getQuestion().getId()).append(",")
                      .append(oi.getResponse());
                } 
                else if (ans instanceof OpenStringAnswer os) {
                    sb.append("os,")
                      .append(os.getId()).append(",")
                      .append(os.getQuestion().getId()).append(",")
                      .append(os.getResponse());
                } 
                else if (ans instanceof SingleChoiceAnswer sc) {
                    sb.append("sc,")
                      .append(sc.getId()).append(",")
                      .append(sc.getQuestion().getId()).append(",")
                      .append(sc.getSelectedOption());
                } 
                else if (ans instanceof MultipleChoiceAnswer mc) {
                    sb.append("mc,")
                      .append(mc.getId()).append(",")
                      .append(mc.getQuestion().getId()).append(",");
                    // Lista de opciones separadas por '|'
                    sb.append(String.join("|", mc.getSelectedOptions()));
                }

                writer.println(sb.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SurveyResponse fromFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line = br.readLine();
            if (line == null) return null;

            String[] header = line.split(",");
            String id = header[0];
            String surveyId = header[1];
            String submittedAt = header[2];
            Survey survey = new Survey();
            survey.setId(surveyId);

            List<Answer> answers = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String type = parts[0];
                Answer a = null;

                switch (type) {
                    case "oi" -> {
                        a = new OpenIntAnswer(
                            parts[1], // id
                            new QuestionRef(parts[2]), 
                            Integer.parseInt(parts[3])
                        );
                    }
                    case "os" -> {
                        a = new OpenStringAnswer(
                            parts[1],
                            new QuestionRef(parts[2]),
                            parts[3]
                        );
                    }
                    case "sc" -> {
                        a = new SingleChoiceAnswer(
                            parts[1],
                            new QuestionRef(parts[2]),
                            parts[3]
                        );
                    }
                    case "mc" -> {
                        String[] opts = parts[3].split("\\|");
                        a = new MultipleChoiceAnswer(
                            parts[1],
                            new QuestionRef(parts[2]),
                            Arrays.asList(opts)
                        );
                    }
                }
                answers.add(a);
            }

            return new SurveyResponse(id, survey, answers, submittedAt);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static class QuestionRef extends Question {
        public QuestionRef(String id) { this.setId(id); }
    }
}
