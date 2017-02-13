package com.linux.sql.to.spreadsheet.batch;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.batch.core.annotation.AfterStep;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;

/**
 *
 * @author comdotlinux
 */
@StepScope
public class SpreadsheetWriter implements ItemWriter<List<Map<String, Object>>> {

    private static final String FILE_NAME = "target/output";

    @Override
    public void write(List<? extends List<Map<String, Object>>> tables) throws Exception {

        String dateTime = DateFormatUtils.format(Calendar.getInstance(),
                "yyyyMMdd_HHmmss");
        String outputFilename = FILE_NAME + "_" + dateTime + ".xlsx";
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Testing");

        List<Map<String, Object>> table = tables.get(0);

        XSSFRow header = sheet.createRow(0);
        for (int t_row = 0; t_row < table.size(); t_row++) {
            XSSFRow row = sheet.createRow(t_row + 1);
            Map<String, Object> get = table.get(t_row);
            int t_col = 0;
            for (Map.Entry<String, Object> entry : get.entrySet()) {
                if (t_row == 0) {
                    createStringCell(header, entry.getKey(), t_col);
                }
                Object value = entry.getValue();
                if (value instanceof Integer) {
                    createNumericCell(row, ((Integer) value).doubleValue(), t_col);
                } else if (value instanceof Timestamp) {
                    Timestamp ts = (Timestamp) value;
                    createStringCell(row, ts.toString(), t_col);
                } else if (value instanceof Date) {
                    Date ts = (Date) value;
                    createStringCell(row, ts.toString(), t_col);
                } else if (value instanceof String) {
                    createStringCell(row, (String) value, t_col);
                } else {
                    createStringCell(row, String.valueOf(value), t_col);
                }
                t_col++;
            }

        }

        try (FileOutputStream fos = new FileOutputStream(outputFilename)) {
            workbook.write(fos);
        } catch (FileNotFoundException fnf) {
            Logger.getLogger(SpreadsheetWriter.class.getName()).log(Level.SEVERE, null, fnf);
        } catch (IOException ioe) {
            Logger.getLogger(SpreadsheetWriter.class.getName()).log(Level.SEVERE, null, ioe);
        }
    }

    private void createNumericCell(Row row, Double val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
        cell.setCellValue(val);
    }

    private void createStringCell(Row row, String val, int col) {
        Cell cell = row.createCell(col);
        cell.setCellType(Cell.CELL_TYPE_STRING);
        cell.setCellValue(val);
    }
}
