package kmeans;

import distance.Distance;
import distance.EuclideanDistance;
import java.util.*;

public class KMeans implements IClusteringAlgorithm {
    @Override
    public ClusterModel fit(double[][] X, int k, Distance dist, long seed, int maxIter, double tol) {
        Objects.requireNonNull(X);
        if (k <= 0) throw new IllegalArgumentException("k must be > 0");
        if (dist == null) dist = new EuclideanDistance();
        final Random rnd = new Random(seed);
        final int n = X.length, d = X[0].length;

        //init: sample k distinct points
        double[][] C = new double[k][d];
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < n; i++) idx.add(i);
        Collections.shuffle(idx, rnd);
        for (int j = 0; j < k; j++) C[j] = Arrays.copyOf(X[idx.get(j)], d);
        
        int[] labels = new int[n];
        double prevInertia = Double.POSITIVE_INFINITY;
        int it = 0;

        while (it < maxIter) {
            //assign
            double inertia = 0.0;
            for (int i = 0; i < n; i++) {
                int best = 0; double bestDist = Double.POSITIVE_INFINITY;
                for (int j = 0; j < k; j++) {
                    double dj = dist.between(X[i], C[j]);
                    if (dj < bestDist) {
                        bestDist = dj;
                        best = j;
                    }
                    labels[i] = best;
                    inertia += bestDist*bestDist; //SSE
                }
            }

            //check convergence
            if (Math.abs(prevInertia - inertia) <= tol*Math.max(1.0, prevInertia)) {
                return new ClusterModel(C, labels, inertia, it+1);
            }
            prevInertia = inertia;

            //update centroids
            double[][] newC = new double[k][d];
            int[] counts = new int[k];
            for (int i = 0; i < n; i++) {
                int c = labels[i];
                counts[c]++;
                for (int t = 0; t < d; t++) newC[c][t] += X[i][t];
            }
            //handle empty clusters: reseed with farthest point
            for (int c = 0; c < k; c++) {
                if (counts[c] == 0) {
                    int far = -1;
                    double farD = -1;
                    for (int i = 0; i < n; i++) {
                        double dj = dist.between(X[i], C[nearest(X[i], C, dist)]);
                        if (dj > farD) {
                            farD = dj;
                            far = i;
                        }
                    }
                    newC[c] = Arrays.copyOf(X[far], d);
                    counts[c] = 1;
                }
                else {
                    for (int t = 0; t < d; t++) newC[c][t] /= counts[c];
                }
            }
            C = newC;
            it++;
        }
        return new ClusterModel(C, labels, prevInertia, it);
    }

    private int nearest(double[] x, double[][] C, Distance dist) {
        int best = 0;
        double bestD = Double.POSITIVE_INFINITY;
        for (int j = 0; j < C.length; j++) {
            double d = dist.between(x, C[j]);
            if (d < bestD) {
                bestD = d;
                best = j;
            }
        }
        return best;
    }

    protected ClusterModel fitWithCustomInit(double[][] X, double[][] initC, Distance dist, long seed, int maxIter, double tol) {
        if (dist == null) dist = new EuclideanDistance();
        final int n = X.length, d = X[0].length, k = initC.length;
        double[][] C = new double[k][d];
        for (int j = 0; j < k; j++) C[j] = Arrays.copyOf(initC[j], d);

        int[] labels = new int[n];
        double prevInertia = Double.POSITIVE_INFINITY;
        int it = 0;

        while (it < maxIter) {
            double inertia = 0.0;
            for (int i = 0; i < n; i++) {
                int best = 0;
                double bestDist = Double.POSITIVE_INFINITY;
                for (int j = 0; j < k; j++) {
                    double dj = dist.between(X[i], C[j]);
                    if (dj < bestDist) {
                        bestDist= dj;
                        best = j;
                    }
                }
                labels[i] = best;
                inertia += bestDist*bestDist;
            }
            if (Math.abs(prevInertia - inertia) <= tol*Math.max(1.0, prevInertia))
                return new ClusterModel(C, labels, inertia, it+1);
            prevInertia = inertia;

            double[][] newC = new double[k][d]; int[] counts = new int[k];
            for (int i = 0; i < n; i++) {
                int c = labels[i];
                counts[c]++;
                for (int t = 0; t < d; t++) newC[c][t] += X[i][t];
            }
            for (int c = 0; c < k; c++) {
                if (counts[c] == 0) { //reseed simple: copia un punto aleatorio cercano
                    int far = iFarthest(X, C, dist);
                    newC[c] = Arrays.copyOf(X[far], d);
                    counts[c] = 1;
                }
                else for (int t = 0; t < d; t++) newC[c][t] /= counts[c];
            }
            C = newC;
            it++;
        }
        return new ClusterModel(C, labels, prevInertia, it);
    }

    private int iFarthest(double[][]X, double[][] C, Distance dist) {
        int far = 0;
        double best = -1;
        for (int i = 0; i < X.length; i++) {
            double dj = dist.between(X[i], C[nearest(X[i], C, dist)]);
            if (dj > best) {
                best = dj;
                far = i;
            }
        }
        return far;
    }
}
