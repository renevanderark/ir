package nl.kb.dare.model.reporting;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public class ExcelReportBuilder {

    public void build(String sheetName, Iterator<ExcelReportRow> rows, OutputStream out) throws IOException {
        try (final SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            final SXSSFSheet sheet = workbook.createSheet(sheetName);
            sheet.trackAllColumnsForAutoSizing();
            sheet.createFreezePane(0, 1);
            final Row headerRow = sheet.createRow(0);

            headerRow.createCell(0).setCellValue("OAI_ID");
            headerRow.createCell(1).setCellValue("OAI_DATESTAMP");
            headerRow.createCell(2).setCellValue("STATUS_CODE");

            headerRow.createCell(3).setCellValue("MESSAGE");
            headerRow.createCell(4).setCellValue("URL");
            headerRow.createCell(5).setCellValue("IP_NAME");

            headerRow.createCell(6).setCellValue("STATE");
            headerRow.createCell(7).setCellValue("TS_CREATE");
            headerRow.createCell(8).setCellValue("TS_PROCESSED");

            int rowCount = 1;
            while (rows.hasNext()) {
                final ExcelReportRow row = rows.next();
                final Row sheetRow = sheet.createRow(rowCount++);
                sheetRow.createCell(0).setCellValue(row.getOaiId());
                sheetRow.createCell(1).setCellValue(row.getOaiDatestamp());
                sheetRow.createCell(2).setCellValue(row.getStatusCode());
                sheetRow.createCell(3).setCellValue(row.getMessage());
                sheetRow.createCell(4).setCellValue(row.getUrl());
                sheetRow.createCell(5).setCellValue(row.getIpName());
                sheetRow.createCell(6).setCellValue(row.getState());
                sheetRow.createCell(7).setCellValue(row.getTsCreate());
                sheetRow.createCell(8).setCellValue(row.getTsProcessed());
            }

            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            out.close();
            workbook.dispose();
        }
    }
}
