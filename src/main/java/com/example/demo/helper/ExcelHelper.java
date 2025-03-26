package com.example.demo.helper;

import com.example.demo.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@Slf4j
public class ExcelHelper {

    private static final int BATCH_SIZE = 1000; // Batch size for processing rows

    public <T> byte[] exportToExcel(Stream<T> dataStream, String header) {

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(outputStream)) {

            Sheet sheet = workbook.createSheet("Data");
            Iterator<T> iterator = dataStream.iterator();
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException("Data stream cannot be empty.");
            }

            // Extract fields once (assume first element exists)
            T firstElement = iterator.next();
            Field[] fields = firstElement.getClass().getDeclaredFields();

            // Add Report Generated timestamp
            addReportGeneratedRow(sheet, header);

            // Create header row
            createHeaderRow(sheet, fields);

            int rowIndex = 2; // Start after timestamp and header row
            int batchCount = 0;
            List<T> batch = new ArrayList<>(BATCH_SIZE);

            do {
                batch.add(firstElement);
                batchCount++;

                if (batchCount >= BATCH_SIZE || !iterator.hasNext()) {
                    for (T data : batch) {
                        Row row = sheet.createRow(rowIndex++);
                        populateRow(row, data, fields);
                    }
                    batch.clear();
                    batchCount = 0;
                }
            } while (iterator.hasNext() && (firstElement = iterator.next()) != null);

            // Adjust column width
            autoSizeColumns(sheet, fields.length);

            workbook.write(bufferedOutput);
            bufferedOutput.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error while exporting file: {}", e.getMessage(), e);
            throw new RuntimeException("Error while generating Excel file", e);
        }
    }

    public <T> byte[] exportToExcelWithoutStreaming(List<T> dataList, String header) {
        if (dataList == null || dataList.isEmpty()) {
            throw new IllegalArgumentException("Data list cannot be empty.");
        }

        long startTime = System.currentTimeMillis();
        log.info("Exporting excel started.");

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); // Keep only 100 rows in memory
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BufferedOutputStream bufferedOutput = new BufferedOutputStream(outputStream)) {

            Sheet sheet = workbook.createSheet("Data");

            // Extract fields once to avoid multiple reflection calls
            Field[] fields = dataList.get(0).getClass().getDeclaredFields();

            // Add Report Generated on timestamp row
            addReportGeneratedRow(sheet, header);

            // Create header row
            createHeaderRow(sheet, fields);

            // Write data in batches for memory efficiency
            int totalRows = dataList.size();
            for (int startRow = 0; startRow < totalRows; startRow += BATCH_SIZE) {
                int endRow = Math.min(startRow + BATCH_SIZE, totalRows);
                populateDataRows(sheet, dataList.subList(startRow, endRow), fields, startRow + 2); // Start after the timestamp and header row
            }

            // Adjust column width based on data length
            autoSizeColumns(sheet, fields.length);

            workbook.write(bufferedOutput);
            bufferedOutput.flush();
            long endTime = System.currentTimeMillis();
            log.info("Exporting excel completed. Time taken: {} ms", (endTime - startTime));
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Error while exporting file: {}", e.getMessage(), e);
            throw new RuntimeException("Error while generating Excel file", e);
        }
    }


    private <T> void populateRow(Row row, T data, Field[] fields) {
        for (int i = 0; i < fields.length; i++) {
            fields[i].setAccessible(true);
            try {
                Object value = fields[i].get(data);
                row.createCell(i).setCellValue(value != null ? value.toString() : "");
            } catch (IllegalAccessException e) {
                log.error("Error accessing field: {}", fields[i].getName(), e);
            }
        }
    }

    private void addReportGeneratedRow(Sheet sheet, String header) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(header);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 255)); // Merge across all columns for the timestamp
    }

    private void createHeaderRow(Sheet sheet, Field[] fields) {
        Row headerRow = sheet.createRow(1); // Start header row after the timestamp row
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);

        for (int i = 0; i < fields.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(StringUtil.toNormalCase(fields[i].getName()));
            cell.setCellStyle(headerStyle);
        }
    }

    private <T> void populateDataRows(Sheet sheet, List<T> dataList, Field[] fields, int startRow) {
        IntStream.range(0, dataList.size()).forEach(i -> {
            Row row = sheet.createRow(startRow + i);
            for (int j = 0; j < fields.length; j++) {
                fields[j].setAccessible(true);
                try {
                    Object value = fields[j].get(dataList.get(i));
                    row.createCell(j).setCellValue(value != null ? value.toString() : "");
                } catch (IllegalAccessException e) {
                    log.error("Error accessing field: {}", fields[j].getName(), e);
                }
            }
        });
    }

    private void autoSizeColumns(Sheet sheet, int numColumns) {
        for (int i = 0; i < numColumns; i++) {
            int maxLength = 0;
            for (int rowNum = 0; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row != null) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        int length = cell.toString().getBytes().length;
                        if (length > maxLength) {
                            maxLength = length;
                        }
                    }
                }
            }
            // Adding extra padding to make the column more readable
            sheet.setColumnWidth(i, (maxLength + 2) * 256);
        }
    }
}
