package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.table.TableModel;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ExcelExporter {

    private ExcelExporter() {
    }

    public static void exportResults(
            Path filePath,
            String functionName,
            String startPoint,
            double epsilon,
            String resultSummary,
            TableModel tableModel
    ) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Результаты");
            CellStyle cellStyle = createCellStyle(workbook);

            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.setHeightInPoints(42);
            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(tableModel.getColumnName(col));
                cell.setCellStyle(cellStyle);
            }

            for (int row = 0; row < tableModel.getRowCount(); row++) {
                Row excelRow = sheet.createRow(rowIndex++);
                excelRow.setHeightInPoints(42);
                for (int col = 0; col < tableModel.getColumnCount(); col++) {
                    Cell cell = excelRow.createCell(col);
                    Object value = tableModel.getValueAt(row, col);
                    cell.setCellValue(cleanHtml(value == null ? "" : value.toString()));
                    cell.setCellStyle(cellStyle);
                }
            }

            for (int col = 0; col < tableModel.getColumnCount(); col++) {
                sheet.autoSizeColumn(col);
                int currentWidth = sheet.getColumnWidth(col);
                sheet.setColumnWidth(col, Math.min(currentWidth + 1000, 18000));
            }

            try (OutputStream outputStream = Files.newOutputStream(filePath)) {
                workbook.write(outputStream);
            }
        }
    }

    private static CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(false);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private static String cleanHtml(String value) {
        return value
                .replace("<html>", "")
                .replace("</html>", "")
                .replace("<br>", System.lineSeparator())
                .replace("&nbsp;", " ");
    }
}
