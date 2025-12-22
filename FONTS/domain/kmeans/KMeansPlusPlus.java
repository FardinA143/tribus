package kmeans;

import java.util.*;
import distance.Distance;
import distance.CosineDistance;

/**
 * Implementació de l'algorisme K-Means++ per a clustering.
 * Millora l'algorisme K-Means estàndard utilitzant una inicialització
 * "intel·ligent" dels centroides basada en probabilitats proporcionals
 * a les distàncies quadrades. D'aquesta manera, estem reduint la
 * probabilitat de convergir a solucions subòptimes i accelerant
 * la convergència.
 */
public class KMeansPlusPlus extends KMeans {

    /**
     * Crea una variant K-Means++ utilitzant la implementació base de {@link KMeans}.
     */
    public KMeansPlusPlus() {
        super();
    }
    
    /**
     * Executa l'algorisme K-Means++ sobre un conjunt de dades.
     * Utilitza la inicialització K++ per seleccionar els centroides inicials
     * de manera més intel·ligent que l'assignació aleatòria, triant punts
     * que estan lluny dels centroides ja seleccionats.
     *
     * @param X Matriu de dades a agrupar (n_mostres x n_features).
     * @param k El nombre de clústers a trobar.
    * @param dist La mètrica de distància a utilitzar. Si és null, s'usarà CosineDistance.
     * @param seed La llavor per al generador de números aleatoris.
     * @param maxIter El nombre màxim d'iteracions a executar.
     * @param tol La tolerància per declarar convergència (canvi en la inèrcia).
     * @return Un objecte ClusterModel amb els centroides, etiquetes i inèrcia resultants.
     */
    @Override
    public ClusterModel fit(double[][] X, int k, Distance dist, long seed, int maxIter, double tol) {
        if (dist == null) dist = new CosineDistance();
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