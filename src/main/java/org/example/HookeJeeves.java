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
            double alpha,
            double eps
    ) {
        List<IterationData> iterations = new ArrayList<>();

        double[] basePoint = x0.clone();
        double[] trialPoint = x0.clone();
        double baseValue = f.apply(basePoint);

        int k = 0;
        while (true) {
            k++;

            double[] searchStart = trialPoint.clone();
            int iterationLogStart = iterations.size();
            double[] exploredPoint = explore(f, searchStart, step, iterations, k, basePoint, baseValue);
            double exploredValue = f.apply(exploredPoint);

            if (exploredValue < baseValue) {
                double[] previousBasePoint = basePoint.clone();
                double[] currentBasePoint = exploredPoint.clone();
                basePoint = currentBasePoint;
                baseValue = exploredValue;
                trialPoint = createPatternPoint(currentBasePoint, previousBasePoint, alpha);
                attachPatternPoint(iterations, iterationLogStart, k, trialPoint);
            } else {
                if (step <= eps) {
                    attachCurrentPoint(iterations, iterationLogStart, k, basePoint);
                    break;
                }
                step /= 2.0;
                trialPoint = basePoint.clone();
                attachCurrentPoint(iterations, iterationLogStart, k, trialPoint);
            }
        }

        return new Result(iterations, basePoint.clone(), baseValue);
    }

    private static double[] createPatternPoint(double[] x2, double[] x1, double alpha) {
        double[] pattern = new double[x2.length];
        for (int i = 0; i < x2.length; i++) {
            pattern[i] = x2[i] + alpha * (x2[i] - x1[i]);
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
                    bestVal,
                    bestVal < fCurrent,
                    null,
                    best.clone()
            ));

            y = best;
        }

        return y;
    }

    private static void attachPatternPoint(List<IterationData> log, int iterationLogStart, int iteration, double[] patternPoint) {
        for (int i = log.size() - 1; i >= iterationLogStart; i--) {
            IterationData data = log.get(i);
            if (data.k != iteration) {
                continue;
            }
            log.set(i, new IterationData(
                    data.k,
                    data.step,
                    data.x,
                    data.fx,
                    data.j,
                    data.y,
                    data.fy,
                    data.direction,
                    data.directionVector,
                    data.lambda,
                    data.yPlus,
                    data.fPlus,
                    data.yMinus,
                    data.fMinus,
                data.yNext,
                data.fNext,
                data.successfulExploration,
                patternPoint,
                patternPoint
            ));
            return;
        }
    }

    private static void attachCurrentPoint(List<IterationData> log, int iterationLogStart, int iteration, double[] currentPoint) {
        for (int i = log.size() - 1; i >= iterationLogStart; i--) {
            IterationData data = log.get(i);
            if (data.k != iteration) {
                continue;
            }
            log.set(i, new IterationData(
                data.k,
                data.step,
                data.x,
                data.fx,
                data.j,
                data.y,
                data.fy,
                data.direction,
                data.directionVector,
                data.lambda,
                data.yPlus,
                data.fPlus,
                data.yMinus,
                data.fMinus,
                data.yNext,
                data.fNext,
                data.successfulExploration,
                data.patternPoint,
                currentPoint
            ));
            return;
        }
    }

    private static double[] createDirectionVector(int size, int index, double value) {
        double[] vector = new double[size];
        vector[index] = value;
        return vector;
    }
}
