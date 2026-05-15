package com.naukri.platforms;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.naukri.config.ConfigManager;
import com.naukri.models.JobListing;
import com.naukri.pages.ApplyPage;
import com.naukri.pages.BasePage;
import com.naukri.utils.WaitUtils;

public class NaukriPlatform extends BasePage {
    private static final Logger logger = LogManager.getLogger(NaukriPlatform.class);
    private ApplyPage applyPage;

    // Naukri Home Locators
    private final By searchInput = By.xpath("//input[contains(@class, 'suggestor-input') or contains(@placeholder, 'Skills') or contains(@placeholder, 'skills') or @name='keyword']");
    private final By experienceDropdown = By.id("experienceDD");
    private final By locationInput = By.xpath("//input[contains(@placeholder, 'location') or contains(@placeholder, 'Location') or @name='location']");
    private final By searchButton = By.xpath("//div[contains(@class, 'qsbSubmit')] | //button[contains(text(), 'Search') or contains(text(), 'SEARCH')] | //div[text()='Search'] | //button[@type='submit']");
    
    // Search Results Locators
    private final By jobCards = By.xpath("//div[contains(@class, 'srp-jobtuple-wrapper')]");
    private final By jobTitleLocator = By.xpath(".//a[@class='title ']");
    private final By companyLocator = By.xpath(".//a[contains(@class, 'comp-name')]");
    private final By locationLocator = By.xpath(".//span[contains(@class, 'locWdth')]");
    private final By nextPaginationButton = By.xpath("//a[contains(@class, 'styles_btn-secondary__2A-vY')]//span[text()='Next']");

    public NaukriPlatform(WebDriver driver) {
        super(driver);
        this.applyPage = new ApplyPage(driver);
    }

    public void login() {
        driver.get("https://www.naukri.com/");
        WaitUtils.longPause();
        if (isElementPresent(By.id("login_Layer"))) {
            logger.warn("Not logged in automatically via profile. Please ensure profile is correct or login manually.");
        } else {
            logger.info("Successfully loaded Naukri with user profile.");
        }
    }

    public void searchJobs(String keyword, String location, String experience) {
        logger.info("Searching for: " + keyword + " in " + location + " with exp: " + experience);
        driver.get("https://www.naukri.com/");
        WaitUtils.longPause();

        try {
            WebElement searchBox = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
            click(searchBox);
            WaitUtils.shortPause();
            searchBox.clear();
            searchBox.sendKeys(keyword);
        } catch (Exception e) {
            logger.warn("Could not interact with primary search input, attempting JS fallback");
            try {
                WebElement searchBox = driver.findElement(searchInput);
                jsClick(searchBox);
                searchBox.clear();
                searchBox.sendKeys(keyword);
            } catch (Exception ex) {
                logger.error("Completely failed to find search input");
                throw new RuntimeException("Search Input not found", ex);
            }
        }
        
        WaitUtils.shortPause();
        
        try {
            List<WebElement> locElements = driver.findElements(locationInput);
            WebElement locElement = locElements.size() > 1 ? locElements.get(1) : locElements.get(0);
            click(locElement);
            WaitUtils.shortPause();
            locElement.clear();
            locElement.sendKeys(location);
            WaitUtils.shortPause();
            locElement.sendKeys(Keys.ENTER);
        } catch (Exception e) {
            logger.warn("Failed to enter location: " + e.getMessage());
        }
        
        try {
            click(searchButton);
        } catch (Exception e) {
            logger.warn("Search button click failed or wasn't needed.");
        }
        WaitUtils.longPause();
        
        logger.info("Search executed.");
    }

    public List<JobListing> processCurrentPageJobs() {
        List<JobListing> processedJobs = new ArrayList<>();
        
        if (!isElementPresent(jobCards, 10)) {
            logger.warn("No job cards found on this page.");
            return processedJobs;
        }

        List<WebElement> cards = driver.findElements(jobCards);
        logger.info("Found " + cards.size() + " jobs on current page.");

        String originalWindow = driver.getWindowHandle();

        for (int i = 0; i < cards.size(); i++) {
            try {
                // Re-fetch to avoid stale elements
                cards = driver.findElements(jobCards);
                if (i >= cards.size()) break;
                
                WebElement card = cards.get(i);
                scrollToElement(card);
                WaitUtils.shortPause();

                String title = getTextSafe(card, jobTitleLocator);
                String company = getTextSafe(card, companyLocator);
                String loc = getTextSafe(card, locationLocator);
                
                logger.info("Processing job: " + title + " at " + company);
                
                // Click the job title to open in new tab (Naukri default behavior)
                WebElement titleLink = card.findElement(jobTitleLocator);
                String url = titleLink.getAttribute("href");
                
                JobListing job = JobListing.builder()
                        .title(title)
                        .company(company)
                        .location(loc)
                        .url(url)
                        .platform("Naukri")
                        .build();

                jsClick(titleLink);
                WaitUtils.mediumPause();

                // Switch to new tab
                Set<String> windows = driver.getWindowHandles();
                if (windows.size() > 1) {
                    for (String window : windows) {
                        if (!window.equals(originalWindow)) {
                            driver.switchTo().window(window);
                            break;
                        }
                    }

                    // Apply process
                    String applyResult = applyPage.applyToJob(title);
                    
                    if (applyResult.equals("SUCCESS")) {
                        job.setStatus("APPLIED");
                        job.setReason("Successfully applied");
                    } else if (applyResult.startsWith("ALREADY_APPLIED")) {
                        job.setStatus("SKIPPED");
                        job.setReason(applyResult);
                    } else if (applyResult.startsWith("SKIPPED")) {
                        job.setStatus("SKIPPED");
                        job.setReason(applyResult);
                    } else {
                        job.setStatus("FAILED");
                        job.setReason(applyResult);
                    }

                    processedJobs.add(job);

                    // Close tab and switch back
                    driver.close();
                    driver.switchTo().window(originalWindow);
                    WaitUtils.shortPause();
                } else {
                     logger.warn("New tab did not open for job: " + title);
                     job.setStatus("FAILED");
                     job.setReason("Failed to open job details tab");
                     processedJobs.add(job);
                }

            } catch (Exception e) {
                logger.error("Error processing a job card", e);
            }
        }

        return processedJobs;
    }

    public boolean nextPage() {
        if (isElementPresent(nextPaginationButton, 5)) {
            logger.info("Navigating to next page...");
            scrollToElement(nextPaginationButton);
            click(nextPaginationButton);
            WaitUtils.longPause();
            return true;
        }
        logger.info("No more pages available.");
        return false;
    }
}
