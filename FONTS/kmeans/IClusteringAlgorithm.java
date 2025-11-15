package kmeans;

import distance.Distance;

/**
 * Interfície per a un algorisme de clustering.
 * Defineix el contracte per a algorismes que poden agrupar dades
 * en k clústers.
 */
public interface IClusteringAlgorithm {

    /**
     * Executa l'algorisme de clustering sobre un conjunt de dades.
     *
     * @param data      La matriu de dades a agrupar (n_mostres x n_features).
     * @param k         El nombre de clústers a trobar.
     * @param distance  La mètrica de distància a utilitzar. Si és null,
     * s'usarà EuclideanDistance.
     * @param seed      La llavor per al generador de números aleatoris,
     * per assegurar reproductibilitat.
     * @param maxIter   El nombre màxim d'iteracions a executar.
     * @param tol       La tolerància per declarar convergència
     * (canvi en la inèrcia).
     * @return Un objecte ClusterModel que conté els centroides,
     * les etiquetes (assignacions) i la inèrcia.
     */
    ClusterModel fit(double[][] data, int k, Distance distance, long seed, int maxIter, double tol);
    
    /**
     * Mètode de conveniència per executar l'algorisme amb paràmetres per defecte.
     *
     * @param data La matriu de dades a agrupar.
     * @param k    El nombre de clústers.
     * @return Un objecte ClusterModel resultant.
     */
    default ClusterModel fit(double[][] data, int k) {
        return fit(data, k, null, System.nanoTime(), 300, 1e-4);
    }
}
