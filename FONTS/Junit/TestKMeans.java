package Junit;

import distance.EuclideanDistance;
import kmeans.ClusterModel;
import kmeans.KMeans;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Proves unitàries per a l'algorisme KMeans estàndard.
 * Verifica la convergència i l'assignació de clústers en un cas simple
 * amb inicialització aleatòria (però amb seed fixa).
 */
public class TestKMeans {

    /**
     * Comprova que 'fit' agrupa correctament un conjunt de dades simple
     * i clarament separat en dos clústers.
     */
    @Test
    public void testFitGroupsSimpleDataCorrectly() {
        KMeans kMeans = new KMeans();
        
        // Dos grups de punts clarament separats
        double[][] data = {
            {0.0, 0.0}, {1.0, 0.0}, {0.0, 1.0}, // Clúster 0
            {10.0, 10.0}, {11.0, 10.0}, {10.0, 11.0} // Clúster 1
        };
        
        int k = 2;
        // Fem servir una seed fixa (0L) per a què la inicialització aleatòria
        // sigui sempre la mateixa i la prova sigui reproduïble.
        ClusterModel model = kMeans.fit(data, k, new EuclideanDistance(), 0L, 100, 1e-4);
        
        int[] labels = model.getLabels();
        
        // Comprovem que els 3 primers punts pertanyen a un clúster
        // i els 3 últims a l'altre.
        int clusterA = labels[0];
        int clusterB = labels[3];
        
        assertNotEquals("Els dos grups haurien d'estar en clústers diferents", clusterA, clusterB);
        
        assertEquals(clusterA, labels[1]);
        assertEquals(clusterA, labels[2]);
        
        assertEquals(clusterB, labels[4]);
        assertEquals(clusterB, labels[5]);
    }
}
