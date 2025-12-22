package Survey;

import distance.Distance;
import distance.CosineDistance;
import distance.EuclideanDistance;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import kmeans.IClusteringAlgorithm;
import kmeans.KMeans;
import kmeans.KMeansPlusPlus;

/**
 * Encapsula la configuració de l'algorisme de clustering d'una enquesta i
 * ofereix constructors per als components necessaris.
 */
public class AlgorithmConfiguration {
    private final String initMethod;
    private final String distance;

    public AlgorithmConfiguration(String initMethod, String distance) {
        this.initMethod = normalizeOrDefault(initMethod, "kmeans++");
        this.distance = normalizeOrDefault(distance, "cosine");
    }

    private String normalizeOrDefault(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.toLowerCase(Locale.ROOT);
    }

    /**
     * Construeix una configuració a partir d'una enquesta existent.
     */
    public static AlgorithmConfiguration fromSurvey(Survey survey) {
        if (survey == null) {
            throw new IllegalArgumentException("Survey cannot be null");
        }
        return new AlgorithmConfiguration(survey.getInitMethod(), survey.getDistance());
    }

    /**
     * Retorna l'algorisme de clustering corresponent a la configuració.
     */
    public IClusteringAlgorithm buildAlgorithm() {
        return switch (initMethod) {
            case "kmeans", "k-means" -> new KMeans();
            case "kmeans++", "k-means++", "kpp" -> new KMeansPlusPlus();
            default -> new KMeans();
        };
    }

    /**
     * Catàleg de mètodes de clustering disponibles (id -> etiqueta) perquè el frontend
     * els pugui llistar sense hardcode.
     */
    public static Map<String, String> supportedInitMethods() {
        Map<String, String> methods = new LinkedHashMap<>();
        methods.put("kmeans", "K-Means");
        methods.put("kmeans++", "K-Means++");
        return methods;
    }

    /**
     * Retorna la mètrica de distància corresponent a la configuració.
     */
    public Distance buildDistance() {
        return switch (distance) {
            case "cosine", "cos" -> new CosineDistance();
            case "euclidean", "l2" -> new EuclideanDistance();
            default -> new CosineDistance();
        };
    }

    public String getInitMethod() {
        return initMethod;
    }

    public String getDistance() {
        return distance;
    }
}
