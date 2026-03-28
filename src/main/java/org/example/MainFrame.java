package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class MainFrame extends JFrame {

    private static final double[] START = {1.0, 1.0, 1.0};
    private static final String DEFAULT_EPS = "0.01";
    private static final String DEFAULT_POINT = "(1.0, 1.0, 1.0)";
    private static final String DEFAULT_RESULT = "Δ = 1.0. Результат расчета появится после запуска.";
    private static final String[] FUNCTIONS = {"F1(x1, x2)", "F2(x1, x2, x3)"};
    private static final String[] COLUMNS = {
            "k", "Δ", "Xk и F(Xk)", "J", "yj и F(yj)", "dj", "yj+Δdj и F(yj+Δdj)", "yj-Δdj и F(yj-Δdj)"
    };

    private final JTextField tfEps = new JTextField(DEFAULT_EPS, 10);
    private final JComboBox<String> cbFunction = new JComboBox<>(FUNCTIONS);
    private final JLabel lbStartPoint = new JLabel(DEFAULT_POINT);
    private final JLabel lbResult = new JLabel(DEFAULT_RESULT);
    private final JTextField tfIteration = new JTextField("1", 5);
    private final HookeJeevesChartPanel chartPanel = new HookeJeevesChartPanel();
    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);

    private int currentIteration;
    private List<IterationData> currentIterations;
    private double[] currentStartPoint;
    private boolean currentProjectionIsF2;
    private String currentResultSummary = "";

    public MainFrame() {
        super("Метод Хука-Дживса");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 800);
        setMinimumSize(new Dimension(1000, 600));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        cbFunction.addActionListener(e -> lbStartPoint.setText(DEFAULT_POINT));
        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildNavigationPanel(), BorderLayout.SOUTH);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setRowHeight(40);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                currentIteration = table.getSelectedRow();
                tfIteration.setText(String.valueOf(currentIteration + 1));
                refreshChart();
            }
        });
    }

    private JPanel buildInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Параметры"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        JButton btnCalc = new JButton("Старт");
        JButton btnClear = new JButton("Очистить");
        JButton btnExport = new JButton("Экспорт в Excel");
        btnCalc.addActionListener(e -> calculate());
        btnClear.addActionListener(e -> reset());
        btnExport.addActionListener(e -> export());

        add(panel, gbc, 0, 0, 1, new JLabel("X0:"));
        add(panel, gbc, 1, 0, 3, lbStartPoint);
        add(panel, gbc, 0, 1, 1, new JLabel("ε:"));
        add(panel, gbc, 1, 1, 1, tfEps);
        add(panel, gbc, 0, 2, 1, new JLabel("Функция:"));
        add(panel, gbc, 1, 2, 3, cbFunction);
        add(panel, gbc, 0, 3, 1, btnCalc);
        add(panel, gbc, 1, 3, 1, btnClear);
        add(panel, gbc, 2, 3, 1, btnExport);
        add(panel, gbc, 0, 4, 4, lbResult);
        return panel;
    }

    private JPanel buildNavigationPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Навигация по строкам"));
        JButton btnPrev = new JButton("<");
        JButton btnNext = new JButton(">");
        btnPrev.addActionListener(e -> selectIteration(currentIteration - 1));
        btnNext.addActionListener(e -> selectIteration(currentIteration + 1));
        tfIteration.addActionListener(e -> selectIteration(parseInt(tfIteration.getText(), currentIteration + 1) - 1));
        panel.add(btnPrev);
        panel.add(tfIteration);
        panel.add(btnNext);
        return panel;
    }

    private JSplitPane buildCenterPanel() {
        chartPanel.setBorder(BorderFactory.createTitledBorder("График"));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Итерации метода"));
        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chartPanel, scroll);
        pane.setDividerLocation(380);
        return pane;
    }

    private void calculate() {
        double eps = Math.max(parseDouble(tfEps.getText(), Double.parseDouble(DEFAULT_EPS)), Double.MIN_VALUE);
        double[] start = cbFunction.getSelectedIndex() == 0 ? new double[]{START[0], START[1]} : START.clone();
        HookeJeeves.Function function = cbFunction.getSelectedIndex() == 0 ? Functions::f1 : Functions::f2;

        tableModel.setRowCount(0);
        currentIteration = 0;
        currentIterations = null;
        currentStartPoint = start.clone();
        currentProjectionIsF2 = cbFunction.getSelectedIndex() == 1;
        currentResultSummary = "";
        tfIteration.setText("1");

        HookeJeeves.Result result = HookeJeeves.minimizeWithResult(function, start, HookeJeeves.INITIAL_STEP, eps);
        currentIterations = result.iterations;

        for (IterationData data : result.iterations) {
            tableModel.addRow(new Object[]{
                    data.k,
                    format(data.step),
                    cell(data.x, data.fx),
                    data.j,
                    cell(data.y, data.fy),
                    direction(data.directionVector),
                    cell(data.yPlus, data.fPlus),
                    cell(data.yMinus, data.fMinus)
            });
        }

        currentResultSummary = "Найденная точка: " + point(result.point)
                + ", F(X*) = " + format(result.value)
                + ", строк журнала: " + result.iterations.size();
        lbResult.setText("Δ = 1.0. " + currentResultSummary);
        refreshChart();
        if (table.getRowCount() > 0) {
            selectIteration(0);
        }
    }

    private void export() {
        if (tableModel.getRowCount() == 0) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить результаты");
        chooser.setSelectedFile(new File("hooke-jeeves-results.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path file = chooser.getSelectedFile().toPath();
        if (!file.toString().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = Path.of(file + ".xlsx");
        }
        try {
            ExcelExporter.exportResults(file, "", lbStartPoint.getText(), parseDouble(tfEps.getText(), 0.01), currentResultSummary, tableModel);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void reset() {
        tfEps.setText(DEFAULT_EPS);
        cbFunction.setSelectedIndex(0);
        lbStartPoint.setText(DEFAULT_POINT);
        lbResult.setText(DEFAULT_RESULT);
        tfIteration.setText("1");
        currentIteration = 0;
        currentIterations = null;
        currentStartPoint = null;
        currentProjectionIsF2 = false;
        currentResultSummary = "";
        tableModel.setRowCount(0);
        chartPanel.clear();
    }

    private void selectIteration(int index) {
        if (table.getRowCount() == 0) {
            return;
        }
        currentIteration = Math.max(0, Math.min(index, table.getRowCount() - 1));
        tfIteration.setText(String.valueOf(currentIteration + 1));
        table.setRowSelectionInterval(currentIteration, currentIteration);
        refreshChart();
    }

    private void refreshChart() {
        chartPanel.showIterations(currentIterations, currentStartPoint, currentIteration, currentProjectionIsF2);
    }

    private static void add(JPanel panel, GridBagConstraints gbc, int x, int y, int width, Component component) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = width;
        panel.add(component, gbc);
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static String format(double value) {
        return String.format(Locale.US, "%.3f", value);
    }

    private static String point(double[] point) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < point.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(format(point[i]));
        }
        return sb.append(")").toString();
    }

    private static String cell(double[] point, double value) {
        return "<html>" + point(point) + "<br>F = " + format(value) + "</html>";
    }

    private static String direction(double[] vector) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(String.format(Locale.US, "%.0f", vector[i]));
        }
        return sb.append(")").toString();
    }
}
