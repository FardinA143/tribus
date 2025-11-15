package kselector;

import kmeans.*;
import distance.Distance;
import java.util.*;

/**
 * Implementació del mètode del colze (Elbow Method) per seleccionar el nombre òptim de clústers.
 * Executa l'algorisme de clustering per a diferents valors de k i calcula la inèrcia per a cadascun.
 * El valor òptim de k es determina trobant el punt de màxima curvatura (el "colze") en la corba
 * d'inèrcia versus k, calculat com la màxima distància perpendicular a la recta que uneix els extrems.
 */
public class ElbowMethod implements IKSelector {

    /**
     * Suggereix el nombre òptim de clústers utilitzant el mètode del colze.
     * Prova diferents valors de k dins del rang especificat i troba el punt
     * on la reducció de la inèrcia comença a disminuir més lentament.
     *
     * @param X Matriu de dades.
     * @param kMin El nombre mínim de clústers a provar.
     * @param kMax El nombre màxim de clústers a provar.
     * @param algo L'algoritme de clustering a utilitzar per provar cada k.
     * @param dist La mètrica de distància a utilitzar.
     * @param seed La seed aleatòria per a consistència.
     * @return El nombre k suggerit com a òptim.
     */
    @Override
    public int suggestK(double[][] X, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed) {
        List<Double> inertia = new ArrayList<>();
        for (int k = kMin; k <= kMax; k++) {
            ClusterModel m = algo.fit(X, k, dist, seed, 200, 1e-4);
            inertia.add(m.getInertia());
        }

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
