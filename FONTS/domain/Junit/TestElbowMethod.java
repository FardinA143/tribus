package Junit;

import distance.Distance;
import kmeans.ClusterModel;
import kmeans.IClusteringAlgorithm;
import kselector.ElbowMethod;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Proves unitàries per a ElbowMethod utilitzant Mocks de Mockito.
 * Simula el comportament de IClusteringAlgorithm per retornar
 * inèrcies predefinides i així verificar que la lògica de
 * 'suggestK' troba el 'colze' (elbow) correcte.
 */
public class TestElbowMethod {
    private ElbowMethod elbowMethod;
    
    // Dades fictícies. El contingut no importa perquè l'algorisme està mockejat.
    private double[][] dummyData;

    /**
     * Configura els mocks abans de cada prova.
     * Defineix el 'guió' del IClusteringAlgorithm simulat per retornar
     * inèrcies (100.0, 20.0, 15.0) per a k=2, 3, i 4, respectivament.
     */
    @Before
    public void setUp() {
        elbowMethod = new ElbowMethod();
        dummyData = new double[][]{{0}, {1}, {2}, {10}, {11}, {12}};
    }

    /**
     * Comprova que 'suggestK' troba el 'colze' (k=3) basant-se en les
     * inèrcies simulades i la lògica de màxima distància a la línia extrema.
     */
    @Test
    public void testSuggestKFindsElbowCorrectly() {
        int kMin = 2, kMax = 4;
        RecordingAlgorithm algorithm = RecordingAlgorithm.withInertia(2, 100.0, 3, 20.0, 4, 15.0);

        int suggestedK = elbowMethod.suggestK(dummyData, kMin, kMax, algorithm, null, 0L);

        // El K suggerit hauria de ser 3, el punt de màxima curvatura.
        assertEquals(3, suggestedK);

        // Verifiquem que l'algorisme s'ha executat per a cadascun dels valors de k provats.
        assertArrayEquals(new int[]{2, 3, 4}, algorithm.getCalls());
    }

    /**
     * Implementació mínima d'IClusteringAlgorithm que retorna models amb inèrcia predefinida
     * i registra els valors de k utilitzats. D'aquesta manera evitem dependències externes
     * (Mockito) i mantenim el test unitari totalment determinista.
     */
    private static final class RecordingAlgorithm implements IClusteringAlgorithm {
        private final Map<Integer, Double> inertiaByK;
        private final List<Integer> calls = new ArrayList<>();

        private RecordingAlgorithm(Map<Integer, Double> inertiaByK) {
            this.inertiaByK = inertiaByK;
        }

        static RecordingAlgorithm withInertia(int k1, double i1, int k2, double i2, int k3, double i3) {
            Map<Integer, Double> map = new HashMap<>();
            map.put(k1, i1);
            map.put(k2, i2);
            map.put(k3, i3);
            return new RecordingAlgorithm(map);
        }

        @Override
        public ClusterModel fit(double[][] data, int k, Distance distance, long seed, int maxIter, double tol) {
            calls.add(k);
            double inertia = inertiaByK.getOrDefault(k, Double.NaN);
            return new ClusterModel(new double[k][1], new int[0], inertia, 1);
        }

        int[] getCalls() {
            return calls.stream().mapToInt(Integer::intValue).toArray();
        }
    }
}