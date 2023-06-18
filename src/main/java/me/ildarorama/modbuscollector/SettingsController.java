package me.ildarorama.modbuscollector;

import com.fazecast.jSerialComm.SerialPort;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import me.ildarorama.modbuscollector.support.SettingsManager;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    private static final List<Integer> SPEEDS = List.of(
            115200, 57600, 34800, 19200, 9600);
    @FXML
    private ComboBox<String> cbxPort;
    @FXML
    private ComboBox<Integer> cbxSpeed;
    @FXML
    private Spinner<Integer> edtSlave;
    @FXML
    private Spinner<Double> edtModbusInterval;
    private Stage stage = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        var ports = Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).toList();

        cbxPort.getItems().addAll(ports);
        cbxSpeed.getItems().addAll(SPEEDS);

        SpinnerValueFactory.IntegerSpinnerValueFactory factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 255);
        edtSlave.setValueFactory(factory);

        SpinnerValueFactory.DoubleSpinnerValueFactory intervalFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1D, 60D, 0.1D, 0.1D);
        edtModbusInterval.setValueFactory(intervalFactory);

        SettingsManager manager = SettingsManager.getInstance();

        cbxPort.getSelectionModel().select(manager.getPort());
        edtModbusInterval.getValueFactory().setValue(manager.getPeriod());
        edtSlave.getValueFactory().setValue(manager.getSlave());
        cbxSpeed.getSelectionModel().select(manager.getSpeed());
    }

    @FXML
    public void btnCloseClick() {
        stage.close();
    }

    @FXML
    public void btnSaveClick() {
        SettingsManager manager = SettingsManager.getInstance();

        manager.setPort(cbxPort.getValue());
        manager.setPeriod(edtModbusInterval.getValue());
        manager.setSlave(edtSlave.getValue());
        manager.setSpeed(cbxSpeed.getValue());
        manager.save();
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
