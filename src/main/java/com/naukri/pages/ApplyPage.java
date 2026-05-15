package com.naukri.pages;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.naukri.utils.ScreenshotUtils;
import com.naukri.utils.WaitUtils;

public class ApplyPage extends BasePage {
    private static final Logger logger = LogManager.getLogger(ApplyPage.class);

    // Right-side JD Panel Locators
    private final By applyButton = By.xpath("//button[contains(@class, 'apply-button')] | //button[text()='Apply'] | //button[text()='Easy Apply'] | //button[contains(text(), 'Apply Now')]");
    private final By alreadyAppliedText = By.xpath("//span[contains(text(),'Already applied')] | //div[contains(text(),'Already Applied')]");
    
    // Multi-step form locators
    private final By nextButton = By.xpath("//button[contains(text(), 'Next')] | //button[@type='submit' and contains(text(), 'Continue')]");
    private final By submitButton = By.xpath("//button[contains(text(), 'Submit')] | //button[@type='submit' and contains(text(), 'Submit application')]");
    private final By doneButton = By.xpath("//button[contains(text(), 'Done')] | //button[contains(text(), 'Return to job search')]");
    private final By successMessage = By.xpath("//*[contains(text(), 'Application submitted')] | //*[contains(text(), 'successfully applied')] | //*[contains(text(), 'Your application was sent')]");
    
    // Mandatory questions indicators
    private final By requiredQuestions = By.xpath("//span[contains(@class, 'required')] | //*[contains(text(), '*')]/ancestor::div[contains(@class, 'question')]");

    public ApplyPage(WebDriver driver) {
        super(driver);
    }

    public String applyToJob(String jobTitle) {
        WaitUtils.mediumPause();
        
        // 1. Check if already applied
        if (isElementPresent(alreadyAppliedText, 3)) {
            logger.info("Job already applied: " + jobTitle);
            return "ALREADY_APPLIED";
        }

        // 2. Detect Apply button
        if (!isElementPresent(applyButton, 5)) {
            logger.warn("Apply button not found for: " + jobTitle + ". Might be external.");
            return "EXTERNAL_OR_NO_APPLY";
        }

        try {
            logger.info("Clicking Apply button for: " + jobTitle);
            click(applyButton);
            WaitUtils.mediumPause();

            // 3. Handle multi-step forms
            return handleMultiStepForm();

        } catch (Exception e) {
            logger.error("Failed during apply process for " + jobTitle, e);
            ScreenshotUtils.takeScreenshot(driver, "apply_failed_" + jobTitle.replaceAll("[^a-zA-Z0-9]", "_"));
            return "FAILED_DURING_APPLY: " + e.getMessage();
        }
    }

    private String handleMultiStepForm() {
        int maxSteps = 10;
        int currentStep = 0;

        while (currentStep < maxSteps) {
            WaitUtils.shortPause();
            
            // Check for success message first
            if (isElementPresent(successMessage, 2)) {
                logger.info("Success message detected!");
                if (isElementPresent(doneButton, 2)) {
                    click(doneButton);
                    WaitUtils.shortPause();
                }
                return "SUCCESS";
            }

            // Check if there are too many unanswered mandatory questions (simplistic check)
            if (isElementPresent(requiredQuestions, 1)) {
                 logger.warn("Found mandatory questions. Skipping to avoid bad data.");
                 // Need a way to close the modal here if we abort. Often an X button or clicking outside.
                 // For now, returning skipped, assuming platform handler will refresh or close modal.
                 return "SKIPPED_MANDATORY_QUESTIONS";
            }

            // Look for Next or Submit
            if (isElementPresent(submitButton, 2)) {
                logger.info("Found Submit button. Clicking...");
                click(submitButton);
                WaitUtils.mediumPause();
                
                // Wait to see if success appears after submit
                if (isElementPresent(successMessage, 5)) {
                    logger.info("Success message detected after submit!");
                    if (isElementPresent(doneButton, 2)) {
                        click(doneButton);
                    }
                    return "SUCCESS";
                }
            } else if (isElementPresent(nextButton, 2)) {
                logger.info("Found Next button. Clicking...");
                click(nextButton);
            } else {
                // No next, no submit, no success. Might be a new tab or unhandled form type.
                logger.warn("Cannot find Next or Submit button. Form might be stuck.");
                return "FAILED_NO_NAVIGATION_BUTTONS";
            }

            currentStep++;
        }

        return "FAILED_TOO_MANY_STEPS";
    }
}
