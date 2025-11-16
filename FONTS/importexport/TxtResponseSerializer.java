package importexport;

import Exceptions.InvalidArgumentException;
import Exceptions.NullArgumentException;
import Response.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

/**
 * Implementació de {@link ResponseSerializer} per a un format de text (TXT).
 *
 * <p>Format esperat (escriptura):</p>
 * <ul>
 *   <li>Una capçalera per resposta amb el prefix <code>resp</code> seguida de
 *       <code>id,surveyId,userId,submittedAt</code>.</li>
 *   <li>Línies de respostes individuals amb el prefix <code>ans</code>, el tipus
 *       (<code>ia</code>, <code>ta</code>, <code>sc</code>, <code>mc</code>),
 *       l'identificador de la pregunta i la càrrega útil. Les respostes de text
 *       s'escriuen en Base64 per preservar comes i caràcters especials.</li>
 *   <li>El bloque finalitza amb una línia <code>end</code> abans de començar la
 *       següent resposta.</li>
 * </ul>
 *
 * <p>Lector tolerant: si el fitxer no segueix el format multi-resposta modern,
 * es prova de reconstruir una única resposta utilitzant el format llegat.</p>
 *
 * <p>L'aplicació intenta ser tolerant en l'escriptura i llença
 * {@link IOException} en llegir si el fitxer no compleix
 * el format esperat.</p>
 */
public class TxtResponseSerializer implements ResponseSerializer {
    private static final String RESPONSE_PREFIX = "resp";
    private static final String ANSWER_PREFIX = "ans";
    private static final String END_PREFIX = "end";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    /**
     * Escriu totes les {@link SurveyResponse} al fitxer indicat seguint el format modern.
     *
     * @param responses Llista de respostes a serialitzar.
     * @param path      Camí del fitxer de sortida.
     */
    @Override
    public void toFile(List<SurveyResponse> responses, String path) {
        if (responses == null || responses.isEmpty()) {
            throw new IllegalArgumentException("No hi ha respostes per exportar.");
        }
        if(!path.toLowerCase().endsWith(".txt")){
            path = path + ".txt";
        }
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (SurveyResponse response : responses) {
                writer.println(String.join(",",
                    RESPONSE_PREFIX,
                    response.getId(),
                    response.getSurveyId(),
                    response.getUserId(),
                    safe(response.getSubmittedAt())
                ));
                writeAnswers(writer, response);
                writer.println(END_PREFIX);
                writer.println();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error escrivint les respostes: " + e.getMessage(), e);
        }
    }

