package Encoder;

import Exceptions.*;
import Survey.*;
import Response.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementació d'un codificador One-Hot per transformar respostes d'enquestes en vectors numèrics.
 * Aquesta classe implementa la interfície IEncoder i proporciona funcionalitat per codificar diferents tipus
 * de preguntes: SingleChoice utilitzant One-Hot Encoding, MultipleChoice utilitzant Multi-Hot Encoding,
 * preguntes numèriques (OpenInt) normalitzades al rang [0, 1], i preguntes de text (OpenString)
 * codificades segons la seva longitud normalitzada.
 * 
 * El codificador ha de ser entrenat (fit) amb una enquesta i les seves respostes abans de poder
 * transformar noves respostes en vectors numèrics.
 */
public class OneHotEncoder implements IEncoder {
    
    /**
     * Llista de noms descriptius per a cada característica/dimensió del vector codificat.
     */
    private List<String> featureNames;
    
    /**
     * Llista ordenada de preguntes de l'enquesta, ordenades per posició.
     */
    private List<Question> orderedQuestions;
    
    /**
     * Mapatge de vocabulari per a preguntes categòriques (SingleChoice i MultipleChoice).
     * La clau externa és l'ID de la pregunta, i el mapa intern associa cada ID d'opció
     * amb el seu índex en el vector de característiques.
     */
    private Map<Integer, Map<Integer, Integer>> categoricalVocab; 
    
    /**
     * Mapatge de preguntes numèriques (OpenInt i OpenString) al seu índex en el vector de característiques.
     */
    private Map<Integer, Integer> numericFeatureMap;

    /**
     * Mapatge de dominis per a preguntes numèriques. 
     * Emmagatzema els valors [mínim, màxim] observats per a cada pregunta numèrica durant el fit.
     */
    private Map<Integer, double[]> numericDomains;

    /**
     * Nombre total de dimensions/característiques en el vector codificat.
     */
    private int totalDims;

    /**
     * Constructor per defecte que inicialitza totes les estructures de dades internes.
     */
    public OneHotEncoder() {
        this.featureNames = new ArrayList<>();
        this.orderedQuestions = new ArrayList<>();
        this.categoricalVocab = new HashMap<>();
        this.numericFeatureMap = new HashMap<>();
        this.numericDomains = new HashMap<>();
        this.totalDims = 0;
    }

