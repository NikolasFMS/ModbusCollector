package me.ildarorama.modbuscollector.support;

import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class SettingsManager {
    private static class SettingManagerHolder {
        private static final SettingsManager instance = new SettingsManager();
        static {
            instance.load();
        }
    }

    private SettingsManager() {}

    public static SettingsManager getInstance() {
        return SettingManagerHolder.instance;
    }
    private Runnable saveCallback = null;
    private String port = "";
    private PortSpeedEnum speed = PortSpeedEnum.SPEED_9600;
    private double period = 1;
    private int slave = 1;

    private ObservableList<ILoggingEvent> log = FXCollections.observableArrayList();

    public void setSaveCallback(Runnable callback) {
        saveCallback = callback;
    }

    public void load() {
        Properties properties = new Properties();
        File file = new File("settings.properties");
        if (file.exists()) {
            try(InputStream is = new FileInputStream(file)) {
                properties.load(is);
                speed = PortSpeedEnum.valueOf(properties.getProperty("speed", "19200"));
                slave = Integer.valueOf(properties.getProperty("slave", "1"));
                port = properties.getProperty("port");
                period = Double.valueOf(properties.getProperty("period", "1"));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        Properties properties = new Properties();
        File file = new File("settings.properties");
            try(OutputStream is = new FileOutputStream(file)) {
                properties.setProperty("speed", speed.name());
                properties.setProperty("period", Double.toString(period));
                properties.setProperty("slave", Integer.toString(slave));
                properties.setProperty("port", port);
                properties.store(is, "Modbus collector settings file");
                if (saveCallback != null) {
                    saveCallback.run();
                }
            } catch(Exception e) {
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Ошибка");
                a.setContentText("Ошибка сохранения настроек");
                a.showAndWait();
            }
    }

    public ObservableList<ILoggingEvent> getLog() {
        return log;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public PortSpeedEnum getSpeed() {
        return speed;
    }

    public void setSpeed(PortSpeedEnum speed) {
        this.speed = speed;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public int getSlave() {
        return slave;
    }

    public void setSlave(int slave) {
        this.slave = slave;
    }
}
