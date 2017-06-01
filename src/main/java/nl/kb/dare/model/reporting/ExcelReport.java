package nl.kb.dare.model.reporting;

import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ExcelReport {

    private final Iterator<StoredErrorReport> result;

    ExcelReport(Iterator<StoredErrorReport> result) {
        this.result = result;

    }


    public void build(OutputStream out) throws IOException, SQLException {
        final SXSSFWorkbook workbook = new SXSSFWorkbook();

        final Map<String, Integer> rowCounts = new HashMap<>();
        while(result.hasNext()) {
            final StoredErrorReport row = result.next();
            System.out.println(row);
/*
            final String sheetName = String.format("%d - %s",
                    ((BigDecimal) row.get("repository_id")).intValue(), row.get("name"));

            if (workbook.getSheet(sheetName) == null) {
                final SXSSFSheet sheet = workbook.createSheet(sheetName);
                sheet.createFreezePane(0, 1);
                final Row headerRow = sheet.createRow(0);
                headerRow.createCell(0).setCellValue("DARE_PREPROCES_ID");
                headerRow.createCell(0).setCellValue("TS_CREATE");
            }

            if (!rowCounts.containsKey(sheetName)) {
                rowCounts.put(sheetName, 1);
            }

            final Integer currentRow = rowCounts.get(sheetName);
            final SXSSFSheet sheet = workbook.getSheet(sheetName);
            final Row sheetRow = sheet.createRow(currentRow);

            System.out.println(row.entrySet());
            sheetRow.createCell(0).setCellValue(((BigDecimal)row.get("dare_preproces_id")).longValue());
            sheetRow.createCell(1).setCellValue(((TIMESTAMP) row.get("ts_create")).stringValue());

            rowCounts.put(sheetName, currentRow + 1);*/
        }

        workbook.write(out);
        out.close();
        workbook.dispose();
    }
}
