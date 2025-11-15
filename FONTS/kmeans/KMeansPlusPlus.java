package kmeans;

import distance.Distance;
import distance.EuclideanDistance;
import java.util.*;

public class KMeansPlusPlus extends KMeans {
    @Override
    public ClusterModel fit(double[][] X, int k, Distance dist, long seed, int maxIter, double tol) {
        if (dist == null) dist = new EuclideanDistance();
        final Random rnd = new Random(seed);
        final int n = X.length, d = X[0].length;

        //K++ initialization
        double[][] C = new double[k][d];
        int first = rnd.nextInt(n);
        C[0] = Arrays.copyOf(X[first], d);

        double[] d2 = new double[n]; //squared distance to nearest centroid
        Arrays.fill(d2, Double.POSITIVE_INFINITY);

        for (int c = 1; c < k; c++) {
            for (int i = 0; i < n; i++) {
                double di = dist.between(X[i], C[0]);
                for (int j = 1; j < c; j++) di = Math.min(di, dist.between(X[i], C[j]));
                d2[i] = di*di;
            }
            double sum = 0;
            for (double v : d2) sum += v;
            double r = rnd.nextDouble()*sum, acc = 0;
            int chosen = 0;
            for (int i = 0; i < n; i++) {
                acc += d2[i];
                if (acc >= r) {
                    chosen = i;
                    break;
                }
            }
            C[c] = Arrays.copyOf(X[chosen], d);
        }
        return super.fitWithCustomInit(X, C, dist, seed, maxIter, tol);
    }
}