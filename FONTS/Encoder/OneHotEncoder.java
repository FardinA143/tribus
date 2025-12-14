package Encoder;

import Survey.*;
import Response.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació refactoritzada d'un codificador per transformar respostes en vectors.
 * Incorpora estratègia "Bag of Words" per a text i estructura modular per evitar disseny monolític.
 */
public class OneHotEncoder implements IEncoder {

    // Metadata general
    private List<String> featureNames;
    private List<Question> orderedQuestions;
    private int totalDims;

    // Estructures específiques per tipus de dada (Separem la lògica)
    private Map<Integer, Map<Integer, Integer>> categoricalVocab; // ID Pregunta -> (ID Opció -> Índex Vector)
    private Map<Integer, Integer> numericIndexMap;                // ID Pregunta -> Índex Vector
    private Map<Integer, double[]> numericDomains;                // ID Pregunta -> [min, max]
    
    // NOU: Estructures per a text (Bag of Words)
    // ID Pregunta -> (Paraula -> Índex relatiu dins la secció de text d'aquesta pregunta)
    private Map<Integer, Map<String, Integer>> textVocab; 
    // ID Pregunta -> Índex d'inici en el vector global
    private Map<Integer, Integer> textIndexStartMap;

    public OneHotEncoder() {
        this.featureNames = new ArrayList<>();
        this.orderedQuestions = new ArrayList<>();
        this.categoricalVocab = new HashMap<>();
        this.numericIndexMap = new HashMap<>();
        this.numericDomains = new HashMap<>();
        this.textVocab = new HashMap<>();
        this.textIndexStartMap = new HashMap<>();
        this.totalDims = 0;
    }

