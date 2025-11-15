package kmeans;

/**
 * Representa el resultat d'un algorisme de clustering.
 * Conté els centroides resultants, les etiquetes assignades a cada punt,
 * la inèrcia (suma de distàncies quadrades als centroides) i el nombre d'iteracions realitzades.
 */
public final class ClusterModel {

    /**
     * Matriu amb els centroides finals, on cada fila és un centroide.
     */
    private final double[][] centroids;
    
    /**
     * Array amb les etiquetes de clúster assignades a cada punt de dades.
     */
    private final int[] labels;
    
    /**
     * La inèrcia del model (suma de distàncies quadrades dins dels clústers).
     */
    private final double inertia;
    
    /**
     * El nombre d'iteracions que ha realitzat l'algorisme fins convergir.
     */
    private final int iterations;

    /**
     * Crea un nou model de clustering amb els resultats especificats.
     *
     * @param centroids Matriu amb els centroides finals.
     * @param labels Array amb les etiquetes de clúster per a cada punt.
     * @param inertia La inèrcia del model.
     * @param iterations El nombre d'iteracions realitzades.
     */
    public ClusterModel(double[][] centroids, int[] labels, double inertia, int iterations) {
        this.centroids = centroids;
        this.labels = labels;
        this.inertia = inertia;
        this.iterations = iterations;
    }
    
    /**
     * Obté la matriu de centroides.
     *
     * @return Matriu on cada fila representa un centroide.
     */
    public double[][] getCentroids() {
        return centroids;
    }

    /**
     * Obté les etiquetes de clúster assignades.
     *
     * @return Array amb l'etiqueta de clúster per a cada punt de dades.
     */
    public int[] getLabels() {
        return labels;
    }

    /**
     * Obté la inèrcia del model.
     *
     * @return La suma de distàncies quadrades dins dels clústers.
     */
    public double getInertia() {
        return inertia;
    }

    /**
     * Obté el nombre d'iteracions realitzades.
     *
     * @return El nombre d'iteracions fins a la convergència.
     */
    public int getIterations() {
        return iterations;
    }
}
