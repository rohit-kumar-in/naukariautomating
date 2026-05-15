package com.naukri.drivers;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import com.naukri.config.ConfigManager;

import io.github.bonigarcia.wdm.WebDriverManager;

public class DriverManager {
    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver getDriver() {
        if (driverThreadLocal.get() == null) {
            initDriver();
        }
        return driverThreadLocal.get();
    }

    private static void initDriver() {
        logger.info("Initializing ChromeDriver...");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        
        // Critical Chrome Options
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);
        options.setExperimentalOption("detach", true);

        // User Profile Support
        String profilePath = ConfigManager.getProperty("chromeProfilePath");
        if (profilePath != null && !profilePath.isEmpty()) {
            logger.info("Using Chrome Profile: " + profilePath);
            options.addArguments("user-data-dir=" + profilePath);
        }
        
        if (ConfigManager.getBooleanProperty("headless")) {
            options.addArguments("--headless=new");
        }

        try {
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
            driverThreadLocal.set(driver);
            logger.info("ChromeDriver initialized successfully.");
        } catch (Exception e) {
            logger.error("Failed to initialize ChromeDriver", e);
            throw new RuntimeException("Could not start Chrome Browser", e);
        }
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            logger.info("Quitting ChromeDriver...");
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}
