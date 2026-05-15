package com.naukri.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.naukri.config.ConfigManager;
import com.naukri.models.JobListing;

public class ExcelReportService {
    private static final Logger logger = LogManager.getLogger(ExcelReportService.class);
    private static final String FILE_PATH = ConfigManager.getProperty("excelReportPath");
    private static final String[] HEADERS = {
        "Timestamp", "Title", "Company", "Location", "URL", "Status", "Reason", "Platform"
    };

    public static void initializeExcelReport() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            return;
        }

        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Applied Jobs");
            Row headerRow = sheet.createRow(0);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
            workbook.close();
            logger.info("Initialized new Excel report at: " + FILE_PATH);
        } catch (IOException e) {
            logger.error("Failed to initialize Excel report", e);
        }
    }

    public static synchronized void appendJobs(List<JobListing> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return;
        }

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {
            
            Sheet sheet = workbook.getSheet("Applied Jobs");
            if (sheet == null) {
                sheet = workbook.createSheet("Applied Jobs");
            }
            
            int lastRowNum = sheet.getLastRowNum();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            for (JobListing job : jobs) {
                lastRowNum++;
                Row row = sheet.createRow(lastRowNum);
                
                row.createCell(0).setCellValue(timestamp);
                row.createCell(1).setCellValue(job.getTitle());
                row.createCell(2).setCellValue(job.getCompany());
                row.createCell(3).setCellValue(job.getLocation());
                row.createCell(4).setCellValue(job.getUrl());
                row.createCell(5).setCellValue(job.getStatus());
                row.createCell(6).setCellValue(job.getReason());
                row.createCell(7).setCellValue(job.getPlatform());
            }

            try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
                workbook.write(fos);
            }
            logger.info("Appended " + jobs.size() + " jobs to Excel report.");
            
        } catch (IOException e) {
            logger.error("Failed to append jobs to Excel report", e);
        }
    }
    
    public static synchronized void appendJob(JobListing job) {
        appendJobs(List.of(job));
    }
}
