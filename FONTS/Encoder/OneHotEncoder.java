package Encoder;

import Survey.*;
import Response.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació d'un codificador (Encoder) per transformar respostes d'enquestes en vectors numèrics.
 * Aquesta classe implementa la interfície IEncoder i proporciona funcionalitat per codificar diferents tipus
 * de preguntes, incloent una estratègia "Bag of Words" per a preguntes de text lliure.
 * * Estratègies de codificació:
 * - SingleChoice i MultipleChoice: Utilitzen One-Hot Encoding (1.0 si l'opció està present).
 * - OpenInt (Numèriques): Normalització min-max al rang [0, 1].
 * - OpenString (Text): Utilitza "Bag of Words" (Bossa de paraules) normalitzada per freqüència.
 * * El codificador ha de ser entrenat (fit) amb una enquesta i les seves respostes abans de poder
 * transformar noves respostes (transform).
 */
public class OneHotEncoder implements IEncoder {

    /** Llista de noms descriptius per a cada característica/dimensió del vector codificat. */
    private List<String> featureNames;
    
    /** Llista ordenada de preguntes de l'enquesta. */
    private List<Question> orderedQuestions;
    
    /** Nombre total de dimensions del vector resultant. */
    private int totalDims;

    // --- Estructures de dades per al mapatge ---

    /** Mapa per a preguntes categòriques: ID Pregunta -> (ID Opció -> Índex Vector). */
    private Map<Integer, Map<Integer, Integer>> categoricalVocab; 
    
    /** Mapa per a l'índex base de preguntes numèriques: ID Pregunta -> Índex Vector. */
    private Map<Integer, Integer> numericIndexMap;                
    
    /** Mapa de dominis (min, max) per a preguntes numèriques: ID Pregunta -> [min, max]. */
    private Map<Integer, double[]> numericDomains;                
    
    /** * Vocabulari per a preguntes de text (Bag of Words).
     * ID Pregunta -> (Paraula -> Índex relatiu dins la secció de text d'aquesta pregunta).
     */
    private Map<Integer, Map<String, Integer>> textVocab; 
    
    /** Índex d'inici en el vector global per a cada pregunta de text: ID Pregunta -> Índex absolut. */
    private Map<Integer, Integer> textIndexStartMap;

    /**
     * Constructor per defecte. Inicialitza les estructures de dades buides.
     */
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

    /**
     * Entrena el codificador amb una enquesta i el conjunt total de respostes.
     * Analitza totes les respostes per construir el vocabulari (per a text) i 
     * calcular els rangs numèrics (min/max).
     * * @param survey L'enquesta que defineix l'estructura.
     * @param allResponses Llista de totes les respostes disponibles per aprendre el domini de dades.
     */
    @Override
    public void fit(Survey survey, List<SurveyResponse> allResponses) {
        resetState();
        
        // Ordenem les preguntes per posició per garantir consistència en el vector
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

    /**
     * Transforma una llista de respostes en una matriu de vectors numèrics.
     * Genera vectors compatibles amb l'estructura apresa durant el `fit`.
     * * @param responsesToTransform Llista de respostes d'usuaris a transformar.
     * @return Matriu de doubles (N x Dims) on cada fila és el vector representatiu d'una resposta.
     * @throws IllegalStateException Si el codificador no ha estat entrenat prèviament.
     */
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
    // MÈTODES PRIVATS D'ENTRENAMENT (FIT)
    // ----------------------------------------------------------------

    /**
     * Genera el mapatge per a preguntes categòriques (Single/Multiple Choice).
     * Assigna una dimensió a cada opció possible definida a l'enquesta.
     */
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

    /**
     * Calcula el rang [min, max] per a preguntes numèriques basant-se en les respostes observades.
     * Si no hi ha respostes, s'assumeix un rang per defecte [0, 1].
     */
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
        // Evitem divisió per zero si totes les respostes són iguals o no n'hi ha
        if (!found || min >= max) { 
            if (min == Double.POSITIVE_INFINITY) min = 0;
            max = min + 1.0; 
        }

        numericDomains.put(q.getId(), new double[]{min, max});
        numericIndexMap.put(q.getId(), totalDims);
        featureNames.add("q" + q.getId() + "_num");
        totalDims++;
    }

    /**
     * Genera el vocabulari (Bag of Words) per a una pregunta de text.
     * Recull totes les paraules úniques de totes les respostes i els assigna una dimensió.
     */
    private void fitText(OpenStringQuestion q, List<SurveyResponse> responses) {
        // 1. Recopilar totes les paraules úniques (Vocabulari) d'aquesta pregunta concreta
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
        // Ordenem alfabèticament per tenir determinisme en les columnes
        List<String> sortedWords = new ArrayList<>(uniqueWords);
        Collections.sort(sortedWords);

        textIndexStartMap.put(q.getId(), totalDims);

        for (String word : sortedWords) {
            wordMap.put(word, relativeIdx);
            // Etiqueta descriptiva per a depuració: q1_word_futbol
            featureNames.add("q" + q.getId() + "_word_" + word);
            relativeIdx++;
            totalDims++;
        }
        textVocab.put(q.getId(), wordMap);
    }

    // ----------------------------------------------------------------
    // MÈTODES PRIVATS DE CODIFICACIÓ (TRANSFORM)
    // ----------------------------------------------------------------

    /**
     * Codifica una resposta Single Choice (One-Hot).
     */
    private void encodeSingleChoice(double[] row, SingleChoiceQuestion q, SingleChoiceAnswer ans) {
        Map<Integer, Integer> map = categoricalVocab.get(q.getId());
        if (map != null && map.containsKey(ans.getOptionId())) {
            row[map.get(ans.getOptionId())] = 1.0;
        }
    }

    /**
     * Codifica una resposta Multiple Choice (Multi-Hot).
     */
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

    /**
     * Codifica una resposta numèrica normalitzant-la al rang [0, 1].
     */
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

    /**
     * Codifica una resposta de text utilitzant freqüència de termes (Term Frequency).
     * Normalitza pel nombre total de paraules vàlides en la resposta per evitar biaix per longitud.
     */
    private void encodeText(double[] row, OpenStringQuestion q, TextAnswer ans) {
        Map<String, Integer> wordMap = textVocab.get(q.getId());
        Integer startIndex = textIndexStartMap.get(q.getId());

        if (wordMap == null || startIndex == null) return;

        String[] words = tokenize(ans.getValue());
        if (words.length == 0) return;

        // Comptem freqüència de cada paraula en aquesta resposta específica
        Map<Integer, Double> tempCounts = new HashMap<>();
        for (String w : words) {
            if (wordMap.containsKey(w)) {
                int absIndex = startIndex + wordMap.get(w);
                tempCounts.put(absIndex, tempCounts.getOrDefault(absIndex, 0.0) + 1.0);
            }
        }

        // Normalització (freqüència relativa): count / total_paraules
        double totalValidWords = 0;
        for(double c : tempCounts.values()) totalValidWords += c;

        if (totalValidWords > 0) {
            for (Map.Entry<Integer, Double> entry : tempCounts.entrySet()) {
                row[entry.getKey()] = entry.getValue() / totalValidWords;
            }
        }
    }

    // ----------------------------------------------------------------
    // MÈTODES AUXILIARS
    // ----------------------------------------------------------------

    /**
     * Reinicia l'estat intern del codificador per a un nou entrenament.
     */
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

    /**
     * Converteix la llista de respostes d'un SurveyResponse en un Mapa per accés ràpid per ID.
     */
    private Map<Integer, Answer> mapAnswers(SurveyResponse res) {
        return res.getAnswers().stream()
                .collect(Collectors.toMap(Answer::getQuestionId, a -> a));
    }

    /**
     * Cerca una resposta específica dins d'un SurveyResponse de forma segura.
     */
    private Answer getAnswerForQuestion(SurveyResponse res, int qId) {
        return res.getAnswers().stream()
                .filter(a -> a.getQuestionId() == qId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Tokenitzador simple: converteix a minúscules, elimina puntuació i símbols,
     * i filtra paraules massa curtes (stop words simples).
     * * @param text El text cru a processar.
     * @return Array de tokens (paraules) netes.
     */
    private String[] tokenize(String text) {
        if (text == null) return new String[0];
        // Normalització: minúscules i reemplaçar tot el que no sigui lletra/número per espai
        // Regex: manté a-z, 0-9 i accents comuns (à-ú, ñ, ç)
        String clean = text.toLowerCase().replaceAll("[^a-z0-9à-úñç]", " ");
        
        return Arrays.stream(clean.split("\\s+"))
                .filter(s -> !s.isEmpty() && s.length() > 2) // Filtre de paraules molt curtes (el, la, de...)
                .toArray(String[]::new);
    }

    /**
     * Obté la llista de noms de les característiques del vector.
     * @return Llista immutable de Strings.
     */
    @Override
    public List<String> getFeatureNames() {
        return Collections.unmodifiableList(featureNames);
    }
}