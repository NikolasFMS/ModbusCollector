package me.ildarorama.modbuscollector.support;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javafx.scene.control.Alert;
import me.ildarorama.modbuscollector.MainController;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataPersister {

    private static final Logger log = LoggerFactory.getLogger(
        DataPersister.class
    );
    private Connection conn;
    private PreparedStatement stmt;

    public DataPersister() {
        initDb();
    }

    private void initDb() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:collector.db");
            conn
                .createStatement()
                .execute(
                    "create table if not exists log(ID INTEGER PRIMARY KEY AUTOINCREMENT, STMP DATETIME NOT NULL, A1 REAL,  A2 REAL,  A3 REAL, A4 REAL, A5 REAL, A6 REAL, A7 REAL, A8 REAL, A9 REAL, A10 REAL)"
                );
            stmt = conn.prepareStatement(
                "INSERT INTO log(STMP, A1, A2, A3, A4, A5, A6, A7, A8, A9, A10) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );
        } catch (Exception e) {
            log.error("Не могу инициализировать базу данных", e);
        }
    }

    public ResultSet report(LocalDateTime from, LocalDateTime to) {
        try {
            long tsFrom = from.toInstant(ZoneOffset.UTC).toEpochMilli();
            long tsTo = to.toInstant(ZoneOffset.UTC).toEpochMilli();
            String s = String.format(
                "select id, stmp, a1, a2, a3, a4, a5, a6, a7, a8 from log where stmp > %d and stmp < %d",
                tsFrom,
                tsTo
            );
            return conn.createStatement().executeQuery(s);
        } catch (Exception e) {
            log.error("Ошибка при выгрузки отчета", e);
        }
        return null;
    }

    public void saveExportToFile(
        File file,
        LocalDateTime from,
        LocalDateTime to
    ) {
        try {
            try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ResultSet result = report(from, to)
            ) {
                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(
                    createHelper
                        .createDataFormat()
                        .getFormat("dd.MM.yyyy h:mm:ss")
                );
                cellStyle.setAlignment(HorizontalAlignment.LEFT);

                XSSFSheet sheet = workbook.createSheet("Выгрузка");
                sheet.setColumnWidth(0, 5000);
                sheet.setColumnWidth(1, 5000);
                sheet.setColumnWidth(2, 5000);
                sheet.setColumnWidth(3, 5000);
                sheet.setColumnWidth(4, 5000);
                sheet.setColumnWidth(5, 5000);
                sheet.setColumnWidth(6, 5000);
                sheet.setColumnWidth(7, 5000);
                sheet.setColumnWidth(8, 5000);
                sheet.setColumnWidth(9, 5000);

                int rowCount = 0;

                Row row = sheet.createRow(0);

                Cell cell = row.createCell(0);
                cell.setCellValue("Дата");

                int idx = 1;
                for (String param : MainController.PARAMS) {
                    cell = row.createCell(idx);
                    cell.setCellValue(param);
                    idx++;
                }
                while (result.next()) {
                    row = sheet.createRow(++rowCount);

                    cell = row.createCell(0);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(result.getTimestamp(2));

                    cell = row.createCell(1);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(3))
                    );

                    cell = row.createCell(2);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(4))
                    );

                    cell = row.createCell(3);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(5))
                    );

                    cell = row.createCell(4);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(6))
                    );

                    cell = row.createCell(5);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(7))
                    );

                    cell = row.createCell(6);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(8))
                    );

                    cell = row.createCell(7);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(9))
                    );

                    cell = row.createCell(8);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(10))
                    );

                    cell = row.createCell(9);
                    cell.setCellValue(
                        String.format("%.2f", result.getFloat(11))
                    );
                }

                try (
                    FileOutputStream outputStream = new FileOutputStream(file)
                ) {
                    workbook.write(outputStream);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка сбора отчета", e);
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Ошибка сбора отчета: " + e.getMessage());
            a.showAndWait();
        }
    }

    public void persist(DeviceResponse resp) {
        if (stmt != null) {
            try {
                stmt.setTimestamp(
                    1,
                    java.sql.Timestamp.valueOf(resp.getTimestamp())
                );
                stmt.setFloat(2, resp.getA1());
                stmt.setFloat(3, resp.getA2());
                stmt.setFloat(4, resp.getA3());
                stmt.setFloat(5, resp.getA4());
                stmt.setFloat(6, resp.getA5());
                stmt.setFloat(7, resp.getA6());
                stmt.setFloat(8, resp.getA7());
                stmt.setFloat(9, resp.getA8());
                stmt.setFloat(10, resp.getA9());
                stmt.setFloat(11, resp.getA10());
                stmt.executeUpdate();
            } catch (SQLException e) {
                log.error("Не могу сохранить запись в БД", e);
            }
        }
    }
}
