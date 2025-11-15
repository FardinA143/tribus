package Domain;

import Exceptions.*;

public interface IClusterValidation {
    double[] scorePerPoint(double[][] data, ClusterModel model, Distance dist);
    default double average(double[][] data, ClusterModel model, Distance dist) {
        double[] s = scorePerPoint(data, model, dist);
        double m = 0;
        for (double v : s) m += v;
        return m/s.length;
    }
}
