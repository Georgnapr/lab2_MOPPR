package org.example;

public final class Functions {

    private Functions() {
    }

    public static double f1(double[] x) {
        return -6 * x[0] - 4 * x[1]
                + x[0] * x[0]
                + x[1] * x[1]
                + 18;
    }

    public static double f2(double[] x) {
        return 4 * x[0] * x[0]
                + 3 * x[1] * x[1]
                + x[2] * x[2]
                + 4 * x[0] * x[1]
                - 2 * x[1] * x[2]
                - 16 * x[0]
                - 4 * x[2];
    }
}
