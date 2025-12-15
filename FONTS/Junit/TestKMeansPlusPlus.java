package Junit;

import distance.EuclideanDistance;
import kmeans.ClusterModel;
import kmeans.KMeansPlusPlus;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Proves unitàries per a l'algorisme KMeansPlusPlus.
 * Verifica que la inicialització K++ convergeix correctament.
 */
public class TestKMeansPlusPlus {

    /**
     * Comprova que 'fit' amb inicialització K++ convergeix
     * correctament en un cas simple.
     */
    @Test
    public void testFitKPPConvergesCorrectly() {
        KMeansPlusPlus kMeansPP = new KMeansPlusPlus();
        
        // Mateixes dades que a la prova de KMeans
        double[][] data = {
            {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, 
            {10.0, 10.0}, {11.0, 10.0}, {10.0, 11.0}
        };
        
        int k = 2;
        // Fem servir una seed fixa per a què la inicialització K++ sigui determinista
        ClusterModel model = kMeansPP.fit(data, k, new EuclideanDistance(), 42L, 100, 1e-4);
        
        int[] labels = model.getLabels();
        
        // Comprovem les assignacions
        int clusterA = labels[0];
        int clusterB = labels[3];
        
        assertNotEquals(clusterA, clusterB);
        
        assertEquals(clusterA, labels[1]);
        assertEquals(clusterA, labels[2]);
        
        assertEquals(clusterB, labels[4]);
        assertEquals(clusterB, labels[5]);
    }
}
