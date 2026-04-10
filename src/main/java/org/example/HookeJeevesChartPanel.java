package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYShapeAnnotation;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.List;

public class HookeJeevesChartPanel extends JPanel {

    private static final String BASE_TITLE = "Траектория поиска";
    private static final Color GRID_COLOR = new Color(220, 220, 220);
    private static final Color PATH_COLOR = new Color(0, 102, 204);
    private static final Color FAILED_PATH_COLOR = new Color(200, 45, 45);
    private static final Color CURRENT_POINT_COLOR = new Color(210, 60, 20);
    private static final Color PIT_OUTLINE_COLOR = new Color(0, 120, 140, 80);
    private static final Color PIT_FILL_COLOR = new Color(60, 170, 190, 28);

    private static final double F1_CENTER_X = 3.0;
    private static final double F1_CENTER_Y = 2.0;
    private static final double F2_CENTER_X = 3.0;
    private static final double F2_CENTER_Y = -2.0;

    private final XYSeries pathSeries;
    private final XYSeries failedPathSeries;
    private final XYSeries currentPointSeries;
    private final JFreeChart chart;

    public HookeJeevesChartPanel() {
        super(new BorderLayout());

        pathSeries = new XYSeries("Траектория", false, true);
        failedPathSeries = new XYSeries("Неудачное направление", false, true);
        currentPointSeries = new XYSeries("Текущая точка", false, true);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(pathSeries);
        dataset.addSeries(failedPathSeries);
        dataset.addSeries(currentPointSeries);

        chart = ChartFactory.createXYLineChart(
                BASE_TITLE,
                "x",
                "y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        configurePlot(chart.getXYPlot());

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        chartPanel.setDomainZoomable(true);
        chartPanel.setRangeZoomable(true);
        add(chartPanel, BorderLayout.CENTER);

        clear();
    }

    public void clear() {
        pathSeries.clear();
        failedPathSeries.clear();
        currentPointSeries.clear();
        rebuildRelief(false);
        chart.setTitle(BASE_TITLE);
    }

    public void showIterations(List<IterationData> iterations, double[] startPoint, int selectedIndex, boolean isProjectedF2) {
        pathSeries.clear();
        failedPathSeries.clear();
        currentPointSeries.clear();
        rebuildRelief(isProjectedF2);

        if (!isValidPoint(startPoint)) {
            chart.setTitle(BASE_TITLE);
            return;
        }

        addPoint(pathSeries, startPoint);

        int safeIndex = iterations == null || iterations.isEmpty()
                ? -1
                : Math.max(0, Math.min(selectedIndex, iterations.size() - 1));

        if (safeIndex >= 0) {
            int i = 0;
            while (i <= safeIndex) {
                int iteration = iterations.get(i).k;
                int groupStart = i;
                while (i <= safeIndex && iterations.get(i).k == iteration) {
                    i++;
                }
                int visibleGroupEnd = i - 1;
                boolean groupComplete = i >= iterations.size() || iterations.get(i).k != iteration;
                boolean nextGroupVisible = i <= safeIndex;
                renderIterationGroup(iterations, groupStart, visibleGroupEnd, groupComplete, nextGroupVisible);
            }
            addCurrentPoint(iterations.get(safeIndex));
        } else {
            addPoint(currentPointSeries, startPoint);
        }

        chart.setTitle(buildTitle(isProjectedF2, safeIndex + 1, iterations == null ? 0 : iterations.size()));
    }

    private void configurePlot(XYPlot plot) {
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(GRID_COLOR);
        plot.setRangeGridlinePaint(GRID_COLOR);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesPaint(0, PATH_COLOR);
        renderer.setSeriesStroke(0, new BasicStroke(2.2f));

        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, true);
        renderer.setSeriesPaint(1, FAILED_PATH_COLOR);
        renderer.setSeriesStroke(1, new BasicStroke(2.2f));

        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShapesVisible(2, true);
        renderer.setSeriesPaint(2, CURRENT_POINT_COLOR);
        renderer.setSeriesShape(2, new Ellipse2D.Double(-5.0, -5.0, 10.0, 10.0));

        plot.setRenderer(renderer);
    }

    private void rebuildRelief(boolean isProjectedF2) {
        XYPlot plot = chart.getXYPlot();
        plot.clearAnnotations();

        double centerX = isProjectedF2 ? F2_CENTER_X : F1_CENTER_X;
        double centerY = isProjectedF2 ? F2_CENTER_Y : F1_CENTER_Y;
        double[][] rings = {
                {0.35, 0.25},
                {0.55, 0.40},
                {0.80, 0.58},
                {1.05, 0.75},
                {1.35, 0.97},
                {1.70, 1.20},
                {2.10, 1.48},
                {2.55, 1.80}
        };

        for (double[] ring : rings) {
            plot.addAnnotation(createPitAnnotation(centerX, centerY, ring[0], ring[1]));
        }
    }

