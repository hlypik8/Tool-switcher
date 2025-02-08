package com.example.tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Tools {

    // Флаги работы программы
    private boolean running = false;
    private boolean clicked = false;

    // Объект Robot для получения цвета пикселя и имитации клика
    private Robot robot;

    // Компоненты интерфейса
    private JLabel mouseCoordsLabel;
    private JTextField checkXField, checkYField;
    private JTextField clickXField, clickYField;
    private JButton startButton, stopButton;

    public Tools() {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            System.exit(1);
        }
        createAndShowGUI();
    }

    private void createAndShowGUI() {
        // Создаём главное окно
        JFrame frame = new JFrame("Tool switcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        // Создаём основной контейнер с вертикальным расположением компонентов
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Метка для отображения текущих координат указателя мыши
        mouseCoordsLabel = new JLabel("Координаты указателя: ");
        mainPanel.add(mouseCoordsLabel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 2. Панель для ввода координат проверки
        JPanel checkLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkLabelPanel.add(new JLabel("Введите координаты проверки:"));
        mainPanel.add(checkLabelPanel);

        JPanel checkCoordsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkCoordsPanel.add(new JLabel("x:"));
        checkXField = new JTextField(5);
        checkCoordsPanel.add(checkXField);
        checkCoordsPanel.add(new JLabel("y:"));
        checkYField = new JTextField(5);
        checkCoordsPanel.add(checkYField);
        mainPanel.add(checkCoordsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 3. Панель для ввода координат для клика
        JPanel clickLabelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clickLabelPanel.add(new JLabel("Введите координаты для клика:"));
        mainPanel.add(clickLabelPanel);

        JPanel clickCoordsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        clickCoordsPanel.add(new JLabel("x:"));
        clickXField = new JTextField(5);
        clickCoordsPanel.add(clickXField);
        clickCoordsPanel.add(new JLabel("y:"));
        clickYField = new JTextField(5);
        clickCoordsPanel.add(clickYField);
        mainPanel.add(clickCoordsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        // 5. Панель с кнопками управления
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        startButton = new JButton("start");
        stopButton = new JButton("stop");
        // Задаём начальные цвета кнопок
        startButton.setBackground(Color.GREEN);
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        stopButton.setBackground(Color.RED);
        stopButton.setForeground(Color.WHITE);
        stopButton.setFocusPainted(false);
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        mainPanel.add(buttonPanel);

        // Добавляем основной контейнер в окно
        frame.getContentPane().add(mainPanel);
        frame.setSize(450, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Таймер Swing, который обновляет интерфейс каждые 100 мс
        Timer timer = new Timer(140, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        timer.start();

        // Обработчик кнопки start
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = true;
                startButton.setBackground(Color.GRAY);
                stopButton.setBackground(Color.RED);
            }
        });

        // Обработчик кнопки stop
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                running = false;
                stopButton.setBackground(Color.GRAY);
                startButton.setBackground(Color.GREEN);
            }
        });
    }

    /**
     * Метод, выполняемый таймером каждые 100 мс:
     * обновляет координаты указателя, считывает значения из полей,
     * проверяет цвет и при совпадении выполняет клик (однократно),
     * а также останавливает работу, если цвет белый.
     */
    private void update() {
        // Обновляем метку с текущими координатами указателя мыши
        Point mousePoint = MouseInfo.getPointerInfo().getLocation();
        mouseCoordsLabel.setText("Координаты указателя: X = " + mousePoint.x + "  Y = " + mousePoint.y);

        if (running) {
            Integer checkX = parseInt(checkXField.getText());
            Integer checkY = parseInt(checkYField.getText());
            if (checkX == null || checkY == null) {
                return;
            }
            // Получаем цвет пикселя по заданным координатам проверки
            Color pixelColor = robot.getPixelColor(checkX, checkY);

            // Если цвет белый (255,255,255), автоматически останавливаем программу
            if (pixelColor.getRed() == 255 && pixelColor.getGreen() == 255 && pixelColor.getBlue() == 255) {
                running = false;
                stopButton.setBackground(Color.GRAY);
                startButton.setBackground(Color.GREEN);
                return;
            }
            // Если цвет пикселя совпадает с заданным пользователем
            if (pixelColor.getRed() == 0 && pixelColor.getGreen() == 150 && pixelColor.getBlue() == 150) {
                // Клик выполняется только один раз при появлении условия
                if (!clicked) {
                    Integer clickX = parseInt(clickXField.getText());
                    Integer clickY = parseInt(clickYField.getText());
                    if (clickX == null || clickY == null) {
                        return;
                    }
                    performMouseClick(clickX, clickY);
                    clicked = true;
                }
            } else {
                clicked = false;
            }
        }
    }

    /**
     * Преобразует строку в число Integer, возвращая null при ошибке.
     */
    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Имитация левого клика мышью по указанным координатам.
     */
    private void performMouseClick(int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void main(String[] args) {
        // Запускаем создание GUI в потоке диспетчеризации событий Swing
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Tools();
            }
        });
    }
}
