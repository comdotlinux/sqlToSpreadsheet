package com.linux.batch;

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
import org.springframework.batch.item.ItemWriter;

/**
 *
 * @author comdotlinux
 */
public class SpreadsheetWriter implements ItemWriter<QueryResult> {

    private static final Logger LOG = Logger.getLogger(SpreadsheetWriter.class.getName());

    private final String outputDirectory;
    private final String outputFilename;
    private XSSFWorkbook workbook;
    private String outputFile;
    private final boolean useTheTimestamp;

    public SpreadsheetWriter(String outputDirectory, String outputFilename, long useTimestamp) {
        this.outputDirectory = outputDirectory;
        this.outputFilename = outputFilename;
        this.useTheTimestamp = useTimestamp == 1l;
    }

    @BeforeStep
    public void beforeStep() {
        this.workbook = new XSSFWorkbook();
        final String timestamp = DateFormatUtils.format(Calendar.getInstance(), "yyyyMMdd_HHmmss");
        StringBuilder outputBuilder = new StringBuilder(outputDirectory).append("/");
        if(useTheTimestamp) {
            outputBuilder.append("_");
            outputBuilder.append(timestamp);
        }
        outputBuilder.append(".xlsx");
        this.outputFile =  outputBuilder.toString();
    }

    public void write(List<? extends QueryResult> results) throws Exception {

        for (QueryResult qr : results) {

            XSSFSheet sheet = workbook.createSheet(qr.getOutputsheetname());

            List<Map<String, Object>> table = qr.getTableData();

            XSSFRow header = sheet.createRow(0);
            for (int t_row = 0; t_row < table.size(); t_row++) {
                XSSFRow row = sheet.createRow(t_row + 1);
                Map<String, Object> get = table.get(t_row);
                int t_col = 0;
                for (Map.Entry<String, Object> entry : get.entrySet()) {
                    if (t_row == 0) {
                        final String column_name = entry.getKey() != null ? entry.getKey().toUpperCase() : "";
                        createStringCell(header, column_name, t_col);
                    }
                    Object value = entry.getValue();
                    if (value instanceof Integer) {
                        createNumericCell(row, ((Integer) value).doubleValue(), t_col);
                    } else {
                        String cellValueAsString;
                        if (value instanceof Timestamp) {
                            Timestamp ts = (Timestamp) value;
                            cellValueAsString = ts.toString();
                        } else if (value instanceof Date) {
                            Date ts = (Date) value;
                            cellValueAsString = ts.toString();
                        } else if (value instanceof String) {
                            cellValueAsString = (String) value;
                        } else {
                            cellValueAsString = String.valueOf(value);
                        }
                        createStringCell(row, cellValueAsString, t_col);
                        t_col++;
                    }
                }
            }
        }
    }

    @AfterStep
    public void afterStep() {
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            workbook.write(fos);

        } catch (FileNotFoundException fnf) {
            LOG.log(Level.SEVERE, "The Output file could not be opened " + outputFile, fnf);

        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "There was an input output error while writing to " + outputFile, ioe);
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