    private XYShapeAnnotation createPitAnnotation(double centerX, double centerY, double radiusX, double radiusY) {
        Shape ellipse = new Ellipse2D.Double(
                centerX - radiusX,
                centerY - radiusY,
                radiusX * 2.0,
                radiusY * 2.0
        );

        return new XYShapeAnnotation(
                ellipse,
                new BasicStroke(1.3f),
                PIT_OUTLINE_COLOR,
                PIT_FILL_COLOR
        );
    }

    private void renderIterationGroup(
            List<IterationData> iterations,
            int groupStart,
            int groupEnd,
            boolean groupComplete,
            boolean nextGroupVisible
    ) {
        if (!groupComplete) {
            renderPartialGroup(iterations, groupStart, groupEnd);
            return;
        }
        IterationData last = iterations.get(groupEnd);
        if (last.patternPoint != null) {
            renderAcceptedGroup(iterations, groupStart, groupEnd, nextGroupVisible);
        } else {
            renderRejectedGroup(iterations, groupStart, groupEnd);
        }
    }

    private void renderPartialGroup(List<IterationData> iterations, int groupStart, int groupEnd) {
        IterationData first = iterations.get(groupStart);
        if (!samePoint(getLastAcceptedPoint(), first.y)) {
            addPoint(pathSeries, first.y);
        }
        for (int i = groupStart; i <= groupEnd; i++) {
            IterationData data = iterations.get(i);
            if (data.successfulExploration) {
                addPoint(pathSeries, data.yNext);
            }
        }
    }

    private void renderAcceptedGroup(List<IterationData> iterations, int groupStart, int groupEnd, boolean nextGroupVisible) {
        IterationData first = iterations.get(groupStart);
        if (!samePoint(getLastAcceptedPoint(), first.y)) {
            addPoint(pathSeries, first.y);
        }
        for (int i = groupStart; i <= groupEnd; i++) {
            IterationData data = iterations.get(i);
            if (data.successfulExploration) {
                addPoint(pathSeries, data.yNext);
            }
        }
        if (!nextGroupVisible) {
            IterationData last = iterations.get(groupEnd);
            if (!samePoint(getLastAcceptedPoint(), last.currentPoint)) {
                addPoint(pathSeries, last.currentPoint);
            }
        }
    }

    private void renderRejectedGroup(List<IterationData> iterations, int groupStart, int groupEnd) {
        IterationData first = iterations.get(groupStart);
        double[] failedStart = getLastAcceptedPoint();
        if (!isValidPoint(failedStart)) {
            failedStart = first.x;
        }
        appendFailedPoint(failedStart);
        if (!samePoint(failedStart, first.y)) {
            appendFailedPoint(first.y);
        }
        for (int i = groupStart; i <= groupEnd; i++) {
            IterationData data = iterations.get(i);
            if (data.successfulExploration) {
                appendFailedPoint(data.yNext);
            }
        }
        breakFailedPath();
    }

    private boolean isValidPoint(double[] point) {
        return point != null && point.length >= 2;
    }

    private boolean samePoint(double[] left, double[] right) {
        if (!isValidPoint(left) || !isValidPoint(right)) {
            return false;
        }
        return Double.compare(left[0], right[0]) == 0
                && Double.compare(left[1], right[1]) == 0;
    }

    private void addPoint(XYSeries series, double[] point) {
        if (isValidPoint(point)) {
            series.add(point[0], point[1]);
        }
    }

    private void appendFailedPoint(double[] point) {
        if (isValidPoint(point)) {
            failedPathSeries.add(point[0], point[1]);
        }
    }

    private void breakFailedPath() {
        failedPathSeries.add(Double.NaN, Double.NaN);
    }

    private void addCurrentPoint(IterationData data) {
        if (isValidPoint(data.currentPoint)) {
            addPoint(currentPointSeries, data.currentPoint);
        } else if (data.patternPoint != null) {
            addPoint(currentPointSeries, data.patternPoint);
        } else {
            addPoint(currentPointSeries, data.yNext);
        }
    }

    private double[] getLastAcceptedPoint() {
        if (pathSeries.isEmpty()) {
            return null;
        }
        int index = pathSeries.getItemCount() - 1;
        Number x = pathSeries.getX(index);
        Number y = pathSeries.getY(index);
        if (x == null || y == null || Double.isNaN(x.doubleValue()) || Double.isNaN(y.doubleValue())) {
            return null;
        }
        return new double[]{x.doubleValue(), y.doubleValue()};
    }

    private String buildTitle(boolean isProjectedF2, int currentStep, int totalSteps) {
        if (isProjectedF2) {
            return BASE_TITLE + " (проекция x-y), шаг " + currentStep + " из " + totalSteps;
        }
        return BASE_TITLE + ", шаг " + currentStep + " из " + totalSteps;
    }
}
