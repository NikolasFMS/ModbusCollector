package me.ildarorama.modbuscollector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.ildarorama.modbuscollector.support.ModbusWorkerTask;
import me.ildarorama.modbuscollector.support.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HelloApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(HelloApplication.class);

    private final ModbusWorkerTask thread = new ModbusWorkerTask();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setMinWidth(600);
        stage.setMinHeight(500);
        log.info("Запуск приложения");
        SettingsManager.getInstance().setSaveCallback(thread::refreshConfig);
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
        HelloController ctrl = fxmlLoader.getController();
        ctrl.setThread(thread);
        ctrl.setHostServices(getHostServices());
        stage.setTitle("Сборщик метрик");
        stage.setScene(scene);
        stage.show();
        new Thread(thread).start();
    }

    @Override
    public void stop(){
        thread.stopWorker();
        thread.cancel();
    }
    public static void main(String[] args) {
        launch();
    }
}