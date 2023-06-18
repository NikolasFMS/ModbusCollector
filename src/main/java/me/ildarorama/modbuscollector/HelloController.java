package me.ildarorama.modbuscollector;

import ch.qos.logback.classic.spi.ILoggingEvent;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import me.ildarorama.modbuscollector.support.DataPersister;
import me.ildarorama.modbuscollector.support.DeviceResponse;
import me.ildarorama.modbuscollector.support.ModbusWorkerTask;
import me.ildarorama.modbuscollector.support.ObjectPropertyBinding;
import me.ildarorama.modbuscollector.support.SettingsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    private Stage stage = null;
    @FXML
    private Label lblState;
    @FXML
    private Label param1;
    @FXML
    private Label param2;
    @FXML
    private Label param3;
    @FXML
    private Label param4;
    @FXML
    private Label param5;
    @FXML
    private Label param6;
    @FXML
    private Label param7;
    @FXML
    private Label param8;
    @FXML
    private Label param9;
    @FXML
    private Label param10;
    @FXML
    private DatePicker edtFrom;
    @FXML
    private DatePicker edtTo;
    @FXML
    private ListView<ILoggingEvent> lstLog;
    @FXML
    private TableView<DeviceResponse> tblData;
    private final ObservableList<DeviceResponse> items = FXCollections.observableArrayList();
    private DataPersister dataPersister;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");

    @FXML
    protected void onExportPressed() {
        if (edtFrom.getValue() == null || edtTo.getValue() == null || edtTo.getValue().compareTo(edtFrom.getValue()) < 0) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Ошибка");
            a.setContentText("Выберите корректный диаппазон");
            a.show();
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Файлы Excel", "*.xslx"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            dataPersister.saveExportToFile(file, edtFrom.getValue().atStartOfDay(), LocalDateTime.of(edtTo.getValue(), LocalTime.MAX));
            try {
                Desktop.getDesktop().open(file.getParentFile());
            } catch (IOException e) {
                log.error("Не могу открыть директорию с отчетом", e);
            }
        }
    }

    @FXML
    protected void onHelloButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 240);

        SettingsController ctrl = fxmlLoader.getController();
        Stage settingStage = new Stage();
        ctrl.setStage(settingStage);
        settingStage.setScene(scene);
        settingStage.setTitle("Настройки");
        settingStage.initModality(Modality.APPLICATION_MODAL);
        settingStage.initOwner(stage);
        settingStage.show();
    }

    public void setThread(ModbusWorkerTask thread) {
        lblState.textProperty().bind(thread.messageProperty());
        dataPersister = new DataPersister();
        thread.valueProperty().addListener((a, b, c) -> {
            dataPersister.persist(c);
            items.add(0, c);
        });


        param1.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a1"), thread.valueProperty()));
        param2.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a2"), thread.valueProperty()));
        param3.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a3"), thread.valueProperty()));
        param4.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a4"), thread.valueProperty()));
        param5.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a5"), thread.valueProperty()));
        param6.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a6"), thread.valueProperty()));
        param7.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a7"), thread.valueProperty()));
        param8.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a8"), thread.valueProperty()));
        param9.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a9"), thread.valueProperty()));
        param10.textProperty().bind(
                Bindings.createStringBinding(new ObjectPropertyBinding<>(thread.valueProperty(), "a10"), thread.valueProperty()));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        edtFrom.setValue(LocalDate.now());
        edtTo.setValue(LocalDate.now());
        lstLog.setCellFactory((param) ->
            new ListCell<ILoggingEvent>(){
                @Override
                public void updateItem(ILoggingEvent log, boolean empty) {
                    super.updateItem(log, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(String.format("%s - %s", LocalDateTime.ofEpochSecond(log.getTimeStamp(), 0, ZoneOffset.UTC).format(DATE_TIME_FORMATTER), log.getFormattedMessage()));
                        setGraphic(null);
                    }
                }
            }
        );
        lstLog.itemsProperty().bind(new SimpleObjectProperty<>(SettingsManager.getInstance().getLog()));

        TableColumn<DeviceResponse, String> dateColumn = new TableColumn<>("Дата");
        dateColumn.setPrefWidth(200);
        dateColumn.setCellValueFactory(v ->  new ReadOnlyObjectWrapper<>(v.getValue().getTimestamp().format(DATE_TIME_FORMATTER)));
        tblData.getColumns().add(dateColumn);

        TableColumn<DeviceResponse, String> param1 = new TableColumn<>("Параметр 1");
        param1.setCellValueFactory(new PropertyValueFactory<>("a1"));
        tblData.getColumns().add(param1);

        TableColumn<DeviceResponse, String> param2 = new TableColumn<>("Параметр 2");
        param2.setCellValueFactory(new PropertyValueFactory<>("a2"));
        tblData.getColumns().add(param2);

        TableColumn<DeviceResponse, String> param3 = new TableColumn<>("Параметр 3");
        param3.setCellValueFactory(new PropertyValueFactory<>("a3"));
        tblData.getColumns().add(param3);

        TableColumn<DeviceResponse, String> param4 = new TableColumn<>("Параметр 4");
        param4.setCellValueFactory(new PropertyValueFactory<>("a4"));
        tblData.getColumns().add(param4);

        TableColumn<DeviceResponse, String> param5 = new TableColumn<>("Параметр 5");
        param5.setCellValueFactory(new PropertyValueFactory<>("a5"));
        tblData.getColumns().add(param5);

        TableColumn<DeviceResponse, String> param6 = new TableColumn<>("Параметр 6");
        param6.setCellValueFactory(new PropertyValueFactory<>("a6"));
        tblData.getColumns().add(param6);

        TableColumn<DeviceResponse, String> param7 = new TableColumn<>("Параметр 7");
        param7.setCellValueFactory(new PropertyValueFactory<>("a7"));
        tblData.getColumns().add(param7);

        TableColumn<DeviceResponse, String> param8 = new TableColumn<>("Параметр 8");
        param8.setCellValueFactory(new PropertyValueFactory<>("a8"));
        tblData.getColumns().add(param8);

        TableColumn<DeviceResponse, String> param9 = new TableColumn<>("Параметр 9");
        param9.setCellValueFactory(new PropertyValueFactory<>("a9"));
        tblData.getColumns().add(param9);

        TableColumn<DeviceResponse, String> param10 = new TableColumn<>("Параметр 10");
        param10.setCellValueFactory(new PropertyValueFactory<>("a10"));
        tblData.getColumns().add(param10);

        tblData.setItems(items);
    }
}