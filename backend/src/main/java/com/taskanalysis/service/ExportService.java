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

            // Add performance metrics section
            rowNum += 2; // Add spacing
            rowNum = addPerformanceMetricsSection(sheet, task, rowNum, workbook);

            // Add subtask-level metrics section
            rowNum += 2; // Add spacing
            addSubtaskMetricsSection(sheet, task, rowNum, workbook);

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

    /**
     * Create metrics header cell style
     */
    private CellStyle createMetricsHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create metrics data cell style
     */
    private CellStyle createMetricsDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    /**
     * Add performance metrics section to the sheet
     */
    private int addPerformanceMetricsSection(Sheet sheet, Task task, int startRow, Workbook workbook) {
        CellStyle metricsHeaderStyle = createMetricsHeaderStyle(workbook);
        CellStyle metricsDataStyle = createMetricsDataStyle(workbook);

        int rowNum = startRow;

        // Section title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 TELJESÍTMÉNY METRIKÁK");
        titleCell.setCellStyle(metricsHeaderStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                rowNum - 1, rowNum - 1, 0, 5
        ));

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Mutató", "Tervezett", "Tényleges", "Egység", "Értékelés", "Megjegyzés"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(metricsHeaderStyle);
        }

        // Planned Total Time
        if (task.getPlannedTotalTimeMinutes() != null) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("Tervezett teljes idő");
            row.createCell(1).setCellValue(task.getPlannedTotalTimeMinutes());
            row.createCell(2).setCellValue(task.getTotalActualTimeSeconds() != null 
                    ? task.getTotalActualTimeSeconds() / 60.0 : 0);
            row.createCell(3).setCellValue("perc");
            row.createCell(4).setCellValue(getTimeVariance(
                    task.getPlannedTotalTimeMinutes(), 
                    task.getTotalActualTimeSeconds()));
            row.createCell(5).setCellValue(getTimeVarianceComment(
                    task.getPlannedTotalTimeMinutes(), 
                    task.getTotalActualTimeSeconds()));
            applyCellStyle(row, metricsDataStyle);
        }

        // Efficiency Score
        Row efficiencyRow = sheet.createRow(rowNum++);
        efficiencyRow.createCell(0).setCellValue("Hatékonysági mutató");
        efficiencyRow.createCell(1).setCellValue(task.getPlannedEfficiencyScore() != null 
                ? String.format("%.3f", task.getPlannedEfficiencyScore()) : "-");
        efficiencyRow.createCell(2).setCellValue(task.getActualEfficiencyScore() != null 
                ? String.format("%.3f", task.getActualEfficiencyScore()) : "-");
        efficiencyRow.createCell(3).setCellValue("pont/perc");
        efficiencyRow.createCell(4).setCellValue(getEfficiencyRating(task.getActualEfficiencyScore()));
        efficiencyRow.createCell(5).setCellValue("Nagyobb érték = gyorsabb pontszerzés");
        applyCellStyle(efficiencyRow, metricsDataStyle);

        // Time per Point
        Row timePerPointRow = sheet.createRow(rowNum++);
        timePerPointRow.createCell(0).setCellValue("Időráfordítás/pont");
        timePerPointRow.createCell(1).setCellValue(task.getPlannedTimePerPoint() != null 
                ? String.format("%.2f", task.getPlannedTimePerPoint()) : "-");
        timePerPointRow.createCell(2).setCellValue(task.getActualTimePerPoint() != null 
                ? String.format("%.2f", task.getActualTimePerPoint()) : "-");
        timePerPointRow.createCell(3).setCellValue("perc/pont");
        timePerPointRow.createCell(4).setCellValue(getTimePerPointRating(task.getActualTimePerPoint()));
        timePerPointRow.createCell(5).setCellValue("Kisebb érték = hatékonyabb");
        applyCellStyle(timePerPointRow, metricsDataStyle);

        // Efficiency Variance
        if (task.getEfficiencyVariancePercent() != null) {
            Row varianceRow = sheet.createRow(rowNum++);
            varianceRow.createCell(0).setCellValue("Hatékonysági eltérés");
            varianceRow.createCell(1).setCellValue("-");
            varianceRow.createCell(2).setCellValue(String.format("%.1f%%", task.getEfficiencyVariancePercent()));
            varianceRow.createCell(3).setCellValue("%");
            varianceRow.createCell(4).setCellValue(getVarianceRating(task.getEfficiencyVariancePercent()));
            varianceRow.createCell(5).setCellValue(getVarianceComment(task.getEfficiencyVariancePercent()));
            applyCellStyle(varianceRow, metricsDataStyle);
        }

        // Total Points
        Row pointsRow = sheet.createRow(rowNum++);
        pointsRow.createCell(0).setCellValue("Összes pontszám");
        pointsRow.createCell(1).setCellValue(task.getTotalPlannedPoints() != null 
                ? task.getTotalPlannedPoints() : 0);
        pointsRow.createCell(2).setCellValue(task.getTotalActualPoints() != null 
                ? task.getTotalActualPoints() : 0);
        pointsRow.createCell(3).setCellValue("pont");
        pointsRow.createCell(4).setCellValue(getPointsAccuracy(
                task.getTotalPlannedPoints(), 
                task.getTotalActualPoints()));
        pointsRow.createCell(5).setCellValue("Becslési pontosság");
        applyCellStyle(pointsRow, metricsDataStyle);

        return rowNum; // Return the current row number for the next section
    }

    /**
     * Add subtask-level metrics section to the sheet
     */
    private void addSubtaskMetricsSection(Sheet sheet, Task task, int startRow, Workbook workbook) {
        List<Subtask> subtasks = task.getSubtasks();
        if (subtasks == null || subtasks.isEmpty()) {
            return;
        }

        CellStyle subtaskHeaderStyle = createSubtaskMetricsHeaderStyle(workbook);
        CellStyle goodStyle = createGoodPerformanceStyle(workbook);
        CellStyle averageStyle = createAveragePerformanceStyle(workbook);
        CellStyle poorStyle = createPoorPerformanceStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);

        int rowNum = startRow;

        // Section title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📊 RÉSZFELADAT SZINTŰ METRIKÁK");
        titleCell.setCellStyle(subtaskHeaderStyle);
        sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(
                rowNum - 1, rowNum - 1, 0, 9
        ));

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "Részfeladat #", 
            "Arányos tervezett idő (perc)", 
            "Tényleges idő (perc)", 
            "Tervezett hatékonyság (pont/perc)", 
            "Tényleges hatékonyság (pont/perc)",
            "Hatékonysági eltérés (%)",
            "Tervezett idő/pont (perc)",
            "Tényleges idő/pont (perc)",
            "Időeltérés (%)",
            "Értékelés"
        };
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(subtaskHeaderStyle);
        }

        // Data rows for each subtask
        for (Subtask subtask : subtasks) {
            Row row = sheet.createRow(rowNum++);

            // Subtask number
            Cell cell0 = row.createCell(0);
            cell0.setCellValue(subtask.getSubtaskNumber());
            cell0.setCellStyle(dataStyle);

            // Proportional planned time
            Cell cell1 = row.createCell(1);
            Integer proportionalTime = subtask.getProportionalPlannedTimeMinutes();
            if (proportionalTime != null) {
                cell1.setCellValue(proportionalTime);
            } else {
                cell1.setCellValue("-");
            }
            cell1.setCellStyle(dataStyle);

            // Actual time (convert seconds to minutes)
            Cell cell2 = row.createCell(2);
            Integer actualTimeSeconds = subtask.getTotalActualTimeSeconds();
            if (actualTimeSeconds != null && actualTimeSeconds > 0) {
                cell2.setCellValue(String.format("%.1f", actualTimeSeconds / 60.0));
            } else {
                cell2.setCellValue("-");
            }
            cell2.setCellStyle(dataStyle);

            // Planned efficiency score
            Cell cell3 = row.createCell(3);
            Double plannedEfficiency = subtask.getPlannedEfficiencyScore();
            if (plannedEfficiency != null) {
                cell3.setCellValue(String.format("%.3f", plannedEfficiency));
            } else {
                cell3.setCellValue("-");
            }
            cell3.setCellStyle(dataStyle);

            // Actual efficiency score
            Cell cell4 = row.createCell(4);
            Double actualEfficiency = subtask.getActualEfficiencyScore();
            if (actualEfficiency != null) {
                cell4.setCellValue(String.format("%.3f", actualEfficiency));
            } else {
                cell4.setCellValue("-");
            }
            cell4.setCellStyle(dataStyle);

            // Efficiency variance percent
            Cell cell5 = row.createCell(5);
            Double efficiencyVariance = subtask.getEfficiencyVariancePercent();
            CellStyle varianceStyle = getVarianceCellStyle(efficiencyVariance, goodStyle, averageStyle, poorStyle, dataStyle);
            if (efficiencyVariance != null) {
                cell5.setCellValue(String.format("%.1f%%", efficiencyVariance));
            } else {
                cell5.setCellValue("-");
            }
            cell5.setCellStyle(varianceStyle);

            // Planned time per point
            Cell cell6 = row.createCell(6);
            Double plannedTimePerPoint = subtask.getPlannedTimePerPoint();
            if (plannedTimePerPoint != null) {
                cell6.setCellValue(String.format("%.2f", plannedTimePerPoint));
            } else {
                cell6.setCellValue("-");
            }
            cell6.setCellStyle(dataStyle);

            // Actual time per point
            Cell cell7 = row.createCell(7);
            Double actualTimePerPoint = subtask.getActualTimePerPoint();
            if (actualTimePerPoint != null) {
                cell7.setCellValue(String.format("%.2f", actualTimePerPoint));
            } else {
                cell7.setCellValue("-");
            }
            cell7.setCellStyle(dataStyle);

            // Time variance percent
            Cell cell8 = row.createCell(8);
            Double timeVariance = subtask.getTimeVariancePercent();
            CellStyle timeVarianceStyle = getTimeVarianceCellStyle(timeVariance, goodStyle, averageStyle, poorStyle, dataStyle);
            if (timeVariance != null) {
                cell8.setCellValue(String.format("%.1f%%", timeVariance));
            } else {
                cell8.setCellValue("-");
            }
            cell8.setCellStyle(timeVarianceStyle);

            // Performance evaluation
            Cell cell9 = row.createCell(9);
            String evaluation = getSubtaskEvaluation(efficiencyVariance, timeVariance);
            cell9.setCellValue(evaluation);
            cell9.setCellStyle(varianceStyle);
        }

        // Legend row
        rowNum++;
        Row legendRow = sheet.createRow(rowNum);
        Cell legendLabel = legendRow.createCell(0);
        legendLabel.setCellValue("Jelmagyarázat:");
        legendLabel.setCellStyle(dataStyle);
        
        Cell legendGood = legendRow.createCell(1);
        legendGood.setCellValue("🟢 >10% jobb");
        legendGood.setCellStyle(goodStyle);
        
        Cell legendAverage = legendRow.createCell(2);
        legendAverage.setCellValue("🟡 ±10% közel");
        legendAverage.setCellStyle(averageStyle);
        
        Cell legendPoor = legendRow.createCell(3);
        legendPoor.setCellValue("🔴 <-10% gyengébb");
        legendPoor.setCellStyle(poorStyle);

        // Auto-size new columns
        for (int i = 6; i < 10; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * Get cell style based on variance value
     */
    private CellStyle getVarianceCellStyle(Double variance, CellStyle goodStyle, CellStyle averageStyle, CellStyle poorStyle, CellStyle defaultStyle) {
        if (variance == null) return defaultStyle;
        if (variance > 10.0) return goodStyle;
        if (variance >= -10.0) return averageStyle;
        return poorStyle;
    }

    /**
     * Get cell style based on time variance (inverse - lower is better)
     */
    private CellStyle getTimeVarianceCellStyle(Double variance, CellStyle goodStyle, CellStyle averageStyle, CellStyle poorStyle, CellStyle defaultStyle) {
        if (variance == null) return defaultStyle;
        if (variance < -10.0) return goodStyle; // Significantly faster is good
        if (variance <= 10.0) return averageStyle; // Within ±10% is average
        return poorStyle; // Significantly slower is poor
    }

    /**
     * Get subtask performance evaluation
     */
    private String getSubtaskEvaluation(Double efficiencyVariance, Double timeVariance) {
        if (efficiencyVariance == null && timeVariance == null) {
            return "Nincs adat";
        }
        
        if (efficiencyVariance != null) {
            if (efficiencyVariance > 20.0) return "🟢 Kiváló";
            if (efficiencyVariance > 10.0) return "🟢 Jó";
            if (efficiencyVariance >= -10.0) return "🟡 Átlagos";
            if (efficiencyVariance >= -20.0) return "🟠 Gyenge";
            return "🔴 Rossz";
        }
        
        // Fallback to time variance if efficiency variance not available
        if (timeVariance < -20.0) return "🟢 Kiváló (gyors)";
        if (timeVariance < -10.0) return "🟢 Jó (gyors)";
        if (timeVariance <= 10.0) return "🟡 Átlagos";
        if (timeVariance <= 20.0) return "🟠 Lassú";
        return "🔴 Nagyon lassú";
    }

    /**
     * Create subtask metrics header style
     */
    private CellStyle createSubtaskMetricsHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Create good performance cell style (green background)
     */
    private CellStyle createGoodPerformanceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create average performance cell style (yellow background)
     */
    private CellStyle createAveragePerformanceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Create poor performance cell style (red background)
     */
    private CellStyle createPoorPerformanceStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Apply cell style to all cells in a row
     */
    private void applyCellStyle(Row row, CellStyle style) {
        for (int i = 0; i < 6; i++) {
            Cell cell = row.getCell(i);
            if (cell != null) {
                cell.setCellStyle(style);
            }
        }
    }

    /**
     * Get efficiency rating based on score (points per minute)
     * 0.1 pont/perc = 1 pont 10 percenként
     * 0.05 pont/perc = 1 pont 20 percenként
     */
    private String getEfficiencyRating(Double score) {
        if (score == null) return "-";
        if (score >= 0.1) return "🟢 Kiváló";
        if (score >= 0.05) return "🟡 Jó";
        if (score >= 0.025) return "🟠 Átlagos";
        return "🔴 Alacsony";
    }

    /**
     * Get time per point rating
     */
    private String getTimePerPointRating(Double timePerPoint) {
        if (timePerPoint == null) return "-";
        if (timePerPoint <= 5.0) return "🟢 Kiváló";
        if (timePerPoint <= 10.0) return "🟡 Jó";
        if (timePerPoint <= 20.0) return "🟠 Átlagos";
        return "🔴 Lassú";
    }

    /**
     * Get variance rating
     */
    private String getVarianceRating(Double variance) {
        if (variance == null) return "-";
        if (variance >= 20.0) return "🟢 Jóval jobb";
        if (variance >= 0) return "🟡 Jobb";
        if (variance >= -20.0) return "🟠 Gyengébb";
        return "🔴 Rosszabb";
    }

    /**
     * Get variance comment
     */
    private String getVarianceComment(Double variance) {
        if (variance == null) return "";
        if (variance > 0) return "A tervezettnél hatékonyabb voltál";
        if (variance == 0) return "Pontosan a tervnek megfelelően";
        return "A tervezettnél kevésbé hatékony";
    }

    /**
     * Get time variance evaluation
     */
    private String getTimeVariance(Integer plannedMinutes, Integer actualSeconds) {
        if (plannedMinutes == null || actualSeconds == null) return "-";
        double actualMinutes = actualSeconds / 60.0;
        double variance = ((actualMinutes - plannedMinutes) / plannedMinutes) * 100;
        if (Math.abs(variance) <= 10) return "🟢 Pontos";
        if (variance < 0) return "🟡 Gyorsabb";
        return "🔴 Lassabb";
    }

    /**
     * Get time variance comment
     */
    private String getTimeVarianceComment(Integer plannedMinutes, Integer actualSeconds) {
        if (plannedMinutes == null || actualSeconds == null) return "";
        double actualMinutes = actualSeconds / 60.0;
        double variance = ((actualMinutes - plannedMinutes) / plannedMinutes) * 100;
        return String.format("%.1f%% eltérés", variance);
    }

    /**
     * Get points accuracy
     */
    private String getPointsAccuracy(Integer planned, Integer actual) {
        if (planned == null || actual == null || planned == 0) return "-";
        double accuracy = ((double) actual / planned) * 100;
        if (Math.abs(accuracy - 100) <= 10) return "🟢 Pontos";
        if (accuracy > 100) return "🟡 Túlteljesítés";
        return "🔴 Alulteljesítés";
    }
}
