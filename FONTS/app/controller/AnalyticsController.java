package app.controller;

import Encoder.OneHotEncoder;
import Response.Answer;
import Response.IntAnswer;
import Response.MultipleChoiceAnswer;
import Response.SingleChoiceAnswer;
import Response.SurveyResponse;
import Response.TextAnswer;
import Survey.Question;
import Survey.Survey;
import distance.EuclideanDistance;
import kmeans.ClusterModel;
import kmeans.KMeans;
import validation.Silhouette;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AnalyticsController {
    private final KMeans kMeans;
    private final Silhouette silhouette;

    public AnalyticsController() {
        this(new KMeans(), new Silhouette());
    }

    public AnalyticsController(KMeans kMeans, Silhouette silhouette) {
        this.kMeans = kMeans;
        this.silhouette = silhouette;
    }

    public AnalyticsResult analyzeSurvey(Survey survey, List<SurveyResponse> responses) {
        if (responses == null || responses.size() < 2) {
            throw new IllegalArgumentException("Se requieren al menos dos respuestas para analizar");
        }
        List<List<Object>> rawRows = responses.stream()
            .map(r -> buildFeatureRow(survey, r))
            .collect(Collectors.toList());
        OneHotEncoder encoder = new OneHotEncoder();
        double[][] X = encoder.fitTransform(rawRows);
        int k = Math.min(Math.max(1, survey.getK()), X.length);
        if (k > X.length) {
            k = X.length;
        }
        ClusterModel model = kMeans.fit(X, k, null, System.nanoTime(), 300, 1e-4);
        double[] scores = silhouette.scorePerPoint(X, model, new EuclideanDistance());
        double avgSilhouette = Arrays.stream(scores).average().orElse(Double.NaN);

        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int label : model.getLabels()) {
            counts.merge(label, 1L, Long::sum);
        }
        return new AnalyticsResult(k, model.getInertia(), avgSilhouette, counts);
    }

    private List<Object> buildFeatureRow(Survey survey, SurveyResponse response) {
        Map<Integer, Answer> answersByQuestion = new LinkedHashMap<>();
        for (Answer answer : response.getAnswers()) {
            answersByQuestion.put(answer.getQuestionId(), answer);
        }
        List<Object> row = new ArrayList<>();
        for (Question question : survey.getQuestions()) {
            row.add(mapAnswerValue(answersByQuestion.get(question.getId())));
        }
        return row;
    }

    private Object mapAnswerValue(Answer answer) {
        if (answer == null) return null;
        if (answer instanceof TextAnswer text) {
            return text.getValue();
        }
        if (answer instanceof IntAnswer num) {
            return num.getValue();
        }
        if (answer instanceof SingleChoiceAnswer single) {
            return "choice:" + single.getOptionId();
        }
        if (answer instanceof MultipleChoiceAnswer multi) {
            return "multi:" + multi.getOptionIds().stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining("|"));
        }
        return null;
    }
}
