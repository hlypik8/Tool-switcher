package com.example.toolswitcher;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class ToolSwitcher extends Application {

    // Флаг, определяющий, запущена ли проверка
    private boolean running = false;
    // Флаг, чтобы клик происходил только один раз при появлении условия
    private boolean clicked = false;
    // Объект для имитации действий мыши и получения цвета экрана
    private Robot robot;

    @Override
    public void start(Stage primaryStage) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
            return;
        }

        // --- Элементы интерфейса ---

        // 1. Отображение текущих координат указателя мыши
        Label mouseCoordsLabel = new Label("Координаты указателя: ");

        // 2. Панель для ввода координат проверки
        Label checkLabel = new Label("Введите координаты проверки:");
        Label checkXLabel = new Label("x:");
        TextField checkXField = new TextField();
        checkXField.setPrefWidth(60);
        Label checkYLabel = new Label("y:");
        TextField checkYField = new TextField();
        checkYField.setPrefWidth(60);
        HBox checkCoordsBox = new HBox(10, checkXLabel, checkXField, checkYLabel, checkYField);
        checkCoordsBox.setAlignment(Pos.CENTER_LEFT);
        VBox checkCoordsVBox = new VBox(5, checkLabel, checkCoordsBox);

        // 3. Панель для ввода координат для клика
        Label clickLabel = new Label("Введите координаты для клика:");
        Label clickXLabel = new Label("x:");
        TextField clickXField = new TextField();
        clickXField.setPrefWidth(60);
        Label clickYLabel = new Label("y:");
        TextField clickYField = new TextField();
        clickYField.setPrefWidth(60);
        HBox clickCoordsBox = new HBox(10, clickXLabel, clickXField, clickYLabel, clickYField);
        clickCoordsBox.setAlignment(Pos.CENTER_LEFT);
        VBox clickCoordsVBox = new VBox(5, clickLabel, clickCoordsBox);

        // 4. Панель для ввода цветовых составляющих проверки
        Label colorLabel = new Label("Введите цвет проверки:");
        Label rLabel = new Label("R:");
        TextField rField = new TextField();
        rField.setPrefWidth(60);
        Label gLabel = new Label("G:");
        TextField gField = new TextField();
        gField.setPrefWidth(60);
        Label bLabel = new Label("B:");
        TextField bField = new TextField();
        bField.setPrefWidth(60);
        HBox colorBox = new HBox(10, rLabel, rField, gLabel, gField, bLabel, bField);
        colorBox.setAlignment(Pos.CENTER_LEFT);
        VBox colorVBox = new VBox(5, colorLabel, colorBox);

        // Основной вертикальный контейнер
        VBox centerBox = new VBox(15, mouseCoordsLabel, checkCoordsVBox, clickCoordsVBox, colorVBox);
        centerBox.setPadding(new Insets(15));

        // 5. Кнопки управления (start и stop)
        Button startButton = new Button("start");
        Button stopButton = new Button("stop");
        // Изначальные цвета кнопок
        startButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        stopButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");

        HBox buttonsBox = new HBox(20, startButton, stopButton);
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.setPadding(new Insets(10));

        // Корневой контейнер (BorderPane)
        BorderPane root = new BorderPane();
        root.setCenter(centerBox);
        root.setBottom(buttonsBox);

        Scene scene = new Scene(root, 450, 300);
        primaryStage.setTitle("Проверка цвета и имитация клика");
        primaryStage.setScene(scene);
        // Делаем окно всегда поверх других
        primaryStage.setAlwaysOnTop(true);
        primaryStage.show();

        // --- Таймлайн для периодического обновления ---
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), event -> {
            // Обновляем координаты указателя мыши
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            mouseCoordsLabel.setText("Координаты указателя: X = " + mousePoint.x + "  Y = " + mousePoint.y);

            if (running) {
                // Получаем координаты проверки из полей
                Integer checkX = parseInt(checkXField.getText());
                Integer checkY = parseInt(checkYField.getText());
                if (checkX == null || checkY == null) {
                    return;
                }
                // Считываем цвет пикселя по указанным координатам проверки
                java.awt.Color pixelColor = robot.getPixelColor(checkX, checkY);

                // Если определяется белый цвет (255, 255, 255), автоматически останавливаем программу
                if (pixelColor.getRed() == 255 && pixelColor.getGreen() == 255 && pixelColor.getBlue() == 255) {
                    running = false;
                    // Меняем цвета кнопок: stop становится серой, start – зеленой
                    stopButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-weight: bold;");
                    startButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
                    return;
                }

                // Получаем требуемые значения цвета из полей ввода (для проверки клика)
                Integer r = parseInt(rField.getText());
                Integer g = parseInt(gField.getText());
                Integer b = parseInt(bField.getText());
                if (r == null || g == null || b == null) {
                    return;
                }
                // Если цвет пикселя совпадает с заданным
                if (pixelColor.getRed() == r && pixelColor.getGreen() == g && pixelColor.getBlue() == b) {
                    // Производим клик только один раз, если еще не было клика по данному условию
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
                    // Сбрасываем флаг, когда условие не выполняется
                    clicked = false;
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // --- Обработчики кнопок ---
        startButton.setOnAction(e -> {
            running = true;
            // При нажатии на start: кнопка start становится серой, stop – красной
            startButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-weight: bold;");
            stopButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold;");
        });

        stopButton.setOnAction(e -> {
            running = false;
            // При нажатии на stop: кнопка stop становится серой, start – зеленой
            stopButton.setStyle("-fx-background-color: gray; -fx-text-fill: white; -fx-font-weight: bold;");
            startButton.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-weight: bold;");
        });
    }

    /**
     * Пробует преобразовать строку в число Integer.
     * Если преобразование не удалось, возвращает null.
     */
    private Integer parseInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Имитация левого клика мышью по заданным координатам.
     */
    private void performMouseClick(int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(50); // Краткая задержка
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
