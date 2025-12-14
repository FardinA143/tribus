package app.controller;

import Encoder.OneHotEncoder;
import Response.SurveyResponse;
import Survey.Survey;
import app.DomainDriver;
import distance.EuclideanDistance;
import kmeans.ClusterModel;
import kmeans.KMeans;
import validation.Silhouette;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsController {
    private final KMeans kMeans;
    private final Silhouette silhouette;
    private final DomainDriver domainDriver;

    public AnalyticsController(DomainDriver domainDriver) {
        this(domainDriver, new KMeans(), new Silhouette());
    }

    public AnalyticsController(DomainDriver domainDriver, KMeans kMeans, Silhouette silhouette) {
        this.domainDriver = domainDriver;
        this.kMeans = kMeans;
        this.silhouette = silhouette;
    }

    public AnalyticsResult analyzeSurvey(Survey survey, List<SurveyResponse> responses) {
        if (survey == null) {
            throw new IllegalArgumentException("Survey cannot be null");
        }
        if (responses == null || responses.size() < 2) {
            throw new IllegalArgumentException("Se requieren al menos dos respuestas para analizar");
        }

        OneHotEncoder encoder = new OneHotEncoder();
        double[][] featureMatrix = encoder.fitTransform(survey, responses);
        int k = sanitizeClusterCount(survey.getK(), responses.size());

        ClusterModel model = kMeans.fit(featureMatrix, k, null, System.nanoTime(), 300, 1e-4);
        double[] scores = silhouette.scorePerPoint(featureMatrix, model, new EuclideanDistance());
        double avgSilhouette = Arrays.stream(scores).average().orElse(Double.NaN);

        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int label : model.getLabels()) {
            counts.merge(label, 1L, Long::sum);
        }

        return new AnalyticsResult(k, model.getInertia(), avgSilhouette, counts);
    }

    private int sanitizeClusterCount(int requestedK, int sampleSize) {
        int k = Math.max(1, requestedK);
        return Math.min(k, sampleSize);
    }
}
