package FONTS.kmeans;

import FONTS.distance.Distance;

public interface IClusteringAlgorithm {
    ClusterModel fit(double[][] data, int k, Distance distance, long seed, int maxIter, double tol);
    default ClusterModel fit(double[][] data, int k) {
        return fit(data, k, null, System.nanoTime(), 300, 1e-4);
    }
}
