package app.controller;

import Encoder.OneHotEncoder;
import Response.SurveyResponse;
import Survey.AlgorithmConfiguration;
import Survey.Survey;
import distance.Distance;
import distance.EuclideanDistance;
import kmeans.ClusterModel;
import kmeans.IClusteringAlgorithm;
import kmeans.KMeans;
import validation.Silhouette;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AnalyticsController {
    private final IClusteringAlgorithm defaultAlgorithm;
    private final Silhouette silhouette;

    public AnalyticsController() {
        this(new KMeans(), new Silhouette());
    }

    public AnalyticsController(IClusteringAlgorithm algorithm, Silhouette silhouette) {
        this.defaultAlgorithm = algorithm;
        this.silhouette = silhouette;
    }

    public AnalyticsResult analyzeSurvey(Survey survey, List<SurveyResponse> responses) {
        if (survey == null) {
            throw new IllegalArgumentException("Survey cannot be null");
        }
        if (responses == null || responses.size() < 2) {
            throw new IllegalArgumentException("Calen com a mínim dues respostes per analitzar");
        }

        OneHotEncoder encoder = new OneHotEncoder();
        double[][] featureMatrix = encoder.fitTransform(survey, responses);
        int k = sanitizeClusterCount(survey.getK(), responses.size());

        AlgorithmConfiguration config = AlgorithmConfiguration.fromSurvey(survey);
        IClusteringAlgorithm algorithm = config.buildAlgorithm();
        Distance distance = config.buildDistance();
        if (algorithm == null) {
            algorithm = defaultAlgorithm;
        }
        if (distance == null) {
            distance = new EuclideanDistance();
        }

        ClusterModel model = algorithm.fit(featureMatrix, k, distance, System.nanoTime(), 300, 1e-4);
        double[] scores = silhouette.scorePerPoint(featureMatrix, model, distance);
        double avgSilhouette = Arrays.stream(scores).average().orElse(Double.NaN);

        Map<Integer, Long> counts = new LinkedHashMap<>();
        for (int label : model.getLabels()) {
            counts.merge(label, 1L, Long::sum);
        }

        // Build 2D projection for UI visualization.
        // Using the first two one-hot features produces mostly 0/1 values (overlaps).
        // Instead, use a deterministic random projection with mean-centering.
        int n = featureMatrix.length;
        int dims = (n > 0 && featureMatrix[0] != null) ? featureMatrix[0].length : 0;
        double[][] points2d;
        double[][] centroids2d;
        if (dims <= 0) {
            points2d = new double[n][2];
            for (int i = 0; i < n; i++) {
                points2d[i][0] = (double) i;
                points2d[i][1] = 0.0;
            }
            centroids2d = null;
        } else {
            long seed = (long) (survey.getId() == null ? 0 : survey.getId().hashCode());
            Projection2D proj = projectTo2D(featureMatrix, seed);
            points2d = proj.points;

            double[][] centroids = model.getCentroids();
            centroids2d = projectCentroidsTo2D(centroids, proj);
        }

        String[] responseIds = new String[responses.size()];
        for (int i = 0; i < responses.size(); i++) {
            SurveyResponse r = responses.get(i);
            responseIds[i] = r == null ? "" : r.getId();
        }

        return new AnalyticsResult(k, model.getInertia(), avgSilhouette, counts,
                responseIds, model.getLabels(), points2d, centroids2d);
    }

    private static class Projection2D {
        final double[] mean;
        final double[] r1;
        final double[] r2;
        final double scale;
        final double[][] points;

        private Projection2D(double[] mean, double[] r1, double[] r2, double scale, double[][] points) {
            this.mean = mean;
            this.r1 = r1;
            this.r2 = r2;
            this.scale = scale;
            this.points = points;
        }
    }

    private Projection2D projectTo2D(double[][] X, long seed) {
        int n = X.length;
        int dims = (n > 0 && X[0] != null) ? X[0].length : 0;
        double[] mean = new double[dims];
        int usedRows = 0;
        for (double[] row : X) {
            if (row == null || row.length != dims) continue;
            for (int j = 0; j < dims; j++) mean[j] += row[j];
            usedRows++;
        }
        if (usedRows > 0) {
            for (int j = 0; j < dims; j++) mean[j] /= usedRows;
        }

        Random rng = new Random(seed);
        double[] r1 = new double[dims];
        double[] r2 = new double[dims];
        for (int j = 0; j < dims; j++) {
            r1[j] = rng.nextGaussian();
            r2[j] = rng.nextGaussian();
        }

        double scale = 1.0 / Math.sqrt(Math.max(1, dims));
        double[][] pts = new double[n][2];
        for (int i = 0; i < n; i++) {
            double[] row = X[i];
            if (row == null || row.length != dims) {
                pts[i][0] = (double) i;
                pts[i][1] = 0.0;
                continue;
            }
            double x = 0.0;
            double y = 0.0;
            for (int j = 0; j < dims; j++) {
                double v = row[j] - mean[j];
                x += v * r1[j];
                y += v * r2[j];
            }
            pts[i][0] = x * scale;
            pts[i][1] = y * scale;
        }

        return new Projection2D(mean, r1, r2, scale, pts);
    }

    private double[][] projectCentroidsTo2D(double[][] centroids, Projection2D proj) {
        if (centroids == null) return null;
        int k = centroids.length;
        int dims = proj.mean.length;
        double[][] out = new double[k][2];
        for (int i = 0; i < k; i++) {
            double[] row = centroids[i];
            if (row == null || row.length != dims) {
                out[i][0] = 0.0;
                out[i][1] = 0.0;
                continue;
            }
            double x = 0.0;
            double y = 0.0;
            for (int j = 0; j < dims; j++) {
                double v = row[j] - proj.mean[j];
                x += v * proj.r1[j];
                y += v * proj.r2[j];
            }
            out[i][0] = x * proj.scale;
            out[i][1] = y * proj.scale;
        }
        return out;
    }

    private int sanitizeClusterCount(int requestedK, int sampleSize) {
        int k = Math.max(1, requestedK);
        return Math.min(k, sampleSize);
    }
}
