package Junit;
package distance;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe EuclideanDistance.
 * Verifica que el càlcul de la distància euclidiana és correcte
 * per a diversos casos.
 */
public class TestEuclideanDistance {

    // Tolerància per a la comparació de valors de tipus double
    private static final double DELTA = 1e-9;
    private final EuclideanDistance distance = new EuclideanDistance();

    /**
     * Comprova que la distància entre dos punts idèntics és zero.
     */
    @Test
    public void testDistanceBetweenEqualPoints() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {1.0, 2.0, 3.0};
        double expected = 0.0;
        double actual = distance.between(a, b);
        assertEquals(expected, actual, DELTA);
    }

    /**
     * Comprova un cas simple de distància 2D (triangle pitagòric 3-4-5).
     */
    @Test
    public void testSimple2DDistance() {
        double[] a = {0.0, 0.0};
        double[] b = {3.0, 4.0};
        double expected = 5.0;
        double actual = distance.between(a, b);
        assertEquals(expected, actual, DELTA);
    }

    /**
     * Comprova un cas simple de distància 3D.
     */
    @Test
    public void testSimple3DDistance() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 6.0, 8.0}; // Diferències: 3, 4, 5
        // sqrt(3^2 + 4^2 + 5^2) = sqrt(9 + 16 + 25) = sqrt(50)
        double expected = Math.sqrt(50.0);
        double actual = distance.between(a, b);
        assertEquals(expected, actual, DELTA);
    }
}
