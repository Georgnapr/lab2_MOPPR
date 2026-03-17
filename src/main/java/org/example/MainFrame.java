package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    // Поля для ввода начальных условий
    private JTextField tfX, tfY, tfZ, teps;
    private JComboBox<String> cbIntegrator;

    // Панели для графиков
    private JPanel chartPanel = new JPanel();

    // Текстовые области для вывода информации
    private JTextArea textAreaInfo;

    public MainFrame() {

        // Настройка главного окна
        setTitle("Методы многомерной оптимизации");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 900);
        setLocationRelativeTo(null); // Центрируем окно на экране

        // Устанавливаем менеджер компоновки
        setLayout(new BorderLayout(10, 10));

        // Создаем все компоненты интерфейса
        createInputPanel();
        createChartPanel();

        // Устанавливаем минимальный размер окна
        setMinimumSize(new Dimension(900, 600));
    }

    //Создание панели ввода данных (северная часть окна)
    private void createInputPanel() {

        // Основная панель с рамкой и заголовком
        JPanel inputPanel = new JPanel();
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Параметры моделирования"
        ));

        // Используем GridBagLayout для гибкого размещения
        inputPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10); // Отступы между компонентами

        // Создаем поля ввода с начальными значениями
        tfX = new JTextField("1.0", 12);
        tfY = new JTextField("1.0", 12);
        tfZ = new JTextField("1.0", 12);
        teps = new JTextField("0.01", 12);


        // Добавляем подсказки (tooltips)
        tfX.setToolTipText("Начальная координата X");
        tfY.setToolTipText("Начальная координата Y");
        tfZ.setToolTipText("Начальная координата Z");
        teps.setToolTipText("Значение epsilon");

        // Создаем выпадающий список для выбора метода интегрирования
        cbIntegrator = new JComboBox<>(new String[]{
                "-6X1  -  4X2   +  X1^2    +   X2^2  +  18",
                "4X1^2  +  3X2^2 +  X3^2   +  4X1*X2  -  2X2X3   -   16X1  -  4X3 "
        });

        // Создаем кнопки
        JButton btnCalculate = new JButton("Выполнить расчет");
        JButton btnClear = new JButton("Очистить поля");

        // Добавляем обработчики для кнопок
        btnCalculate.addActionListener(new CalculateListener());
        btnClear.addActionListener(e -> resetInputFields());

        // Размещаем компоненты на панели
        // Строка 0: Заголовки для координат
        gbc.gridy = 0;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Начальные координаты (м):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(new JLabel("X"), gbc);
        gbc.gridx = 2;
        inputPanel.add(new JLabel("Y"), gbc);
        gbc.gridx = 3;
        inputPanel.add(new JLabel("Z"), gbc);

        // Строка 1: Поля для координат
        gbc.gridy = 1;
        gbc.gridx = 1;
        inputPanel.add(tfX, gbc);
        gbc.gridx = 2;
        inputPanel.add(tfY, gbc);
        gbc.gridx = 3;
        inputPanel.add(tfZ, gbc);

        //Строка 2: Заголовки для эпсилона
        gbc.gridy = 2;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Значение эпсилон:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(new JLabel("e"), gbc);

        // Строка 3: Поля для координат
        gbc.gridy = 3;
        gbc.gridx = 1;
        inputPanel.add(teps, gbc);

        // Строка 4: Выбор метода
        gbc.gridy = 4;
        gbc.gridx = 0;
        inputPanel.add(new JLabel("Метод интегрирования:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3; // Занимает 2 колонки
        inputPanel.add(cbIntegrator, gbc);

        // Строка 3: Кнопки
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        inputPanel.add(btnCalculate, gbc);
        gbc.gridx = 1;
        inputPanel.add(btnClear, gbc);

        // Контейнер левой области
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(inputPanel, BorderLayout.NORTH);

        // Добавляем контейнер в левую часть главного окна
        add(topContainer, BorderLayout.WEST);
    }

    //Создание панели с графиками (центральная часть окна)
    private void createChartPanel() {

        JPanel chartsContainer = new JPanel(new BorderLayout());
        chartsContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Результаты моделирования"
        ));

        JPanel chartsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


        chartsContainer.add(new JScrollPane(chartsPanel), BorderLayout.CENTER);
        add(chartsContainer, BorderLayout.CENTER);
    }

    private void updateChart(int index, JComponent chart) {
        chartPanel.removeAll();
        chartPanel.add(chart, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    //Сброс полей ввода к значениям по умолчанию
    private void resetInputFields() {
        tfX.setText("1.0");
        tfY.setText("1.0");
        tfZ.setText("1.0");
        teps.setText("0.01");
        cbIntegrator.setSelectedIndex(0);

    }

    //Внутренний класс-слушатель для кнопки расчета
    private class CalculateListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                // Пробуем прочитать значения (для проверки, что это числа)
                double x = Double.parseDouble(tfX.getText());
                double y = Double.parseDouble(tfY.getText());
                double z = Double.parseDouble(tfZ.getText());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(MainFrame.this,
                        "Ошибка ввода: все поля должны содержать числа.",
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
