package FONTS.validation;

import FONTS.kmeans.ClusterModel;
import FONTS.distance.Distance;
import FONTS.distance.EuclideanDistance;

public class Silhouette implements IClusterValidation {
    @Override
    public double[] scorePerPoint(double[][] X, ClusterModel model, Distance dist) {
        if (dist == null) dist = new EuclideanDistance();
        int n = X.length, k = model.getCentroids().length;
        int[] lab = model.getLabels();
        double[] s = new double[n];

        //pre-agrupa
        int[] counts = new int[k];
        for (int l : lab) counts[l]++;

        for (int i = 0; i < n; i++) {
            int ci = lab[i];
            //a(i): distancia media a su cluster
            double a = 0;
            int ca = 0;
            for (int j = 0; j < n; j++)
                if (lab[j] == ci && i != j) {
                    a += dist.between(X[i], X[j]);
                    ca++;
                }
            a = (ca == 0) ? 0 : a/ca;

            //b(i): mínima distancia media a otro cluster
            double b = Double.POSITIVE_INFINITY;
            for (int c = 0; c < k; c++)
                if (c != ci && counts[c] > 0) {
                    double sum = 0;
                    int cb = 0;
                    for (int j = 0; j < n; j++)
                        if (lab[j] == c) {
                            sum += dist.between(X[i], X[j]);
                            cb++;
                        }
                    double mean = sum/cb;
                    if (mean < b) b = mean;
                }
            s[i] = (b == 0 && a == 0) ? 0 : (b-a)/Math.max(a, b);
        }
        return s;
    }
}
