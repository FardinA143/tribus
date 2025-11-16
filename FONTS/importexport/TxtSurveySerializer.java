package importexport;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import Survey.*;

/**
 * Implementació de {@link SurveySerializer} que permet exportar i importar
 * enquestes en format de text pla (TXT), utilitzant un format basat en
 * línies i valors separats per comes.
 *
 * <p>El fitxer generat conté:</p>
 * <ul>
 *   <li><b>Línia 1:</b> Els camps principals de l'enquesta.</li>
 *   <li><b>Línies següents:</b> Una línia per a cada pregunta, amb format
 *       dependent del tipus de pregunta.</li>
 * </ul>
 *
 * <p>Suporta els següents tipus de preguntes:</p>
 * <ul>
 *   <li>{@link OpenIntQuestion}</li>
 *   <li>{@link MultipleChoiceQuestion}</li>
 *   <li>{@link OpenStringQuestion}</li>
 *   <li>{@link SingleChoiceQuestion}</li>
 * </ul>
 */
public class TxtSurveySerializer implements SurveySerializer {

    /**
     * Escriu una enquesta en un fitxer de text seguint el format establert.
     * La primera línia conté els camps de l'enquesta i les següents línies
     * contenen cadascuna de les preguntes.
     *
     * @param s     Enquesta que es vol serialitzar.
     * @param path  Ruta del fitxer on es desarà l'enquesta.
     */
    @Override
    public void toFile(Survey s, String path) {
        if(!path.toLowerCase().endsWith(".txt")){
            path = path + ".txt";
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {

            // Línia 1: dades principals de l'enquesta
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

            // Línies següents: preguntes de l'enquesta
            for (Question q : s.getQuestions()) {
                StringBuilder sb = new StringBuilder();

                // Camps comuns
                sb.append(q.getId()).append(",")
                  .append(q.getText()).append(",")
                  .append(q.isRequired()).append(",")
                  .append(q.getPosition()).append(",")
                  .append(q.getWeight());

                // Camps específics segons el tipus
                                if (q instanceof OpenIntQuestion oi) {
                                        sb.append(",oi,")
                                            .append(oi.getMin()).append(",")
                                            .append(oi.getMax());
                                }
                                else if (q instanceof MultipleChoiceQuestion mc) {
                                        sb.append(",mc,")
                                            .append(mc.getMinChoices()).append(",")
                                            .append(mc.getMaxChoices()).append(",")
                                            .append(serializeOptions(mc.getOptions()));
                                }
                                else if (q instanceof OpenStringQuestion os) {
                                        sb.append(",os,")
                                            .append(os.getMaxLength());
                                }
                                else if (q instanceof SingleChoiceQuestion sc) {
                                        sb.append(",sc,")
                                            .append(serializeOptions(sc.getOptions()));
                                }
                                else {
                                        sb.append(",sc");
                                }

                writer.println(sb.toString());
            }

        } catch (IOException e) {
            System.err.println("Error escrivint l'enquesta al fitxer: " + e.getMessage());
        }
    }

    /**
     * Llegeix una enquesta des d'un fitxer de text en format propi i construeix
     * un objecte {@link Survey} amb les seves preguntes.
     *
     * @param path Ruta del fitxer que conté l'enquesta.
     * @return Un objecte {@link Survey} carregat des del fitxer.
     * @throws IOException Si el fitxer està buit, incomplet,
     *                     malformat o algun dada no es pot parsejar.
     */
    @Override
    public Survey fromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {

            String line = br.readLine();
            if (line == null) throw new IOException("File is empty");

            // Camps principals de Survey
            String[] surveyFields = line.split(",", -1);
            if (surveyFields.length < 9) throw new IOException("Invalid header format");

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

            // Llegir preguntes
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);

                if(parts.length < 6) continue; // Salta línies malformades

                Question q = null;
                String type = parts[5];

                switch (type) {
                    case "oi" -> {
                        if(parts.length >= 8) q = new OpenIntQuestion(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            Boolean.parseBoolean(parts[2]),
                            Integer.parseInt(parts[3]),
                            Double.parseDouble(parts[4]),
                            Integer.parseInt(parts[6]),
                            Integer.parseInt(parts[7])
                        );
                    }

                    case "mc" -> {
                        if(parts.length >= 8) {
                            MultipleChoiceQuestion mc = new MultipleChoiceQuestion(
                                Integer.parseInt(parts[0]),
                                parts[1],
                                Boolean.parseBoolean(parts[2]),
                                Integer.parseInt(parts[3]),
                                Double.parseDouble(parts[4]),
                                Integer.parseInt(parts[6]),
                                Integer.parseInt(parts[7])
                            );
                            if(parts.length >= 9) {
                                hydrateOptions(mc.getOptions(), parts[8]);
                            }
                            q = mc;
                        }
                    }

                    case "sc" -> {
                        SingleChoiceQuestion sc = new SingleChoiceQuestion(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            Boolean.parseBoolean(parts[2]),
                            Integer.parseInt(parts[3]),
                            Double.parseDouble(parts[4])
                        );
                        if(parts.length >= 7) {
                            hydrateOptions(sc.getOptions(), parts[6]);
                        }
                        q = sc;
                    }

                    case "os" -> {
                        if(parts.length >= 7) q = new OpenStringQuestion(
                            Integer.parseInt(parts[0]),
                            parts[1],
                            Boolean.parseBoolean(parts[2]),
                            Integer.parseInt(parts[3]),
                            Double.parseDouble(parts[4]),
                            Integer.parseInt(parts[6])
                        );
                    }
                }

                if (q != null) questions.add(q);
            }

            survey.importQuestions(questions);
            return survey;

        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error reading survey file: " + e.getMessage(), e);
        }
    }

    private String serializeOptions(List<ChoiceOption> options) {
        if (options == null || options.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("|");
        for (ChoiceOption option : options) {
            String label = option.getLabel() == null ? "" : option.getLabel();
            String encodedLabel = Base64.getEncoder().encodeToString(label.getBytes(StandardCharsets.UTF_8));
            joiner.add(option.getId() + ":" + encodedLabel);
        }
        return joiner.toString();
    }

    private void hydrateOptions(List<ChoiceOption> target, String encoded) {
        if (target == null || encoded == null || encoded.isEmpty()) {
            return;
        }
        String[] tokens = encoded.split("\\|");
        for (String token : tokens) {
            if (token.isEmpty()) {
                continue;
            }
            String[] parts = token.split(":", 2);
            if (parts.length < 2) {
                continue;
            }
            try {
                int id = Integer.parseInt(parts[0]);
                String label = new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                target.add(new ChoiceOption(id, label));
            } catch (IllegalArgumentException e) {
                // Ignore malformed option entries
            }
        }
    }
}
