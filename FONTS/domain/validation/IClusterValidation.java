package validation;

import kmeans.ClusterModel;
import distance.Distance;

/**
 * Interfície per a una estratègia de validació de la qualitat d'un clustering.
 * Permet avaluar com de bones són les agrupacions resultants.
 */
public interface IClusterValidation {

    /**
     * Calcula la puntuació de validació per a cada punt de dades.
     *
     * @param data  La matriu de dades original.
     * @param model El model de clustering (amb centroides i etiquetes) a avaluar.
     * @param dist  La mètrica de distància utilitzada per al clustering.
     * @return Un array de double (double[]) on cada índex i conté
     * la puntuació del punt i en la matriu de dades.
     */
    double[] scorePerPoint(double[][] data, ClusterModel model, Distance dist);

    /**
     * Calcula la puntuació de validació mitjana per a tot el clustering.
     *
     * @param data  La matriu de dades original.
     * @param model El model de clustering a avaluar.
     * @param dist  La mètrica de distància.
     * @return La puntuació mitjana (Coeficient de Silhouette promig).
     */
    default double average(double[][] data, ClusterModel model, Distance dist) {
        double[] s = scorePerPoint(data, model, dist);
        double m = 0;
        for (double v : s) m += v;
        return m/s.length;
    }
}
