package FONTS.Encoder;

import java.util.List;

public interface IEncoder {
    //prepara qualsevol estat intern (diccionaris, dominis, etc.)
    void fit(List<?> rawRows);

    //transforma files "raw" a una matriu numèrica (nRows x nFeatures)
    double[][] transform(List<?> rawRows);

    //fit + transform
    default double[][] fitTransform(List<?> rawRows) {
        fit(rawRows);
        return transform(rawRows);
    }

    //noms de les columnes resultants (export)
    List<String> getFeatureNames();
}