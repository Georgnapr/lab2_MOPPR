package org.example;

public class IterationData {

    public final int k;
    public final double step;
    public final double[] x;
    public final double fx;

    public final int j;
    public final double[] y;
    public final double fy;

    public final String direction;
    public final double[] directionVector;
    public final double lambda;

    public final double[] yPlus;
    public final double fPlus;

    public final double[] yMinus;
    public final double fMinus;

    public final double[] yNext;
    public final double fNext;
    public final boolean successfulExploration;
    public final double[] patternPoint;
    public final double[] currentPoint;

    public IterationData(
            int k,
            double step,
            double[] x,
            double fx,
            int j,
            double[] y,
            double fy,
            String direction,
            double[] directionVector,
            double lambda,
            double[] yPlus,
            double fPlus,
            double[] yMinus,
            double fMinus,
            double[] yNext,
            double fNext,
            boolean successfulExploration,
            double[] patternPoint,
            double[] currentPoint
    ) {
        this.k = k;
        this.step = step;
        this.x = x.clone();
        this.fx = fx;
        this.j = j;
        this.y = y.clone();
        this.fy = fy;
        this.direction = direction;
        this.directionVector = directionVector.clone();
        this.lambda = lambda;
        this.yPlus = yPlus.clone();
        this.fPlus = fPlus;
        this.yMinus = yMinus.clone();
        this.fMinus = fMinus;
        this.yNext = yNext.clone();
        this.fNext = fNext;
        this.successfulExploration = successfulExploration;
        this.patternPoint = patternPoint == null ? null : patternPoint.clone();
        this.currentPoint = currentPoint == null ? null : currentPoint.clone();
    }
}
