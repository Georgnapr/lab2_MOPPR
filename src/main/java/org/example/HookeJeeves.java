package org.example;

import java.util.ArrayList;
import java.util.List;

public final class HookeJeeves {

    public static final double INITIAL_STEP = 1.0;

    private HookeJeeves() {
    }

    public static class Result {
        public final List<IterationData> iterations;
        public final double[] point;
        public final double value;

        public Result(List<IterationData> iterations, double[] point, double value) {
            this.iterations = iterations;
            this.point = point.clone();
            this.value = value;
        }
    }

    public interface Function {
        double apply(double[] x);
    }

    public static Result minimizeWithResult(
            Function f,
            double[] x0,
            double step,
            double eps
    ) {
        List<IterationData> iterations = new ArrayList<>();

        double[] x = x0.clone();
        double[] base = x0.clone();
        double baseValue = f.apply(base);

        int k = 0;
        while (step > eps) {
            k++;

            double[] currentX = x.clone();
            double currentFx = f.apply(currentX);
            double[] newPoint = explore(f, currentX, step, iterations, k, currentX, currentFx);
            double newValue = f.apply(newPoint);

            if (newValue < baseValue) {
                double[] previousBase = base;
                base = newPoint;
                baseValue = newValue;
                x = createPatternPoint(newPoint, previousBase);
            } else {
                step /= 2.0;
                x = base.clone();
            }
        }

        return new Result(iterations, base.clone(), baseValue);
    }

    private static double[] createPatternPoint(double[] newPoint, double[] previousBase) {
        double[] pattern = new double[newPoint.length];
        for (int i = 0; i < newPoint.length; i++) {
            pattern[i] = newPoint[i] + (newPoint[i] - previousBase[i]);
        }
        return pattern;
    }

    private static double[] explore(
            Function f,
            double[] x,
            double step,
            List<IterationData> log,
            int iteration,
            double[] xk,
            double fxk
    ) {
        double[] y = x.clone();

        for (int j = 0; j < x.length; j++) {
            double[] yCurrent = y.clone();

            double[] yPlus = yCurrent.clone();
            yPlus[j] += step;

            double[] yMinus = yCurrent.clone();
            yMinus[j] -= step;

            double fCurrent = f.apply(yCurrent);
            double fPlus = f.apply(yPlus);
            double fMinus = f.apply(yMinus);

            double[] best = yCurrent;
            double bestVal = fCurrent;
            String direction = "0";
            double[] directionVector = new double[x.length];
            double lambda = 0.0;

            if (fPlus < bestVal) {
                best = yPlus;
                bestVal = fPlus;
                direction = "+";
                directionVector = createDirectionVector(x.length, j, 1.0);
                lambda = step;
            }

            if (fMinus < bestVal) {
                best = yMinus;
                bestVal = fMinus;
                direction = "-";
                directionVector = createDirectionVector(x.length, j, -1.0);
                lambda = step;
            }

            log.add(new IterationData(
                    iteration,
                    step,
                    xk.clone(),
                    fxk,
                    j + 1,
                    yCurrent,
                    fCurrent,
                    direction,
                    directionVector,
                    lambda,
                    yPlus,
                    fPlus,
                    yMinus,
                    fMinus,
                    best.clone(),
                    bestVal
            ));

            y = best;
        }

        return y;
    }

    private static double[] createDirectionVector(int size, int index, double value) {
        double[] vector = new double[size];
        vector[index] = value;
        return vector;
    }
}
