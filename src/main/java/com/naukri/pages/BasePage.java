package com.naukri.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Actions actions;
    protected JavascriptExecutor js;
    private static final Logger logger = LogManager.getLogger(BasePage.class);

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        this.actions = new Actions(driver);
        this.js = (JavascriptExecutor) driver;
    }

    protected void click(By locator) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (StaleElementReferenceException e) {
            logger.warn("Stale element encountered on click. Retrying...");
            driver.findElement(locator).click();
        } catch (Exception e) {
            logger.warn("Standard click failed. Attempting JS click on: " + locator);
            jsClick(locator);
        }
    }
    
    protected void click(WebElement element) {
         try {
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (StaleElementReferenceException e) {
            logger.warn("Stale element encountered on click. Retrying...");
            element.click();
        } catch (Exception e) {
            logger.warn("Standard click failed. Attempting JS click");
            jsClick(element);
        }
    }

    protected void jsClick(By locator) {
        WebElement element = driver.findElement(locator);
        jsClick(element);
    }
    
    protected void jsClick(WebElement element) {
        js.executeScript("arguments[0].click();", element);
    }

    protected void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText();
    }
    
    protected String getTextSafe(By locator) {
        try {
            List<WebElement> elements = driver.findElements(locator);
            if (!elements.isEmpty()) {
                return elements.get(0).getText();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
    
    protected String getTextSafe(WebElement parent, By locator) {
         try {
            List<WebElement> elements = parent.findElements(locator);
            if (!elements.isEmpty()) {
                return elements.get(0).getText();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }

    protected void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        scrollToElement(element);
    }
    
    protected void scrollToElement(WebElement element) {
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
    }

    protected boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }
    
    protected boolean isElementPresent(By locator, int timeoutSec) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSec));
            shortWait.until(ExpectedConditions.presenceOfElementLocated(locator));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean isElementVisible(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
