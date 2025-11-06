package FONTS.kmeans;

public final class ClusterModel {
    private final double[][] centroids;
    private final int[] labels;
    private final double inertia;
    private final int iterations;

    public ClusterModel(double[][] centroids, int[] labels, double inertia, int iterations) {
        this.centroids = centroids;
        this.labels = labels;
        this.inertia = inertia;
        this.iterations = iterations;
    }
    
    public double[][] getCentroids() {
        return centroids;
    }

    public int[] getLabels() {
        return labels;
    }

    public double getInertia() {
        return inertia;
    }

    public int getIterations() {
        return iterations;
    }
}
