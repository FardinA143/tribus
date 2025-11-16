package distance;

/**
 * Implementació de l'estratègia de distància utilitzant la mètrica Euclidiana.
 * Calcula l'arrel quadrada de la suma de les diferències al quadrat entre components.
 *
 * @see Distance
 */
public final class EuclideanDistance implements Distance {

    /**
     * Calcula la distància Euclidiana entre dos vectors.
     *
     * @param a El primer vector (punt).
     * @param b El segon vector (punt).
     * @return La distància Euclidiana.
     */
    @Override public double between(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i]-b[i];
            s += d*d;
        }
        return Math.sqrt(s);
    }
}
