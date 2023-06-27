package me.ildarorama.modbuscollector;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.sun.javafx.charts.Legend;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class HelloController implements Initializable {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);
    public static final List<String> PARAMS = Arrays.asList(
            "Тc.отраб.газов на входе",
            "Тc.отраб.газов на выходе",
            "ΔР отраб. газов",
            "Тс воды на входе",
            "Тс воды на выходе",
            "P воды на входе",
            "P воды на выходе",
            "ΔР воздух");

    private HostServices hostServices;
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
    private DatePicker edtFrom;
    @FXML
    private DatePicker edtTo;
    @FXML
    private ListView<ILoggingEvent> lstLog;
    @FXML
    private TableView<DeviceResponse> tblData;
    @FXML
    private LineChart<String, Number> chartMain;
    private XYChart.Series<String, Number> series1 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series2 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series3 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series4 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series5 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series6 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series7 = new XYChart.Series<>();
    private XYChart.Series<String, Number> series8 = new XYChart.Series<>();
    private final ObservableList<DeviceResponse> items = FXCollections.observableArrayList();
    private DataPersister dataPersister;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

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
            hostServices.showDocument("file://" + file);
        }
    }

    @FXML
    public void onExitClick() {
        Platform.exit();
        System.exit(0);
    }

    @FXML
    protected void onHelloButtonClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 240);

        SettingsController ctrl = fxmlLoader.getController();
        Stage settingStage = new Stage();
        settingStage.setResizable(false);
        settingStage.setMinHeight(300);
        settingStage.setMinWidth(600);

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
            series1.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA1()));
            series2.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA2()));
            series3.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA3()));
            series4.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA4()));
            series5.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA5()));
            series6.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA6()));
            series7.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA7()));
            series8.getData().add(new XYChart.Data<>(c.getTimestamp().format(TIME_FORMATTER), c.getA8()));

            if (series1.getData().size() > 30) {
                series1.getData().remove(0);
                series2.getData().remove(0);
                series3.getData().remove(0);
                series4.getData().remove(0);
                series5.getData().remove(0);
                series6.getData().remove(0);
                series7.getData().remove(0);
                series8.getData().remove(0);
            }
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
    }

    private void test() {
        chartMain.setAnimated(false);
        for (Node n : chartMain.getChildrenUnmodifiable()) {
            if (n instanceof Legend) {
                Legend l = (Legend) n;
                for (Legend.LegendItem li : l.getItems()) {
                    for (XYChart.Series<String, Number> s : chartMain.getData()) {
                        if (s.getName().equals(li.getText())) {
                            li.getSymbol().setCursor(Cursor.HAND); // Hint user that legend symbol is clickable
                            li.getSymbol().setOnMouseClicked(me -> {
                                if (me.getButton() == MouseButton.PRIMARY) {
                                    s.getNode().setVisible(!s.getNode().isVisible()); // Toggle visibility of line
                                    for (XYChart.Data<String, Number> d : s.getData()) {
                                        if (d.getNode() != null) {
                                            d.getNode().setVisible(s.getNode().isVisible()); // Toggle visibility of every node in the series
                                        }
                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        edtFrom.setValue(LocalDate.now());
        edtTo.setValue(LocalDate.now());
        lstLog.setCellFactory((param) ->
                new ListCell<ILoggingEvent>() {
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
        dateColumn.setCellValueFactory(v -> new ReadOnlyObjectWrapper<>(v.getValue().getTimestamp().format(DATE_TIME_FORMATTER)));
        tblData.getColumns().add(dateColumn);

        TableColumn<DeviceResponse, String> param1 = new TableColumn<>(PARAMS.get(0));
        param1.setCellValueFactory(new PropertyValueFactory<>("a1"));
        tblData.getColumns().add(param1);

        TableColumn<DeviceResponse, String> param2 = new TableColumn<>(PARAMS.get(1));
        param2.setCellValueFactory(new PropertyValueFactory<>("a2"));
        tblData.getColumns().add(param2);

        TableColumn<DeviceResponse, String> param3 = new TableColumn<>(PARAMS.get(2));
        param3.setCellValueFactory(new PropertyValueFactory<>("a3"));
        tblData.getColumns().add(param3);

        TableColumn<DeviceResponse, String> param4 = new TableColumn<>(PARAMS.get(3));
        param4.setCellValueFactory(new PropertyValueFactory<>("a4"));
        tblData.getColumns().add(param4);

        TableColumn<DeviceResponse, String> param5 = new TableColumn<>(PARAMS.get(4));
        param5.setCellValueFactory(new PropertyValueFactory<>("a5"));
        tblData.getColumns().add(param5);

        TableColumn<DeviceResponse, String> param6 = new TableColumn<>(PARAMS.get(5));
        param6.setCellValueFactory(new PropertyValueFactory<>("a6"));
        tblData.getColumns().add(param6);

        TableColumn<DeviceResponse, String> param7 = new TableColumn<>(PARAMS.get(6));
        param7.setCellValueFactory(new PropertyValueFactory<>("a7"));
        tblData.getColumns().add(param7);

        TableColumn<DeviceResponse, String> param8 = new TableColumn<>(PARAMS.get(7));
        param8.setCellValueFactory(new PropertyValueFactory<>("a8"));
        tblData.getColumns().add(param8);

        tblData.setItems(items);
        chartMain.setCreateSymbols(false);
        chartMain.legendVisibleProperty().set(true);
        series1.setName(PARAMS.get(0));
        series2.setName(PARAMS.get(1));
        series3.setName(PARAMS.get(2));
        series4.setName(PARAMS.get(3));
        series5.setName(PARAMS.get(4));
        series6.setName(PARAMS.get(5));
        series7.setName(PARAMS.get(6));
        series8.setName(PARAMS.get(7));
        chartMain.getData().addAll(series1, series2, series3, series4, series5, series6, series7, series8);
        test();
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}