    @Override
    public void fit(Survey survey, List<SurveyResponse> allResponses) {
        resetState();
        
        // Ordenem preguntes per mantenir consistència
        this.orderedQuestions = survey.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getPosition))
                .collect(Collectors.toList());

        for (Question q : this.orderedQuestions) {
            if (q instanceof SingleChoiceQuestion) {
                fitCategorical((SingleChoiceQuestion) q);
            } else if (q instanceof MultipleChoiceQuestion) {
                fitCategorical((MultipleChoiceQuestion) q);
            } else if (q instanceof OpenIntQuestion) {
                fitNumeric((OpenIntQuestion) q, allResponses);
            } else if (q instanceof OpenStringQuestion) {
                fitText((OpenStringQuestion) q, allResponses);
            }
        }
    }

    @Override
    public double[][] transform(List<SurveyResponse> responsesToTransform) {
        if (totalDims == 0 && !orderedQuestions.isEmpty()) {
            throw new IllegalStateException("Encoder has not been fitted. Call fit() first.");
        }

        double[][] X = new double[responsesToTransform.size()][totalDims];

        for (int i = 0; i < responsesToTransform.size(); i++) {
            SurveyResponse res = responsesToTransform.get(i);
            Map<Integer, Answer> answerMap = mapAnswers(res);

            for (Question q : this.orderedQuestions) {
                Answer ans = answerMap.get(q.getId());
                if (ans == null || ans.isEmpty()) continue;

                if (q instanceof SingleChoiceQuestion) {
                    encodeSingleChoice(X[i], (SingleChoiceQuestion) q, (SingleChoiceAnswer) ans);
                } else if (q instanceof MultipleChoiceQuestion) {
                    encodeMultiChoice(X[i], (MultipleChoiceQuestion) q, (MultipleChoiceAnswer) ans);
                } else if (q instanceof OpenIntQuestion) {
                    encodeNumeric(X[i], (OpenIntQuestion) q, (IntAnswer) ans);
                } else if (q instanceof OpenStringQuestion) {
                    encodeText(X[i], (OpenStringQuestion) q, (TextAnswer) ans);
                }
            }
        }
        return X;
    }

    // ----------------------------------------------------------------
    // MÈTODES PRIVATS D'AJUDA (FIT) - Trenquem el monolito
    // ----------------------------------------------------------------

    private void fitCategorical(Question q) {
        Map<Integer, Integer> optionMap = new HashMap<>();
        Collection<ChoiceOption> options = (q instanceof SingleChoiceQuestion) 
            ? ((SingleChoiceQuestion) q).getOptions() 
            : ((MultipleChoiceQuestion) q).getOptions();

        for (ChoiceOption opt : options) {
            optionMap.put(opt.getId(), totalDims);
            featureNames.add("q" + q.getId() + "_opt" + opt.getId());
            totalDims++;
        }
        categoricalVocab.put(q.getId(), optionMap);
    }

    private void fitNumeric(OpenIntQuestion q, List<SurveyResponse> responses) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        boolean found = false;

        for (SurveyResponse res : responses) {
            Answer ans = getAnswerForQuestion(res, q.getId());
            if (ans instanceof IntAnswer ia) {
                double val = ia.getValue();
                if (val < min) min = val;
                if (val > max) max = val;
                found = true;
            }
        }
        if (!found || min > max) { min = 0; max = 1; } // Evitar divisió per zero

        numericDomains.put(q.getId(), new double[]{min, max});
        numericIndexMap.put(q.getId(), totalDims);
        featureNames.add("q" + q.getId() + "_num");
        totalDims++;
    }

    private void fitText(OpenStringQuestion q, List<SurveyResponse> responses) {
        // 1. Recopilar totes les paraules úniques (Vocabulari) d'aquesta pregunta
        Set<String> uniqueWords = new HashSet<>();
        
        for (SurveyResponse res : responses) {
            Answer ans = getAnswerForQuestion(res, q.getId());
            if (ans instanceof TextAnswer ta) {
                String[] words = tokenize(ta.getValue());
                Collections.addAll(uniqueWords, words);
            }
        }

        // 2. Mapejar paraula -> index relatiu
        Map<String, Integer> wordMap = new HashMap<>();
        int relativeIdx = 0;
        // Ordenem per tenir determinisme
        List<String> sortedWords = new ArrayList<>(uniqueWords);
        Collections.sort(sortedWords);

        textIndexStartMap.put(q.getId(), totalDims);

        for (String word : sortedWords) {
            wordMap.put(word, relativeIdx);
            featureNames.add("q" + q.getId() + "_word_" + word);
            relativeIdx++;
            totalDims++;
        }
        textVocab.put(q.getId(), wordMap);
    }

    // ----------------------------------------------------------------
    // MÈTODES PRIVATS D'AJUDA (TRANSFORM)
    // ----------------------------------------------------------------

    private void encodeSingleChoice(double[] row, SingleChoiceQuestion q, SingleChoiceAnswer ans) {
        Map<Integer, Integer> map = categoricalVocab.get(q.getId());
        if (map != null && map.containsKey(ans.getOptionId())) {
            row[map.get(ans.getOptionId())] = 1.0;
        }
    }

    private void encodeMultiChoice(double[] row, MultipleChoiceQuestion q, MultipleChoiceAnswer ans) {
        Map<Integer, Integer> map = categoricalVocab.get(q.getId());
        if (map != null) {
            for (int optId : ans.getOptionIds()) {
                if (map.containsKey(optId)) {
                    row[map.get(optId)] = 1.0;
                }
            }
        }
    }

    private void encodeNumeric(double[] row, OpenIntQuestion q, IntAnswer ans) {
        Integer idx = numericIndexMap.get(q.getId());
        if (idx != null) {
            double[] range = numericDomains.get(q.getId());
            double min = range[0];
            double max = range[1];
            double dist = max - min;
            double val = (dist < 1e-9) ? 0.0 : (ans.getValue() - min) / dist;
            row[idx] = Math.max(0.0, Math.min(1.0, val));
        }
    }

    private void encodeText(double[] row, OpenStringQuestion q, TextAnswer ans) {
        // Implementació Bag of Words Normalitzada
        Map<String, Integer> wordMap = textVocab.get(q.getId());
        Integer startIndex = textIndexStartMap.get(q.getId());

        if (wordMap == null || startIndex == null) return;

        String[] words = tokenize(ans.getValue());
        if (words.length == 0) return;

        // Comptem freqüència de paraules en aquesta resposta
        Map<Integer, Double> tempCounts = new HashMap<>();
        for (String w : words) {
            if (wordMap.containsKey(w)) {
                int absIndex = startIndex + wordMap.get(w);
                tempCounts.put(absIndex, tempCounts.getOrDefault(absIndex, 0.0) + 1.0);
            }
        }

        // Normalització (perquè textos llargs no pesin més que curts)
        // Dividim cada recompte pel total de paraules vàlides trobades (Term Frequency)
        double totalValidWords = 0;
        for(double c : tempCounts.values()) totalValidWords += c;

        if (totalValidWords > 0) {
            for (Map.Entry<Integer, Double> entry : tempCounts.entrySet()) {
                row[entry.getKey()] = entry.getValue() / totalValidWords;
            }
        }
    }

    // ----------------------------------------------------------------
    // UTILITATS
    // ----------------------------------------------------------------

    private void resetState() {
        featureNames.clear();
        orderedQuestions.clear();
        categoricalVocab.clear();
        numericIndexMap.clear();
        numericDomains.clear();
        textVocab.clear();
        textIndexStartMap.clear();
        totalDims = 0;
    }

    private Map<Integer, Answer> mapAnswers(SurveyResponse res) {
        return res.getAnswers().stream()
                .collect(Collectors.toMap(Answer::getQuestionId, a -> a));
    }

    private Answer getAnswerForQuestion(SurveyResponse res, int qId) {
        return res.getAnswers().stream()
                .filter(a -> a.getQuestionId() == qId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Tokenitzador simple: minúscules, elimina puntuació bàsica i divideix per espais.
     */
    private String[] tokenize(String text) {
        if (text == null) return new String[0];
        // Normalització bàsica: minúscules i reemplaçar tot el que no sigui lletra/número per espai
        String clean = text.toLowerCase().replaceAll("[^a-z0-9à-úñç]", " ");
        return Arrays.stream(clean.split("\\s+"))
                .filter(s -> !s.isEmpty() && s.length() > 2) // Opcional: Ignorar paraules molt curtes (stop words cutres)
                .toArray(String[]::new);
    }

    @Override
    public List<String> getFeatureNames() {
        return Collections.unmodifiableList(featureNames);
    }
}