package kselector;

import kmeans.IClusteringAlgorithm;
import kmeans.ClusterModel;
import distance.Distance;
import java.util.*;

public class ElbowMethod implements IKSelector {
    @Override
    public int suggestK(double[][] X, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed) {
        List<Double> inertia = new ArrayList<>();
        for (int k = kMin; k <= kMax; k++) {
            ClusterModel m = algo.fit(X, k, dist, seed, 200, 1e-4);
            inertia.add(m.getInertia());
        }
        //Detecta el codo por máxima distancia a la recta entre extremos
        double x1 = kMin, y1 = inertia.get(0);
        double x2 = kMax, y2 = inertia.get(inertia.size()-1);
        double bestD = -1;
        int bestK = kMin;
        for (int idx = 0; idx < inertia.size(); idx++) {
            double x0 = kMin + idx, y0 = inertia.get(idx);
            double d = Math.abs((y2 - y1)*x0 - (x2 - x1)*y0 + x2*y1 - y2*x1)/
                       Math.hypot(y2 - y1, x2 - x1);
            if (d > bestD) {
                bestD = d;
                bestK = (int)x0;
            }
        }
        return bestK;
    }
}
