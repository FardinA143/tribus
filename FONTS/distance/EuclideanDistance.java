package distance;

public final class EuclideanDistance implements Distance {
    @Override public double between(double[] a, double[] b) {
        double s = 0.0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i]-b[i];
            s += d*d;
        }
        return Math.sqrt(s);
    }
}
