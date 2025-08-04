package me.ildarorama.modbuscollector;

import com.fazecast.jSerialComm.SerialPort;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import me.ildarorama.modbuscollector.support.PortSpeedEnum;
import me.ildarorama.modbuscollector.support.SettingsManager;

public class SettingsController implements Initializable {

    @FXML
    private ComboBox<String> cbxPort;

    @FXML
    private ComboBox<PortSpeedEnum> cbxSpeed;

    @FXML
    private Spinner<Integer> edtSlave;

    @FXML
    private Spinner<Double> edtModbusInterval;

    @FXML
    private TextField edtDataSourceEndpoint;

    private Stage stage = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        List<String> ports = Arrays.stream(SerialPort.getCommPorts())
            .map(SerialPort::getSystemPortName)
            .collect(Collectors.toList());

        cbxSpeed.setConverter(
            new StringConverter<PortSpeedEnum>() {
                @Override
                public String toString(PortSpeedEnum user) {
                    if (user == null) {
                        return null;
                    } else {
                        return Integer.toString(user.speed());
                    }
                }

                @Override
                public PortSpeedEnum fromString(String id) {
                    return PortSpeedEnum.valueOf(id);
                }
            }
        );
        cbxPort.getItems().addAll(ports);
        cbxSpeed
            .getItems()
            .addAll(
                Arrays.stream(PortSpeedEnum.values()).collect(
                    Collectors.toList()
                )
            );

        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 255);
        edtSlave.setValueFactory(factory);

        SpinnerValueFactory.DoubleSpinnerValueFactory intervalFactory =
            new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.1D,
                60D,
                0.1D,
                0.1D
            );
        edtModbusInterval.setValueFactory(intervalFactory);

        SettingsManager manager = SettingsManager.getInstance();

        cbxPort.getSelectionModel().select(manager.getPort());
        edtModbusInterval.getValueFactory().setValue(manager.getPeriod());
        edtSlave.getValueFactory().setValue(manager.getSlave());
        cbxSpeed.getSelectionModel().select(manager.getSpeed());
        edtDataSourceEndpoint.setText(manager.getDataSourceUrl());
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
        manager.setDataSourceUrl(edtDataSourceEndpoint.getText());
        manager.save();
        stage.close();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
