package validation;

import kmeans.ClusterModel;
import distance.Distance;
import distance.EuclideanDistance;

/**
 * Implementació del coeficient de Silhouette per validar la qualitat d'un clustering.
 * El coeficient de Silhouette mesura com de similar és un punt als altres punts del seu
 * propi clúster (cohesió) en comparació amb els punts dels altres clústers (separació).
 * Els valors varien entre -1 i 1, on valors propers a 1 indiquen bona agrupació,
 * valors propers a 0 indiquen punts a la frontera entre clústers, i valors negatius
 * indiquen possible assignació incorrecta.
 */
public class Silhouette implements IClusterValidation {

    /**
     * Crea un calculador del coeficient de Silhouette sense estat compartit.
     */
    public Silhouette() {
    }

    /**
     * Calcula el coeficient de Silhouette per a cada punt de dades.
     * Per a cada punt i, calcula a(i) com la distància mitjana als altres punts
     * del seu clúster, i b(i) com la mínima distància mitjana als punts d'altres clústers.
     * El coeficient s(i) es calcula com (b(i) - a(i)) / max(a(i), b(i)).
     *
     * @param X Matriu de dades original.
     * @param model El model de clustering (amb centroides i etiquetes) a avaluar.
     * @param dist La mètrica de distància utilitzada per al clustering.
     * @return Un array de double on cada índex i conté el coeficient de Silhouette del punt i.
     */
    @Override
    public double[] scorePerPoint(double[][] X, ClusterModel model, Distance dist) {
        if (dist == null) dist = new EuclideanDistance();
        int n = X.length, k = model.getCentroids().length;
        int[] lab = model.getLabels();
        double[] s = new double[n];

        int[] counts = new int[k];
        for (int l : lab) counts[l]++;

        for (int i = 0; i < n; i++) {
            int ci = lab[i];

            double a = 0;
            int ca = 0;
            for (int j = 0; j < n; j++)
                if (lab[j] == ci && i != j) {
                    a += dist.between(X[i], X[j]);
                    ca++;
                }
            a = (ca == 0) ? 0 : a/ca;

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