    /**
     * Llegeix totes les {@link SurveyResponse} presents al fitxer.
     *
     * @param path Camí del fitxer a llegir.
     * @return Llista de respostes reconstruïdes a partir del fitxer.
     * @throws IOException Si el fitxer és buit o malformat.
     */
    @Override
    public List<SurveyResponse> fromFile(String path) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String firstLine;
            while ((firstLine = reader.readLine()) != null && firstLine.isBlank()) {
                // Skip empty lines before actual content
            }
            if (firstLine == null) {
                throw new IOException("File is empty");
            }
            if (firstLine.startsWith(RESPONSE_PREFIX + ",")) {
                return parseModernFormat(firstLine, reader);
            }
            return Collections.singletonList(parseLegacyResponse(firstLine, reader));
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Error reading file: " + e.getMessage(), e);
        }
    }

    private void writeAnswers(PrintWriter writer, SurveyResponse response) {
        for (Answer answer : response.getAnswers()) {
            if (answer == null || answer.isEmpty()) {
                continue;
            }
            if (answer instanceof IntAnswer intAnswer) {
                writer.println(String.join(",",
                    ANSWER_PREFIX,
                    "ia",
                    String.valueOf(intAnswer.getQuestionId()),
                    String.valueOf(intAnswer.getValue())
                ));
            } else if (answer instanceof TextAnswer textAnswer) {
                writer.println(String.join(",",
                    ANSWER_PREFIX,
                    "ta",
                    String.valueOf(textAnswer.getQuestionId()),
                    encodeText(textAnswer.getValue())
                ));
            } else if (answer instanceof SingleChoiceAnswer singleChoiceAnswer) {
                writer.println(String.join(",",
                    ANSWER_PREFIX,
                    "sc",
                    String.valueOf(singleChoiceAnswer.getQuestionId()),
                    String.valueOf(singleChoiceAnswer.getOptionId())
                ));
            } else if (answer instanceof MultipleChoiceAnswer multipleChoiceAnswer) {
                writer.println(String.join(",",
                    ANSWER_PREFIX,
                    "mc",
                    String.valueOf(multipleChoiceAnswer.getQuestionId()),
                    serializeMultipleChoice(multipleChoiceAnswer)
                ));
            }
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String encodeText(String value) {
        if (value == null) {
            return "";
        }
        return BASE64_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decodeText(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            return "";
        }
        return new String(BASE64_DECODER.decode(encoded), StandardCharsets.UTF_8);
    }

    private String serializeMultipleChoice(MultipleChoiceAnswer answer) {
        StringJoiner joiner = new StringJoiner("|");
        for (Integer optionId : answer.getOptionIds()) {
            joiner.add(String.valueOf(optionId));
        }
        return joiner.toString();
    }

    private List<SurveyResponse> parseModernFormat(String firstLine, BufferedReader reader) throws IOException {
        List<SurveyResponse> responses = new ArrayList<>();
        String currentId = null;
        String currentSurveyId = null;
        String currentUserId = null;
        String currentSubmittedAt = null;
        List<Answer> currentAnswers = new ArrayList<>();

        String line = firstLine;
        while (true) {
            String content = line.trim();
            if (!content.isEmpty()) {
                if (content.startsWith(RESPONSE_PREFIX + ",")) {
                    if (currentId != null) {
                        responses.add(buildResponse(currentId, currentSurveyId, currentUserId, currentSubmittedAt, currentAnswers));
                        currentAnswers = new ArrayList<>();
                    }
                    String[] headerParts = content.split(",", 5);
                    if (headerParts.length < 5) {
                        throw new IOException("Invalid response header: " + content);
                    }
                    currentId = headerParts[1];
                    currentSurveyId = headerParts[2];
                    currentUserId = headerParts[3];
                    currentSubmittedAt = headerParts[4].isEmpty() ? null : headerParts[4];
                } else if (content.startsWith(ANSWER_PREFIX + ",")) {
                    if (currentId == null) {
                        throw new IOException("Answer found before response header.");
                    }
                    String[] parts = content.split(",", 4);
                    Answer answer = deserializeModernAnswer(parts);
                    if (answer != null) {
                        currentAnswers.add(answer);
                    }
                } else if (content.startsWith(END_PREFIX)) {
                    if (currentId != null) {
                        responses.add(buildResponse(currentId, currentSurveyId, currentUserId, currentSubmittedAt, currentAnswers));
                        currentId = null;
                        currentSurveyId = null;
                        currentUserId = null;
                        currentSubmittedAt = null;
                        currentAnswers = new ArrayList<>();
                    }
                } else {
                    throw new IOException("Unknown line: " + content);
                }
            }

            line = reader.readLine();
            if (line == null) {
                break;
            }
        }

        if (currentId != null) {
            responses.add(buildResponse(currentId, currentSurveyId, currentUserId, currentSubmittedAt, currentAnswers));
        }
        return responses;
    }

    private SurveyResponse parseLegacyResponse(String headerLine, BufferedReader reader) throws IOException {
        String[] surveyFields = headerLine.split(",", -1);
        if (surveyFields.length < 4) {
            throw new IOException("Invalid header format");
        }
        List<Answer> answers = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            String content = line.trim();
            if (content.isEmpty()) {
                continue;
            }
            String[] parts = content.split(",", 3);
            if (parts.length < 3) {
                continue;
            }
            Answer answer = deserializeLegacyAnswer(parts[0], parts[1], parts[2]);
            if (answer != null) {
                answers.add(answer);
            }
        }
        return new SurveyResponse(
            surveyFields[0],
            surveyFields[1],
            surveyFields[2],
            surveyFields[3],
            answers
        );
    }

    private SurveyResponse buildResponse(String id, String surveyId, String userId, String submittedAt, List<Answer> answers) {
        return new SurveyResponse(id, surveyId, userId, submittedAt, answers);
    }

    private Answer deserializeModernAnswer(String[] parts) throws IOException {
        if (parts.length < 4) {
            throw new IOException("Invalid answer line");
        }
        String type = parts[1];
        int questionId = Integer.parseInt(parts[2]);
        String payload = parts[3];
        try {
            return switch (type) {
                case "ia" -> new IntAnswer(questionId, Integer.parseInt(payload));
                case "mc" -> new MultipleChoiceAnswer(questionId, payload.replace('|', ','));
                case "sc" -> new SingleChoiceAnswer(questionId, Integer.parseInt(payload));
                case "ta" -> new TextAnswer(questionId, decodeText(payload));
                default -> null;
            };
        } catch (NullArgumentException | InvalidArgumentException e) {
            throw new IOException("Invalid answer entry", e);
        }
    }

    private Answer deserializeLegacyAnswer(String type, String questionIdToken, String payload) throws IOException {
        try {
            int questionId = Integer.parseInt(questionIdToken);
            return switch (type) {
                case "ia" -> new IntAnswer(questionId, Integer.parseInt(payload));
                case "mc" -> new MultipleChoiceAnswer(questionId, payload);
                case "sc" -> new SingleChoiceAnswer(questionId, Integer.parseInt(payload));
                case "ta" -> new TextAnswer(questionId, payload);
                default -> null;
            };
        } catch (Exception e) {
            throw new IOException("Invalid answer entry", e);
        }
    }
}
