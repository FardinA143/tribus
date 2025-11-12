package Domain;

import Exceptions.*;
import java.util.*;

public class OneHotEncoder implements IEncoder {
    private final List<String> featureNames = new ArrayList<>();
    private List<Map<String, Integer>> vocabPerColumn = new ArrayList<>();
    private int totalDims = 0;

    @Override
    public void fit(List<?> rawRows) {
        //rawRow = List<Object> ya normalizada por dominio
        //y que cada columna es: int/double (númerico) o String (categoría)
        if (rawRows.isEmpty()) {
            featureNames.clear();
            vocabPerColumn.clear();
            totalDims = 0;
            return;
        }
        List<?> first = (List<?>) rawRows.get(0);
        int m = first.size();
        vocabPerColumn = new ArrayList<>(Collections.nCopies(m, null));
        featureNames.clear();
        totalDims = 0;

        for (int c = 0; c < m; c++) {
            Object val = ((List<?>)rawRows.get(0)).get(c);
            if (val instanceof Number) {
                vocabPerColumn.set(c, null);
                featureNames.add("col" + c);
                totalDims += 1;
            }
            else {
                //categórica -> one-hot
                Map<String,Integer> dict = new LinkedHashMap<>();
                for (Object row : rawRows) {
                    Object v = ((List<?>)row).get(c);
                    if (v != null) dict.putIfAbsent(String.valueOf(v), dict.size());
                }
                vocabPerColumn.set(c, dict);
                for (String cat : dict.keySet()) featureNames.add("col"+c+"="+cat);
                totalDims += dict.size();
            }
        }
    }

    @Override
    public double[][] transform(List<?> rawRows) {
        double[][] X = new double[rawRows.size()][totalDims];
        for (int i = 0; i < rawRows.size(); i++) {
            List<?> row = (List<?>) rawRows.get(i);
            int ofs = 0;
            for (int c = 0; c < row.size(); c++) {
                Map<String,Integer> dict = vocabPerColumn.get(c);
                if (dict == null) {
                    X[i][ofs++] = row.get(c) == null ? 0.0 : ((Number)row.get(c)).doubleValue();
                }
                else {
                    Object v = row.get(c);
                    if (v != null) {
                        Integer idx = dict.get(String.valueOf(v));
                        if (idx != null) X[i][ofs + idx] = 1.0;
                    }
                    ofs += dict.size();
                }
            }
        }
        return X;
    }

    @Override public List<String> getFeatureNames() {
        return Collections.unmodifiableList(featureNames);
    }
}
