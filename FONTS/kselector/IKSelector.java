package kselector;

import kmeans.IClusteringAlgorithm;
import distance.Distance;

public interface IKSelector {
    int suggestK(double[][] data, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed);
}
