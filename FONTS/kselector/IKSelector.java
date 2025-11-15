package Domain;

import Exceptions.*;

public interface IKSelector {
    int suggestK(double[][] data, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed);
}
