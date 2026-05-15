package com.naukri.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import com.naukri.config.ConfigManager;
import com.naukri.drivers.DriverManager;
import com.naukri.models.JobListing;
import com.naukri.platforms.NaukriPlatform;

public class AutoApplyService {
    private static final Logger logger = LogManager.getLogger(AutoApplyService.class);
    private NaukriPlatform platform;
    
    private int successfulApplies = 0;
    private int skippedJobs = 0;
    private int failedJobs = 0;
    private int totalProcessed = 0;

    public void startEngine() {
        ExcelReportService.initializeExcelReport();
        
        WebDriver driver = DriverManager.getDriver();
        platform = new NaukriPlatform(driver);

        String keyword = ConfigManager.getProperty("keyword");
        String experience = ConfigManager.getProperty("experience");
        String[] locations = ConfigManager.getProperty("locations").split(",");
        int targetApplications = ConfigManager.getIntProperty("maxApplications");

        platform.login();

        for (String location : locations) {
            location = location.trim();
            if (successfulApplies >= targetApplications) {
                break;
            }

            logger.info("==========================================");
            logger.info("Starting search for location: " + location);
            logger.info("==========================================");

            try {
                platform.searchJobs(keyword, location, experience);
                
                boolean hasNextPage = true;
                while (hasNextPage && successfulApplies < targetApplications) {
                    List<JobListing> processedOnPage = platform.processCurrentPageJobs();
                    
                    for (JobListing job : processedOnPage) {
                        totalProcessed++;
                        if ("APPLIED".equals(job.getStatus())) {
                            successfulApplies++;
                        } else if ("SKIPPED".equals(job.getStatus())) {
                            skippedJobs++;
                        } else {
                            failedJobs++;
                        }
                        ExcelReportService.appendJob(job);
                    }
                    
                    logger.info("Progress: " + successfulApplies + "/" + targetApplications + " successful applications.");

                    if (successfulApplies >= targetApplications) {
                        break;
                    }

                    hasNextPage = platform.nextPage();
                }
            } catch (Exception e) {
                logger.error("Error processing location: " + location, e);
            }
        }

        printSummary();
    }

    private void printSummary() {
        logger.info("\n==========================================");
        logger.info("AUTOMATION RUN SUMMARY");
        logger.info("==========================================");
        logger.info("Total Processed : " + totalProcessed);
        logger.info("Successfully Applied : " + successfulApplies);
        logger.info("Skipped : " + skippedJobs);
        logger.info("Failed : " + failedJobs);
        logger.info("==========================================");

        if (successfulApplies >= ConfigManager.getIntProperty("maxApplications")) {
            System.out.println("\n100 APPLICATION TARGET COMPLETED\n");
            logger.info("100 APPLICATION TARGET COMPLETED");
        } else {
            logger.info("Run finished before hitting target. Reached: " + successfulApplies);
        }
    }
}
