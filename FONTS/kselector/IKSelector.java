package FONTS.kselector;

import FONTS.kmeans.ClusterModel;
import FONTS.kmeans.IClusteringAlgorithm;
import FONTS.distance.Distance;

public interface IKSelector {
    int suggestK(double[][] data, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed);
}
