package me.ildarorama.modbuscollector.support;

import javafx.scene.control.Alert;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class DataPersister {
    private static final Logger log = LoggerFactory.getLogger(DataPersister.class);

    private Connection conn;
    private PreparedStatement stmt;

    public DataPersister() {
        initDb();
    }

    private void initDb() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:collector.db");
            conn.createStatement().execute("""
                            create table if not exists log(ID INTEGER PRIMARY KEY AUTOINCREMENT, STMP DATETIME NOT NULL, A INT);
                    """);
            stmt = conn.prepareStatement("""
                    INSERT INTO log(STMP, A) values (?, ?)
                    """);
        } catch (Exception e) {
            log.error("Не могу инициализировать базу данных", e);
        }
    }

    public ResultSet report(LocalDateTime from, LocalDateTime to) {
        try {
            var selectStmt = conn.prepareStatement("select * from log where stmp > ? and stmp < ?");
            stmt.setLong(1, from.toInstant(ZoneOffset.UTC).toEpochMilli());
            stmt.setLong(2, to.toInstant(ZoneOffset.UTC).toEpochMilli());

            var result = selectStmt.execute();
            if (result) {
                return selectStmt.getResultSet();
            }
        } catch (Exception e) {
            log.error("Ошибка при выгрузки отчета", e);
        }
        return null;
    }


    public void saveExportToFile(File file, LocalDateTime from, LocalDateTime to) {
        try {

            try (XSSFWorkbook workbook = new XSSFWorkbook();
                 ResultSet result = report(from, to)) {

                CellStyle cellStyle = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                cellStyle.setDataFormat(
                        createHelper.createDataFormat().getFormat("dd.MM.yyyy h:mm:ss"));

                XSSFSheet sheet = workbook.createSheet("Выгрузка");
                sheet.setColumnWidth(0, 5000);

                int rowCount = 0;

                Row row = sheet.createRow(++rowCount);

                Cell cell = row.createCell(0);
                cell.setCellValue("Дата");

                cell = row.createCell(1);
                cell.setCellValue("А");

                while (result.next()) {
                    row = sheet.createRow(++rowCount);

                    cell = row.createCell(0);
                    cell.setCellStyle(cellStyle);
                    cell.setCellValue(result.getTimestamp(1));

                    cell = row.createCell(1);
                    cell.setCellValue(result.getInt(2));
                }

                try (FileOutputStream outputStream = new FileOutputStream(file)) {
                    workbook.write(outputStream);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка сбора отчета", e);
            new Alert(Alert.AlertType.ERROR).showAndWait();
        }
    }

    public void persist(DeviceResponse resp) {
        if (stmt != null) {
            try {
                stmt.setTimestamp(1, java.sql.Timestamp.valueOf(resp.getTimestamp()));
                stmt.setInt(2, resp.getA1());
                stmt.executeUpdate();
            } catch (SQLException e) {
                log.error("Не могу сохранить запись в БД", e);
            }
        }
    }
}
