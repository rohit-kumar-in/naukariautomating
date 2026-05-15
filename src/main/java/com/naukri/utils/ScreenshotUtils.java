package com.naukri.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtils {
    private static final Logger logger = LogManager.getLogger(ScreenshotUtils.class);

    public static void takeScreenshot(WebDriver driver, String name) {
        if (driver instanceof TakesScreenshot) {
            TakesScreenshot screenshotDriver = (TakesScreenshot) driver;
            File screenshotFile = screenshotDriver.getScreenshotAs(OutputType.FILE);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "reports/screenshots/" + name + "_" + timestamp + ".png";
            
            try {
                Path destPath = Paths.get(fileName);
                Files.createDirectories(destPath.getParent());
                Files.copy(screenshotFile.toPath(), destPath);
                logger.info("Screenshot saved to: " + fileName);
            } catch (IOException e) {
                logger.error("Failed to save screenshot", e);
            }
        }
    }
}
