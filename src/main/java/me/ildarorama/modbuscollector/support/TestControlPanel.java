package me.ildarorama.modbuscollector.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TestControlPanel extends BorderPane {

    private static class ParameterControl {

        String name;
        String unit;
        double min;
        double max;
        double defaultValue;
        Slider slider;
        TextField textField;
        CheckBox autoCheckBox;
        Label valueLabel;

        ParameterControl(
            String name,
            String unit,
            double min,
            double max,
            double defaultValue
        ) {
            this.name = name;
            this.unit = unit;
            this.min = min;
            this.max = max;
            this.defaultValue = defaultValue;
        }
    }

    private final List<ParameterControl> parameters = new ArrayList<
        ParameterControl
    >();
    private final Random random = new Random();
    private Timer autoUpdateTimer;
    private volatile boolean isRunning = false;
    private Stage stage;

    public interface DataProvider {
        float[] getCurrentValues();
        void setManualMode(boolean manual);
    }

    private DataProvider dataProvider;

    public TestControlPanel() {
        initializeParameters();
        createUI();
        startAutoUpdateTimer();
    }

    private void initializeParameters() {
        parameters.add(
            new ParameterControl(
                "Частота вращения ротора (n)",
                "об/мин",
                0,
                50000,
                15000
            )
        );
        parameters.add(
            new ParameterControl("Температура масла (Tм)", "°C", -40, 150, 80)
        );
        parameters.add(
            new ParameterControl(
                "Температура ОГ на входе в турбину (Tог1)",
                "°C",
                0,
                800,
                500
            )
        );
        parameters.add(
            new ParameterControl(
                "Температура ОГ на выходе из турбины (Tог2)",
                "°C",
                0,
                800,
                450
            )
        );
        parameters.add(
            new ParameterControl(
                "Температура воздуха на входе в компрессор (Tв1)",
                "°C",
                -40,
                200,
                25
            )
        );
        parameters.add(
            new ParameterControl(
                "Температура воздуха на выходе из компрессора (Tв2)",
                "°C",
                -40,
                200,
                120
            )
        );
        parameters.add(
            new ParameterControl(
                "Избыточное давление газа на входе в турбину (Pг1)",
                "кПа",
                0,
                250,
                150
            )
        );
        parameters.add(
            new ParameterControl(
                "Избыточное давление газа на выходе из турбины (Pг2)",
                "кПа",
                0,
                250,
                100
            )
        );
        parameters.add(
            new ParameterControl(
                "Разрежение на входе в компрессор (Pк1)",
                "кПа",
                -100,
                0,
                -30
            )
        );
        parameters.add(
            new ParameterControl(
                "Избыточное давление на выходе из компрессора (Pк2)",
                "кПа",
                0,
                500,
                250
            )
        );
    }

    private void createUI() {
        setStyle("-fx-background-color: #f5f5f5;");
        setPadding(new Insets(10));

        Label titleLabel = new Label("Панель управления параметрами");
        titleLabel.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        );

        HBox titleBox = new HBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(0, 0, 1, 0));

        HBox globalControls = createGlobalControls();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");

        VBox parametersBox = createParametersBox();
        scrollPane.setContent(parametersBox);

        VBox topBox = new VBox(10);
        topBox.getChildren().addAll(titleBox, globalControls);

        setTop(topBox);
        setCenter(scrollPane);
    }

    private HBox createGlobalControls() {
        HBox controlsBox = new HBox(20);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(10));
        controlsBox.setStyle(
            "-fx-background-color: #ecf0f1; -fx-background-radius: 5px;"
        );

        Button enableAllAutoButton = new Button("Включить все авто");
        enableAllAutoButton.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        enableAllAutoButton.setOnAction(e -> setAllAutoMode(false));

        Button disableAllAutoButton = new Button("Выключить все авто");
        disableAllAutoButton.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        disableAllAutoButton.setOnAction(e -> setAllAutoMode(true));

        Button resetButton = new Button("Сбросить к умолчанию");
        resetButton.setStyle(
            "-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        resetButton.setOnAction(e -> resetToDefaults());

        Button randomButton = new Button("Случайные значения");
        randomButton.setStyle(
            "-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;"
        );
        randomButton.setOnAction(e -> setRandomValues());

        controlsBox
            .getChildren()
            .addAll(
                enableAllAutoButton,
                disableAllAutoButton,
                resetButton,
                randomButton
            );

        return controlsBox;
    }

    private VBox createParametersBox() {
        VBox parametersBox = new VBox(10);
        parametersBox.setPadding(new Insets(10));

        for (int i = 0; i < parameters.size(); i++) {
            ParameterControl param = parameters.get(i);
            VBox parameterBox = createParameterControl(param, i + 1);
            parametersBox.getChildren().add(parameterBox);
        }

        return parametersBox;
    }

    private VBox createParameterControl(ParameterControl param, int index) {
        VBox paramBox = new VBox(8);
        paramBox.setStyle(
            "-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: #bdc3c7; -fx-border-radius: 8px; -fx-border-width: 1px;"
        );
        paramBox.setPadding(new Insets(15));

        Label nameLabel = new Label(index + ". " + param.name);
        nameLabel.setStyle(
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        );

        param.valueLabel = new Label(
            String.format("%.1f %s", param.defaultValue, param.unit)
        );
        param.valueLabel.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"
        );

        HBox headerBox = new HBox();
        headerBox.getChildren().addAll(nameLabel);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headerBox.getChildren().addAll(spacer, param.valueLabel);

        param.slider = new Slider(param.min, param.max, param.defaultValue);
        param.slider.setShowTickLabels(true);
        param.slider.setShowTickMarks(true);
        param.slider.setMajorTickUnit((param.max - param.min) / 5);
        param.slider.setMinorTickCount(5);
        param.slider.setBlockIncrement((param.max - param.min) / 10);

        param.slider
            .valueProperty()
            .addListener((obs, oldVal, newVal) -> {
                if (!param.autoCheckBox.isSelected()) {
                    updateParameterValue(param, newVal.doubleValue());
                }
            });

        param.textField = new TextField(String.valueOf(param.defaultValue));
        param.textField.setPrefWidth(100);
        param.textField.setPromptText("Введите значение");

        param.textField.setOnAction(e -> {
            try {
                double value = Double.parseDouble(param.textField.getText());
                if (value >= param.min && value <= param.max) {
                    param.slider.setValue(value);
                    updateParameterValue(param, value);
                } else {
                    showValueRangeAlert(param);
                    param.textField.setText(
                        String.valueOf(param.slider.getValue())
                    );
                }
            } catch (NumberFormatException ex) {
                showInvalidNumberAlert();
                param.textField.setText(
                    String.valueOf(param.slider.getValue())
                );
            }
        });

        param.autoCheckBox = new CheckBox("Автоматический режим");
        param.autoCheckBox.setStyle("-fx-font-size: 12px;");
        param.autoCheckBox.setSelected(false);

        param.autoCheckBox.setOnAction(e -> {
            boolean autoMode = param.autoCheckBox.isSelected();
            param.slider.setDisable(autoMode);
            param.textField.setDisable(autoMode);

            if (autoMode) {
                param.slider.setStyle("-fx-opacity: 0.6;");
                param.textField.setStyle("-fx-opacity: 0.6;");
            } else {
                param.slider.setStyle("-fx-opacity: 1.0;");
                param.textField.setStyle("-fx-opacity: 1.0;");
            }
        });

        Label rangeLabel = new Label(
            String.format(
                "Диапазон: %.0f - %.0f %s",
                param.min,
                param.max,
                param.unit
            )
        );
        rangeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");

        HBox controlsBox = new HBox(10);
        controlsBox.setAlignment(Pos.CENTER_LEFT);
        controlsBox
            .getChildren()
            .addAll(
                new Label("Значение:"),
                param.textField,
                param.autoCheckBox
            );

        paramBox
            .getChildren()
            .addAll(headerBox, param.slider, controlsBox, rangeLabel);
        param.autoCheckBox.fire();

        return paramBox;
    }

    private void updateParameterValue(
        final ParameterControl param,
        final double value
    ) {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    param.valueLabel.setText(
                        String.format("%.1f %s", value, param.unit)
                    );
                    param.textField.setText(String.format("%.1f", value));
                }
            }
        );
    }

    private void setAllAutoMode(boolean autoMode) {
        for (ParameterControl param : parameters) {
            param.autoCheckBox.setSelected(autoMode);
            param.autoCheckBox.fire();
        }
    }

    private void resetToDefaults() {
        for (ParameterControl param : parameters) {
            param.slider.setValue(param.defaultValue);
            updateParameterValue(param, param.defaultValue);
        }
    }

    private void setRandomValues() {
        for (int i = 0; i < parameters.size(); i++) {
            ParameterControl param = parameters.get(i);

            if (!param.autoCheckBox.isSelected()) {
                float min = (float) param.min;
                float max = (float) param.max;

                float value = min + (max - min) * random.nextFloat();
                int bits = Float.floatToIntBits(value);
                int low = bits & 0xFFFF;
                int high = (bits >> 16) & 0xFFFF;

                if (i < parameters.size() - 1) {
                    parameters.get(i).slider.setValue(low);
                    parameters.get(i + 1).slider.setValue(high);
                    updateParameterValue(parameters.get(i), low);
                    updateParameterValue(parameters.get(i + 1), high);
                    i++;
                }
            }
        }
    }

    private void startAutoUpdateTimer() {
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
        }

        autoUpdateTimer = new Timer(true);
        autoUpdateTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    if (isRunning) {
                        updateAutoParameters();
                    }
                }
            },
            1000,
            1000
        );
    }

    private void updateAutoParameters() {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    for (ParameterControl param : parameters) {
                        if (param.autoCheckBox.isSelected()) {
                            double currentValue = param.slider.getValue();
                            double range = param.max - param.min;
                            double variation = range * 0.02;

                            double newValue =
                                currentValue +
                                (random.nextGaussian() * variation);

                            newValue = Math.max(
                                param.min,
                                Math.min(param.max, newValue)
                            );

                            param.slider.setValue(newValue);
                            updateParameterValue(param, newValue);
                        }
                    }
                }
            }
        );
    }

    public float[] getCurrentValues() {
        float[] values = new float[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            values[i] = (float) parameters.get(i).slider.getValue();
        }
        return values;
    }

    public boolean isParameterAutomatic(int index) {
        if (index >= 0 && index < parameters.size()) {
            return parameters.get(index).autoCheckBox.isSelected();
        }
        return false;
    }

    public void start() {
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
        if (autoUpdateTimer != null) {
            autoUpdateTimer.cancel();
            autoUpdateTimer = null;
        }
    }

    public void showWindow() {
        if (stage == null) {
            stage = new Stage();
            stage.setTitle("Панель управления тестовыми параметрами");
            stage.setScene(new Scene(this, 800, 700));
            stage.setOnCloseRequest(e -> stop());
        }

        start();
        stage.show();
        stage.toFront();
    }

    public void hideWindow() {
        if (stage != null) {
            stage.hide();
        }
        stop();
    }

    private void showValueRangeAlert(ParameterControl param) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Неверное значение");
        alert.setHeaderText("Значение вне допустимого диапазона");
        alert.setContentText(
            String.format(
                "Значение должно быть в диапазоне от %.0f до %.0f %s",
                param.min,
                param.max,
                param.unit
            )
        );
        alert.showAndWait();
    }

    private void showInvalidNumberAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Неверный формат");
        alert.setHeaderText("Неверный формат числа");
        alert.setContentText(
            "Пожалуйста, введите корректное числовое значение."
        );
        alert.showAndWait();
    }

    public void setDataProvider(DataProvider provider) {
        this.dataProvider = provider;
    }

    public String[] getParameterNames() {
        String[] names = new String[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            names[i] = parameters.get(i).name;
        }
        return names;
    }

    public String[] getParameterUnits() {
        String[] units = new String[parameters.size()];
        for (int i = 0; i < parameters.size(); i++) {
            units[i] = parameters.get(i).unit;
        }
        return units;
    }
}
