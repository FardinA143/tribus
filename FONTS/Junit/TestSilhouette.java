package Junit;

import distance.Distance;
import distance.EuclideanDistance;
import kmeans.ClusterModel;
import org.junit.Before;
import org.junit.Test;
import validation.Silhouette;

import static org.junit.Assert.*;

/**
 * Proves unitàries per a la classe Silhouette utilitzant Mocks de Mockito.
 * Simula un ClusterModel (etiquetes) i una Distance (distàncies entre punts)
 * per verificar que el càlcul del coeficient de Silhouette és matemàticament correcte.
 */
public class TestSilhouette {
    private Silhouette silhouette;
    private Distance distance;
    private double[][] data;
    private ClusterModel model;

    /**
     * Configura els mocks. Defineix un model amb 4 punts en 2 clústers (0, 0, 1, 1)
     * i simula totes les distàncies necessàries entre parelles de punts.
     */
    @Before
    public void setUp() {
        silhouette = new Silhouette();
        distance = new EuclideanDistance();
        data = new double[][]{{0.0}, {1.0}, {10.0}, {12.0}}; // 4 punts a una sola dimensió

        // Punts 0 i 1 estan al clúster 0. Punts 2 i 3 al clúster 1.
        int[] labels = new int[]{0, 0, 1, 1};
        double[][] centroids = {{0.5}, {11.0}};
        model = new ClusterModel(centroids, labels, 0.0, 1);
    }

    /**
     * Comprova el càlcul del coeficient de Silhouette per a punts individuals,
     * verificant els valors 'a(i)' (cohesió) i 'b(i)' (separació) manualment.
     */
    @Test
    public void testScorePerPointCalculatesCorrectly() {
        double[] scores = silhouette.scorePerPoint(data, model, distance);

        double expected_s0 = (11.0 - 1.0) / 11.0;   // (b - a) / max(a, b)
        double expected_s2 = (9.5 - 2.0) / 9.5;

        assertEquals(expected_s0, scores[0], 1e-9);
        assertEquals(expected_s2, scores[2], 1e-9);
    }
    
    /**
     * Comprova el càlcul de la mitjana de Silhouette.
     */
    @Test
    public void testAverageCalculatesCorrectly() {
        // Calculem els valors per als altres 2 punts per a la mitjana
        // s(1): a(1) = d(1,0) = 1.0
        //       b(1) = (d(1,2) + d(1,3)) / 2 = (9.0 + 10.0) / 2 = 9.5
        //       s(1) = (9.5 - 1.0) / 9.5 = 8.5 / 9.5
        double expected_s0 = (11.0 - 1.0) / 11.0;
        double expected_s1 = (10.0 - 1.0) / 10.0;
        
        // s(3): a(3) = d(3,2) = 2.0
        //       b(3) = (d(3,0) + d(3,1)) / 2 = (12.0 + 11.0) / 2 = 11.5
        double expected_s2 = (9.5 - 2.0) / 9.5;
        double expected_s3 = (11.5 - 2.0) / 11.5;

        double expectedAverage = (expected_s0 + expected_s1 + expected_s2 + expected_s3) / 4.0;
        
        double actualAverage = silhouette.average(data, model, distance);
        
        assertEquals(expectedAverage, actualAverage, 1e-9);
    }
}