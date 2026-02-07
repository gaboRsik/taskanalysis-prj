package com.taskanalysis.service;

import com.taskanalysis.entity.Subtask;
import com.taskanalysis.entity.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    /**
     * Generate Excel file for task export
     *
     * @param task Task to export
     * @return Excel file as byte array
     * @throws IOException if file generation fails
     */
    public byte[] generateExcelExport(Task task) throws IOException {
        log.info("Generating Excel export for task: {}", task.getName());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sanitizeSheetName(task.getName()));

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle summaryStyle = createSummaryStyle(workbook);

            int rowNum = 0;

            // Create header row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Részfeladat #", "Idő", "Tervezett pont", "Tényleges pont", "Feladat", "Kategória"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            List<Subtask> subtasks = task.getSubtasks();
            long totalSeconds = 0;
            int totalPlannedPoints = 0;
            int totalActualPoints = 0;

            for (Subtask subtask : subtasks) {
                Row row = sheet.createRow(rowNum++);

                // Calculate subtask time
                long subtaskSeconds = calculateSubtaskTime(subtask);
                totalSeconds += subtaskSeconds;

                // Subtask number
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(subtask.getSubtaskNumber());
                cell0.setCellStyle(dataStyle);

                // Time
                Cell cell1 = row.createCell(1);
                cell1.setCellValue(formatDuration(subtaskSeconds));
                cell1.setCellStyle(dataStyle);

                // Planned points
                Cell cell2 = row.createCell(2);
                if (subtask.getPlannedPoints() != null) {
                    cell2.setCellValue(subtask.getPlannedPoints());
                    totalPlannedPoints += subtask.getPlannedPoints();
                } else {
                    cell2.setCellValue("-");
                }
                cell2.setCellStyle(dataStyle);

                // Actual points
                Cell cell3 = row.createCell(3);
                if (subtask.getActualPoints() != null) {
                    cell3.setCellValue(subtask.getActualPoints());
                    totalActualPoints += subtask.getActualPoints();
                } else {
                    cell3.setCellValue("-");
                }
                cell3.setCellStyle(dataStyle);

                // Task name
                Cell cell4 = row.createCell(4);
                cell4.setCellValue(task.getName());
                cell4.setCellStyle(dataStyle);

                // Category name
                Cell cell5 = row.createCell(5);
                cell5.setCellValue(task.getCategory() != null ? task.getCategory().getName() : "Nincs kategória");
                cell5.setCellStyle(dataStyle);
            }

            // Add summary row
            Row summaryRow = sheet.createRow(rowNum);
            Cell summaryLabel = summaryRow.createCell(0);
            summaryLabel.setCellValue("Összesen");
            summaryLabel.setCellStyle(summaryStyle);

            Cell summaryTime = summaryRow.createCell(1);
            summaryTime.setCellValue(formatDuration(totalSeconds));
            summaryTime.setCellStyle(summaryStyle);

            Cell summaryPlanned = summaryRow.createCell(2);
            summaryPlanned.setCellValue(totalPlannedPoints);
            summaryPlanned.setCellStyle(summaryStyle);

            Cell summaryActual = summaryRow.createCell(3);
            summaryActual.setCellValue(totalActualPoints);
            summaryActual.setCellStyle(summaryStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            log.info("Excel export generated successfully");
            return outputStream.toByteArray();
        }
    }

    /**
     * Calculate total time for a subtask
     */
    private long calculateSubtaskTime(Subtask subtask) {
        return subtask.getTimeEntries().stream()
                .filter(entry -> entry.getEndTime() != null)
                .mapToLong(entry -> Duration.between(entry.getStartTime(), entry.getEndTime()).getSeconds())
                .sum();
    }

    /**
     * Format duration in HH:MM:SS
     */
    private String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Sanitize sheet name (Excel has restrictions)
     */
    private String sanitizeSheetName(String name) {
        String sanitized = name.replaceAll("[\\\\/*\\[\\]:?]", "_");
        return sanitized.length() > 31 ? sanitized.substring(0, 31) : sanitized;
    }

    /**
     * Create header cell style
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create data cell style
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create summary cell style
     */
    private CellStyle createSummaryStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