    /**
     * Entrena el codificador amb una enquesta i les seves respostes.
     * Analitza l'estructura de l'enquesta i les respostes per crear el vocabulari
     * d'opcions per a preguntes categòriques, calcular els rangs [min, max] per a
     * preguntes numèriques, i assignar índexs a cada característica en el vector resultant.
     * Després de cridar aquest mètode, el codificador està preparat per transformar respostes.
     * 
     * @param survey L'enquesta que defineix l'estructura de les preguntes.
     * @param allResponses Llista de totes les respostes a l'enquesta, utilitzades per calcular
     * els dominis de les preguntes obertes.
     */
    @Override
    public void fit(Survey survey, List<SurveyResponse> allResponses) {
        featureNames.clear();
        orderedQuestions.clear();
        categoricalVocab.clear();
        numericFeatureMap.clear();
        numericDomains.clear();
        totalDims = 0;

        this.orderedQuestions = survey.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getPosition))
                .collect(Collectors.toList());

        for (Question q : this.orderedQuestions) {
            
            if (q instanceof SingleChoiceQuestion scq) {
                Map<Integer, Integer> optionMap = new HashMap<>();
                for (ChoiceOption opt : scq.getOptions()) {
                    optionMap.put(opt.getId(), totalDims);
                    featureNames.add("q" + q.getId() + "_opt" + opt.getId());
                    totalDims++;
                }
                categoricalVocab.put(q.getId(), optionMap);
            
            }
            else if (q instanceof MultipleChoiceQuestion mcq) {
                Map<Integer, Integer> optionMap = new HashMap<>();
                for (ChoiceOption opt : mcq.getOptions()) {
                    optionMap.put(opt.getId(), totalDims);
                    featureNames.add("q" + q.getId() + "_opt" + opt.getId());
                    totalDims++;
                }
                categoricalVocab.put(q.getId(), optionMap);

            }
            else if (q instanceof OpenIntQuestion oiq) {
                double min = Double.POSITIVE_INFINITY;
                double max = Double.NEGATIVE_INFINITY;
                for (SurveyResponse res : allResponses) {
                    for (Answer ans : res.getAnswers()) { 
                        if (ans.getQuestionId() == q.getId() && ans instanceof IntAnswer ia) {
                            if (ia.getValue() < min) min = ia.getValue();
                            if (ia.getValue() > max) max = ia.getValue();
                        }
                    }
                }
                if (min > max) { min = 0; max = 0; } 
                numericDomains.put(q.getId(), new double[]{min, max});

                numericFeatureMap.put(q.getId(), totalDims);
                featureNames.add("q" + q.getId() + "_val");
                totalDims++;

            }
            else if (q instanceof OpenStringQuestion) {
                double maxLen = 280;
                numericDomains.put(q.getId(), new double[]{0, maxLen});
                
                numericFeatureMap.put(q.getId(), totalDims);
                featureNames.add("q" + q.getId() + "_len");
                totalDims++;
            }
        }
    }

    /**
     * Transforma una llista de respostes a enquesta en una matriu de vectors numèrics.
     * Cada resposta es converteix en un vector de dimensió totalDims. Les preguntes SingleChoice
     * generen un 1.0 a la posició de l'opció triada, les MultipleChoice generen un 1.0 a cada
     * posició de les opcions triades, les OpenInt es normalitzen al rang [0, 1] segons el domini
     * observat en fit, i les OpenString es codifiquen segons la seva longitud normalitzada.
     * Les respostes buides o nul·les es codifiquen com 0.0 en totes les seves dimensions.
     * 
     * @param responsesToTransform Llista de respostes a transformar.
     * @return Matriu on cada fila és el vector codificat d'una resposta.
     * @throws IllegalStateException si el codificador no ha estat entrenat prèviament amb fit().
     */
    @Override
    public double[][] transform(List<SurveyResponse> responsesToTransform) {
        if (totalDims == 0) {
            throw new IllegalStateException("Encoder has not been fitted. Call fit() first.");
        }

        double[][] X = new double[responsesToTransform.size()][totalDims];
        
        for (int i = 0; i < responsesToTransform.size(); i++) {
            SurveyResponse res = responsesToTransform.get(i);
            
            Map<Integer, Answer> answerMap = res.getAnswers().stream()
                    .collect(Collectors.toMap(Answer::getQuestionId, ans -> ans));
            
            for (Question q : this.orderedQuestions) {
                Answer ans = answerMap.get(q.getId());
                
                if (ans == null || ans.isEmpty()) { 
                    continue;
                }

                if (q instanceof SingleChoiceQuestion) {
                    Map<Integer, Integer> optionMap = categoricalVocab.get(q.getId());
                    if (optionMap != null && ans instanceof SingleChoiceAnswer sca) {
                        Integer featureIndex = optionMap.get(sca.getOptionId());
                        if (featureIndex != null) {
                            X[i][featureIndex] = 1.0;
                        }
                    }
                }
                else if (q instanceof MultipleChoiceQuestion) {
                    Map<Integer, Integer> optionMap = categoricalVocab.get(q.getId());
                    if (optionMap != null && ans instanceof MultipleChoiceAnswer mca) {
                        for (int optId : mca.getOptionIds()) { 
                            Integer featureIndex = optionMap.get(optId);
                            if (featureIndex != null) {
                                X[i][featureIndex] = 1.0;
                            }
                        }
                    }
                }
                else if (q instanceof OpenIntQuestion) {
                    Integer featureIndex = numericFeatureMap.get(q.getId());
                    if (featureIndex != null && ans instanceof IntAnswer ia) {
                        double[] domain = numericDomains.get(q.getId());
                        double min = domain[0];
                        double max = domain[1];
                        double range = (max - min);
                        
                        if (range <= 1e-9) {
                            X[i][featureIndex] = (ia.getValue() >= min) ? 1.0 : 0.0;
                        }
                        else {
                            double normalized = (ia.getValue() - min) / range;
                            X[i][featureIndex] = Math.max(0.0, Math.min(1.0, normalized));
                        }
                    }
                }
                else if (q instanceof OpenStringQuestion) {
                    Integer featureIndex = numericFeatureMap.get(q.getId());
                    if (featureIndex != null && ans instanceof TextAnswer ta) {
                        double[] domain = numericDomains.get(q.getId());
                        double maxLen = domain[1];
                        if (maxLen > 0) {
                            double normalized = ta.getValue().length() / maxLen;
                            X[i][featureIndex] = Math.min(normalized, 1.0);
                        }
                    }
                }
            }
        }
        return X;
    }

    /**
     * Obté la llista de noms descriptius per a cada característica del vector codificat.
     * Els noms segueixen el format: "q<questionId>_opt<optionId>" per a opcions de preguntes
     * categòriques, "q<questionId>_val" per a valors numèrics de OpenInt, i "q<questionId>_len"
     * per a longituds de OpenString.
     * 
     * @return Llista immutable de noms de característiques, en el mateix ordre que les dimensions del vector.
     */
    @Override
    public List<String> getFeatureNames() {
        return Collections.unmodifiableList(featureNames);
    }
}