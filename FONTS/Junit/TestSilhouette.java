package Junit;
package validation;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import kmeans.ClusterModel;
import distance.Distance;

/**
 * Proves unitàries per a la classe Silhouette utilitzant Mocks de Mockito.
 * Simula un ClusterModel (etiquetes) i una Distance (distàncies entre punts)
 * per verificar que el càlcul del coeficient de Silhouette és matemàticament correcte.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestSilhouette {

    @Mock
    private ClusterModel mockModel; // Mock del resultat del clúster

    @Mock
    private Distance mockDistance; // Mock de la calculadora de distància

    @InjectMocks
    private Silhouette silhouette; // La classe que provem

    // Dades fictícies. 4 punts. El contingut no importa perquè mockegem la distància.
    private double[][] data;
    private int[] labels;

    /**
     * Configura els mocks. Defineix un model amb 4 punts en 2 clústers (0, 0, 1, 1)
     * i simula totes les distàncies necessàries entre parelles de punts.
     */
    @Before
    public void setUp() {
        data = new double[][]{{0}, {1}, {10}, {11}}; // 4 punts
        
        // Punts 0 i 1 estan al clúster 0.
        // Punts 2 i 3 estan al clúster 1.
        labels = new int[]{0, 0, 1, 1};

        // Definim el guió del mockModel
        when(mockModel.getCentroids()).thenReturn(new double[2][1]); // k=2
        when(mockModel.getLabels()).thenReturn(labels);

        // Definim el guió del mockDistance per a TOTES les parelles (i, j)
        
        // Distàncies dins del clúster 0 (cohesió)
        when(mockDistance.between(data[0], data[1])).thenReturn(1.0); // a(0) i a(1)
        when(mockDistance.between(data[1], data[0])).thenReturn(1.0);

        // Distàncies dins del clúster 1 (cohesió)
        when(mockDistance.between(data[2], data[3])).thenReturn(2.0); // a(2) i a(3)
        when(mockDistance.between(data[3], data[2])).thenReturn(2.0);

        // Distàncies entre clúster 0 i clúster 1 (separació)
        when(mockDistance.between(data[0], data[2])).thenReturn(10.0);
        when(mockDistance.between(data[0], data[3])).thenReturn(11.0);
        when(mockDistance.between(data[1], data[2])).thenReturn(9.0);
        when(mockDistance.between(data[1], data[3])).thenReturn(10.0);
        
        // ... i les simètriques (necessàries per b(i))
        when(mockDistance.between(data[2], data[0])).thenReturn(10.0);
        when(mockDistance.between(data[3], data[0])).thenReturn(11.0);
        when(mockDistance.between(data[2], data[1])).thenReturn(9.0);
        when(mockDistance.between(data[3], data[1])).thenReturn(10.0);
    }

    /**
     * Comprova el càlcul del coeficient de Silhouette per a punts individuals,
     * verificant els valors 'a(i)' (cohesió) i 'b(i)' (separació) manualment.
     */
    @Test
    public void testScorePerPointCalculatesCorrectly() {
        double[] scores = silhouette.scorePerPoint(data, mockModel, mockDistance);

        // --- Càlcul manual per al punt 0 (label 0) ---
        // a(0) = distància mitjana al seu clúster (només al punt 1)
        // a(0) = d(0,1) = 1.0
        // b(0) = min(distància mitjana a altres clústers)
        // b(0)_c1 = (d(0,2) + d(0,3)) / 2 = (10.0 + 11.0) / 2 = 10.5
        // b(0) = 10.5
        // s(0) = (b(0) - a(0)) / max(a(0), b(0)) = (10.5 - 1.0) / 10.5 = 9.5 / 10.5
        double expected_s0 = 9.5 / 10.5;
        
        // --- Càlcul manual per al punt 2 (label 1) ---
        // a(2) = distància mitjana al seu clúster (només al punt 3)
        // a(2) = d(2,3) = 2.0
        // b(2) = min(distància mitjana a altres clústers)
        // b(2)_c0 = (d(2,0) + d(2,1)) / 2 = (10.0 + 9.0) / 2 = 9.5
        // b(2) = 9.5
        // s(2) = (b(2) - a(2)) / max(a(2), b(2)) = (9.5 - 2.0) / 9.5 = 7.5 / 9.5
        double expected_s2 = 7.5 / 9.5;

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
        double expected_s1 = 8.5 / 9.5;
        
        // s(3): a(3) = d(3,2) = 2.0
        //       b(3) = (d(3,0) + d(3,1)) / 2 = (11.0 + 10.0) / 2 = 10.5
        //       s(3) = (10.5 - 2.0) / 10.5 = 8.5 / 10.5
        double expected_s3 = 8.5 / 10.5;

        double expected_s0 = 9.5 / 10.5;
        double expected_s2 = 7.5 / 9.5;

        double expectedAverage = (expected_s0 + expected_s1 + expected_s2 + expected_s3) / 4.0;
        
        double actualAverage = silhouette.average(data, mockModel, mockDistance);
        
        assertEquals(expectedAverage, actualAverage, 1e-9);
    }
}