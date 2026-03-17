package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private JTextField tfX, tfY, tfZ, teps;
    private JComboBox<String> cbFunction;

    private JPanel chartPanel;
    private JTable table;
    private DefaultTableModel tableModel;

    private JButton btnExport;
    private JButton btnPrev, btnNext;
    private JTextField tfIteration;

    private int currentIteration = 0;

    public MainFrame() {
        setTitle("Метод Хука-Дживса");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));

        createInputPanel();
        createCenterPanel();

        setMinimumSize(new Dimension(900, 600));
    }

    // 🔹 ПАНЕЛЬ ВВОДА
    private void createInputPanel() {

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Параметры"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        tfX = new JTextField("1.0", 10);
        tfY = new JTextField("1.0", 10);
        tfZ = new JTextField("1.0", 10);
        teps = new JTextField("0.01", 10);

        cbFunction = new JComboBox<>(new String[]{
                "F1(x1,x2)",
                "F2(x1,x2,x3)"
        });

        JButton btnCalc = new JButton("Старт");
        JButton btnClear = new JButton("Очистить");
        btnExport = new JButton("Экспорт в Excel");

        btnExport.addActionListener(e ->
                JOptionPane.showMessageDialog(this,
                        "Экспорт пока не реализован")
        );

        btnCalc.addActionListener(new CalculateListener());
        btnClear.addActionListener(e -> resetInputFields());

        // --- размещение ---
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("X0:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(tfX, gbc);
        gbc.gridx = 2;
        inputPanel.add(tfY, gbc);
        gbc.gridx = 3;
        inputPanel.add(tfZ, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("ε:"), gbc);

        gbc.gridx = 1;
        inputPanel.add(teps, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Функция:"), gbc);

        gbc.gridx = 1; gbc.gridwidth = 3;
        inputPanel.add(cbFunction, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 3;
        gbc.gridx = 0;
        inputPanel.add(btnCalc, gbc);

        gbc.gridx = 1;
        inputPanel.add(btnClear, gbc);

        gbc.gridx = 2;
        inputPanel.add(btnExport, gbc);

        add(inputPanel, BorderLayout.NORTH);
    }

    // 🔹 ЦЕНТР (график + таблица)
    private void createCenterPanel() {

        // --- панель итераций ---
        JPanel iterationPanel = new JPanel();
        iterationPanel.setBorder(BorderFactory.createTitledBorder("Итерация"));

        btnPrev = new JButton("←");
        btnNext = new JButton("→");
        tfIteration = new JTextField("0", 5);

        iterationPanel.add(btnPrev);
        iterationPanel.add(tfIteration);
        iterationPanel.add(btnNext);

        btnPrev.addActionListener(e -> changeIteration(-1));
        btnNext.addActionListener(e -> changeIteration(1));
        tfIteration.addActionListener(e -> setIterationFromField());

        add(iterationPanel, BorderLayout.SOUTH);

        // --- split ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);

        // --- график ---
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("График (заглушка)"));

        JLabel chartStub = new JLabel("Здесь будет график", SwingConstants.CENTER);
        chartPanel.add(chartStub, BorderLayout.CENTER);

        // --- таблица ---
        String[] columns = {
                "k", "Δ", "Xk", "F(Xk)",
                "j", "yj", "F(yj)",
                "dj", "yj+Δdj", "F(yj+Δdj)",
                "yj-Δdj", "F(yj-Δdj)"
        };

        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder("Итерации метода"));

        // ВАЖНО: добавляем в splitPane
        splitPane.setLeftComponent(chartPanel);
        splitPane.setRightComponent(scroll);

        add(splitPane, BorderLayout.CENTER);
    }

    // 🔹 работа с таблицей
    private void addIterationRow(Object... data) {
        tableModel.addRow(data);

        // автоскролл вниз
        table.scrollRectToVisible(
                table.getCellRect(table.getRowCount() - 1, 0, true)
        );
    }

    private void clearTable() {
        tableModel.setRowCount(0);
    }

    // 🔹 сброс
    private void resetInputFields() {
        tfX.setText("1.0");
        tfY.setText("1.0");
        tfZ.setText("1.0");
        teps.setText("0.01");
        cbFunction.setSelectedIndex(0);
        clearTable();
    }

    // 🔹 управление итерациями
    private void changeIteration(int delta) {
        currentIteration += delta;

        if (currentIteration < 0) currentIteration = 0;
        if (currentIteration >= table.getRowCount())
            currentIteration = table.getRowCount() - 1;

        tfIteration.setText(String.valueOf(currentIteration));

        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(currentIteration, currentIteration);
        }
    }

    private void setIterationFromField() {
        try {
            currentIteration = Integer.parseInt(tfIteration.getText());

            if (currentIteration >= 0 && currentIteration < table.getRowCount()) {
                table.setRowSelectionInterval(currentIteration, currentIteration);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Неверный номер итерации");
        }
    }

    // 🔹 заглушка расчета
    private class CalculateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double x = Double.parseDouble(tfX.getText());
                double y = Double.parseDouble(tfY.getText());
                double z = Double.parseDouble(tfZ.getText());
                double eps = Double.parseDouble(teps.getText());

                clearTable();
                currentIteration = 0;
                tfIteration.setText("0");

                // пример строки
                addIterationRow(
                        1,
                        0.2,
                        "(2.00, 3.00)",
                        16.00,
                        1,
                        "(2.20, 3.00)",
                        14.44,
                        "(1,0)",
                        "(2.20, 3.20)",
                        17.64,
                        "(2.20, 2.80)",
                        11.56
                );

                addIterationRow(
                        2,
                        0.2,
                        "(2.00, 3.00)",
                        16.00,
                        1,
                        "(2.20, 3.00)",
                        14.44,
                        "(1,0)",
                        "(2.20, 3.20)",
                        17.64,
                        "(2.20, 2.80)",
                        11.56
                );

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Ошибка ввода",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}