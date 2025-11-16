package distance;

/**
 * Defineix la interfície per a una estratègia de càlcul de distància.
 * Permet calcular la distància entre dos punts (vectors) en un espai n-dimensional.
 */
public interface Distance {
    /**
     * Calcula la distància entre dos vectors de tipus double.
     *
     * @param a El primer vector (punt).
     * @param b El segon vector (punt).
     * @return La distància calculada entre el vector a i b.
     */
    double between(double[] a, double[] b);
}
