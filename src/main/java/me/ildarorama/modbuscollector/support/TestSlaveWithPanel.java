package me.ildarorama.modbuscollector.support;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.procimg.InputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TestSlaveWithPanel extends Application {

    private static final int MODBUS_PORT = 5002;
    private static final int UNIT_ID = 1;
    private static final int START_REGISTER = 512;
    private static final int REGISTER_COUNT = 20;

    private ModbusSlave slave;
    private List<SimpleInputRegister> registers;
    private TestControlPanel controlPanel;
    private Timer updateTimer;
    private Random random = new Random();

    private volatile boolean serverRunning = false;
    private Label statusLabel;

    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("Modbus Test Server с панелью управления");
        System.out.println("===========================================");
        System.out.println("Запуск графического интерфейса...");

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Modbus Test Server - Панель управления");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        VBox topPanel = createServerControlPanel();
        root.setTop(topPanel);

        controlPanel = new TestControlPanel();
        root.setCenter(controlPanel);

        VBox bottomPanel = createInfoPanel();
        root.setBottom(bottomPanel);

        Scene scene = new Scene(root, 1000, 800);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(
            new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    stopServer();
                    controlPanel.stop();
                    Platform.exit();
                    System.exit(0);
                }
            }
        );

        primaryStage.show();

        startServer();
    }

    private VBox createServerControlPanel() {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: #ecf0f1; -fx-background-radius: 5px;"
        );

        Label titleLabel = new Label("Управление Modbus TCP сервером");
        titleLabel.setStyle(
            "-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"
        );

        Label serverInfoLabel = new Label(
            String.format(
                "Адрес: localhost:%d | Регистры: %d-%d",
                MODBUS_PORT,
                START_REGISTER,
                START_REGISTER + REGISTER_COUNT - 1
            )
        );
        serverInfoLabel.setStyle(
            "-fx-font-size: 12px; -fx-text-fill: #7f8c8d;"
        );

        HBox controlBox = new HBox(15);
        controlBox.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Остановлен");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        Button infoButton = new Button("Информация");
        infoButton.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white;"
        );
        infoButton.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    showServerInfo();
                }
            }
        );

        controlBox.getChildren().addAll(statusLabel, infoButton);

        panel.getChildren().addAll(titleLabel, serverInfoLabel, controlBox);
        return panel;
    }

    private VBox createInfoPanel() {
        VBox panel = new VBox(5);
        panel.setPadding(new Insets(10));
        panel.setStyle(
            "-fx-background-color: #34495e; -fx-background-radius: 5px;"
        );

        Label infoLabel1 = new Label("Этот сервер эмулирует ПР200 с данными");

        infoLabel1.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        panel.getChildren().addAll(infoLabel1);
        return panel;
    }

    private void toggleServer() {
        if (serverRunning) {
            stopServer();
        } else {
            startServer();
        }
    }

    private void startServer() {
        try {
            slave = ModbusSlaveFactory.createTCPSlave(MODBUS_PORT, 1);
            SimpleProcessImage img = new SimpleProcessImage(UNIT_ID);

            registers = new ArrayList<SimpleInputRegister>();
            for (int i = 0; i < REGISTER_COUNT; i++) {
                SimpleInputRegister reg = new SimpleInputRegister(0);
                img.addInputRegister(START_REGISTER + i, reg);
                registers.add(reg);
            }

            slave.addProcessImage(UNIT_ID, img);
            slave.open();

            startUpdateTimer();

            controlPanel.start();

            serverRunning = true;
            updateServerStatus();

            System.out.println(
                "Modbus TCP сервер запущен на порту " + MODBUS_PORT
            );
            System.out.println(
                "Доступны регистры " +
                START_REGISTER +
                "-" +
                (START_REGISTER + REGISTER_COUNT - 1)
            );
        } catch (ModbusException e) {
            showErrorAlert(
                "Ошибка запуска сервера",
                "Не удалось запустить Modbus сервер",
                "Возможно, порт " +
                MODBUS_PORT +
                " уже занят другим приложением.\n" +
                "Закройте другие экземпляры программы и попробуйте снова.\n\n" +
                "Ошибка: " +
                e.getMessage()
            );
        }
    }

    private void stopServer() {
        try {
            if (updateTimer != null) {
                updateTimer.cancel();
                updateTimer = null;
            }

            if (slave != null) {
                slave.close();
                slave = null;
            }

            controlPanel.stop();
            serverRunning = false;
            updateServerStatus();

            System.out.println("Modbus TCP сервер остановлен");
        } catch (Exception e) {
            System.err.println(
                "❌ Ошибка при остановке сервера: " + e.getMessage()
            );
        }
    }

    private void updateServerStatus() {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    if (serverRunning) {
                        statusLabel.setText("Работает");
                        statusLabel.setStyle(
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #27ae60;"
                        );
                    } else {
                        statusLabel.setText("Остановлен");
                        statusLabel.setStyle(
                            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #95a5a6;"
                        );
                    }
                }
            }
        );
    }

    private void startUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }

        updateTimer = new Timer("ModbusUpdateTimer", true);
        updateTimer.scheduleAtFixedRate(
            new TimerTask() {
                @Override
                public void run() {
                    updateRegisters();
                }
            },
            0,
            1000
        );
    }

    private void updateRegisters() {
        if (!serverRunning || registers == null) {
            return;
        }

        try {
            float[] values = controlPanel.getCurrentValues();

            for (int i = 0; i < values.length && i < 10; i++) {
                int[] regs = floatToRegisters(values[i]);

                int regIndex1 = i * 2;
                int regIndex2 = i * 2 + 1;

                if (
                    regIndex1 < registers.size() && regIndex2 < registers.size()
                ) {
                    registers.get(regIndex1).setValue(regs[0]);
                    registers.get(regIndex2).setValue(regs[1]);
                }
            }
        } catch (Exception e) {
            System.err.println(
                "❌ Ошибка при обновлении регистров: " + e.getMessage()
            );
        }
    }

    private int[] floatToRegisters(float value) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putFloat(value);
        bb.flip();

        ShortBuffer sb = bb.asShortBuffer();
        int low = sb.get() & 0xFFFF;
        int high = sb.get() & 0xFFFF;

        return new int[] { low, high };
    }

    private void showServerInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация о сервере");
        alert.setHeaderText("Modbus TCP Test Server");

        String info = String.format(
            "Протокол: Modbus TCP\n" +
            "Адрес: localhost (127.0.0.1)\n" +
            "Порт: %d\n" +
            "Регистры: %d-%d (%d регистров)\n" +
            "Параметры: 10\n" +
            "Обновление: каждую секунду\n" +
            "Тип данных: Float32 (2 регистра на значение)\n\n" +
            "Возможности панели управления:\n" +
            "• Ручная настройка каждого параметра\n" +
            "• Автоматическая генерация данных\n" +
            "• Реалистичные диапазоны значений\n" +
            "• Быстрые действия (сброс, случайные значения)\n\n" +
            "Для подключения ModbusCollector:\n" +
            "1. Запустить ModbusCollector\n" +
            "2. Настройки → Тип соединения: TCP\n" +
            "3. Адрес: localhost, Порт: %d\n",
            MODBUS_PORT,
            START_REGISTER,
            START_REGISTER + REGISTER_COUNT - 1,
            REGISTER_COUNT,
            MODBUS_PORT
        );

        alert.setContentText(info);

        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().setPrefHeight(400);

        alert.showAndWait();
    }

    private void showErrorAlert(
        final String title,
        final String header,
        final String content
    ) {
        Platform.runLater(
            new Runnable() {
                @Override
                public void run() {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle(title);
                    alert.setHeaderText(header);
                    alert.setContentText(content);
                    alert.getDialogPane().setPrefWidth(450);
                    alert.showAndWait();
                }
            }
        );
    }

    private static class AutoUpdateThread extends Thread {

        private final List<SimpleInputRegister> registers;
        private final TestControlPanel controlPanel;
        private final Random random = new Random();
        private volatile boolean running = true;

        public AutoUpdateThread(
            List<SimpleInputRegister> registers,
            TestControlPanel controlPanel
        ) {
            super("AutoUpdate");
            setDaemon(true);
            this.registers = registers;
            this.controlPanel = controlPanel;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    System.err.println(
                        "Ошибка в автоматическом обновлении: " + e.getMessage()
                    );
                }
            }
        }

        public void stopRunning() {
            running = false;
            interrupt();
        }
    }
}
