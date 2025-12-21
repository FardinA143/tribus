package app.controller;

import java.util.Collections;
import java.util.Map;

public class AnalyticsResult {
    private final int clusters;
    private final double inertia;
    private final double averageSilhouette;
    private final Map<Integer, Long> clusterCounts;

    // Optional: 2D projection for visualization
    private final String[] responseIds;
    private final int[] labels;
    private final double[][] points2d;
    private final double[][] centroids2d;

    public AnalyticsResult(int clusters, double inertia, double averageSilhouette, Map<Integer, Long> clusterCounts,
                           String[] responseIds, int[] labels, double[][] points2d, double[][] centroids2d) {
        this.clusters = clusters;
        this.inertia = inertia;
        this.averageSilhouette = averageSilhouette;
        this.clusterCounts = Collections.unmodifiableMap(clusterCounts);
        this.responseIds = responseIds;
        this.labels = labels;
        this.points2d = points2d;
        this.centroids2d = centroids2d;
    }

    public AnalyticsResult(int clusters, double inertia, double averageSilhouette, Map<Integer, Long> clusterCounts) {
        this(clusters, inertia, averageSilhouette, clusterCounts, null, null, null, null);
    }

    public int getClusters() {
        return clusters;
    }

    public double getInertia() {
        return inertia;
    }

    public double getAverageSilhouette() {
        return averageSilhouette;
    }

    public Map<Integer, Long> getClusterCounts() {
        return clusterCounts;
    }

    public String[] getResponseIds() {
        return responseIds;
    }

    public int[] getLabels() {
        return labels;
    }

    public double[][] getPoints2d() {
        return points2d;
    }

    public double[][] getCentroids2d() {
        return centroids2d;
    }
}
