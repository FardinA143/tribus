package Junit;
package kselector;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import kmeans.ClusterModel;
import kmeans.IClusteringAlgorithm;
import distance.Distance;

/**
 * Proves unitàries per a ElbowMethod utilitzant Mocks de Mockito.
 * Simula el comportament de IClusteringAlgorithm per retornar
 * inèrcies predefinides i així verificar que la lògica de
 * 'suggestK' troba el 'colze' (elbow) correcte.
 */
@RunWith(MockitoJUnitRunner.class)
public class TestElbowMethod {

    @Mock // Creem un Mock de l'algorisme
    private IClusteringAlgorithm mockAlgorithm;

    @Mock // Creem Mocks per als models de resultat
    private ClusterModel mockModelK2, mockModelK3, mockModelK4;

    @InjectMocks // Creem una instància de la classe a provar i li injectem els mocks
    private ElbowMethod elbowMethod;
    
    // Dades fictícies. El contingut no importa perquè l'algorisme està mockejat.
    private double[][] dummyData;

    /**
     * Configura els mocks abans de cada prova.
     * Defineix el 'guió' del IClusteringAlgorithm simulat:
     * - Si es crida amb k=2, retorna mockModelK2 (inèrcia 100.0)
     * - Si es crida amb k=3, retorna mockModelK3 (inèrcia 20.0)
     * - Si es crida amb k=4, retorna mockModelK4 (inèrcia 15.0)
     */
    @Before
    public void setUp() {
        dummyData = new double[][]{{0}, {1}, {2}, {10}, {11}, {12}};

        // 1. Definim el "guió" dels mocks
        // 'any()' i 'eq()' són 'matchers' de Mockito.
        when(mockAlgorithm.fit(any(), eq(2), any(), anyLong(), anyInt(), anyDouble()))
            .thenReturn(mockModelK2);
        
        when(mockAlgorithm.fit(any(), eq(3), any(), anyLong(), anyInt(), anyDouble()))
            .thenReturn(mockModelK3);

        when(mockAlgorithm.fit(any(), eq(4), any(), anyLong(), anyInt(), anyDouble()))
            .thenReturn(mockModelK4);

        // 2. Definim la inèrcia que retornarà cada model
        when(mockModelK2.getInertia()).thenReturn(100.0); // Inèrcia alta
        when(mockModelK3.getInertia()).thenReturn(20.0);  // Gran baixada (el colze)
        when(mockModelK4.getInertia()).thenReturn(15.0);  // Baixada petita
    }

    /**
     * Comprova que 'suggestK' troba el 'colze' (k=3) basant-se en les
     * inèrcies simulades (100.0, 20.0, 15.0).
     */
    @Test
    public void testSuggestKFindsElbowCorrectly() {
        // Executem el mètode a provar
        int kMin = 2, kMax = 4;
        int suggestedK = elbowMethod.suggestK(dummyData, kMin, kMax, mockAlgorithm, null, 0L);

        // Verifiquem que el 'k' suggerit és 3, que és on
        // la inèrcia va passar de 100 a 20 (la curvatura màxima).
        assertEquals(3, suggestedK);

        // Opcional: verifiquem que s'ha cridat a 'fit' 3 cops
        verify(mockAlgorithm, times(1)).fit(any(), eq(2), any(), anyLong(), anyInt(), anyDouble());
        verify(mockAlgorithm, times(1)).fit(any(), eq(3), any(), anyLong(), anyInt(), anyDouble());
        verify(mockAlgorithm, times(1)).fit(any(), eq(4), any(), anyLong(), anyInt(), anyDouble());
    }
}
