package distance;

/**
 * Implementació de la distància del Cosinus (1 - Similitud del Cosinus).
 * És ideal per a dades de text (Bag of Words) o dades disperses on la magnitud
 * del vector no és tan important com la seva orientació (perfil).
 */
public class CosineDistance implements Distance {

    public CosineDistance() {
    }

    /**
     * Calcula la distància del Cosinus entre dos vectors.
     * Distància = 1 - (ProducteEscalar(A, B) / (Norma(A) * Norma(B)))
     * * @param a Primer vector.
     * @param b Segon vector.
     * @return Valor entre [0, 1] (o fins a 2 si hi ha valors negatius, que no és el cas aquí).
     * Retorna 1.0 si algun dels vectors és zero (ortogonalitat màxima/sense informació).
     */
    @Override
    public double between(double[] a, double[] b) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        // Evitar divisió per zero si un vector és buit/nul (tot 0)
        if (normA == 0.0 || normB == 0.0) {
            return 1.0; // Màxima distància si no hi ha informació per comparar
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        
        // Assegurar que estem en rang [-1, 1] per errors de punt flotant
        similarity = Math.max(-1.0, Math.min(1.0, similarity));

        // Distància = 1 - Similitud
        return 1.0 - similarity;
    }
}