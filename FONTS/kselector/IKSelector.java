package kselector;

import kmeans.IClusteringAlgorithm;
import distance.Distance;

/**
 * Interfície per a una estratègia de selecció del nombre òptim de clústers (k).
 */
public interface IKSelector {

    /**
     * Suggereix el nombre k òptim de clústers dins d'un rang donat.
     *
     * @param data  La matriu de dades.
     * @param kMin  El nombre mínim de clústers a provar.
     * @param kMax  El nombre màxim de clústers a provar.
     * @param algo  L'algoritme de clustering a utilitzar per provar cada k.
     * @param dist  La mètrica de distància a utilitzar.
     * @param seed  La seed aleatòria per a consistència.
     * @return El nombre k suggerit com a òptim.
     */
    int suggestK(double[][] data, int kMin, int kMax, IClusteringAlgorithm algo, Distance dist, long seed);
}