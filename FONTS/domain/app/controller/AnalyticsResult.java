package app.controller;

import java.util.Collections;
import java.util.Map;

public class AnalyticsResult {
    private final int clusters;
    private final double inertia;
    private final double averageSilhouette;
    private final Map<Integer, Long> clusterCounts;

    public AnalyticsResult(int clusters, double inertia, double averageSilhouette, Map<Integer, Long> clusterCounts) {
        this.clusters = clusters;
        this.inertia = inertia;
        this.averageSilhouette = averageSilhouette;
        this.clusterCounts = Collections.unmodifiableMap(clusterCounts);
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
}
