package com.naukri.tests;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.naukri.drivers.DriverManager;
import com.naukri.services.AutoApplyService;

public class AutoApplyTest {

    @BeforeSuite
    public void setup() {
        // Driver initialization is handled lazily in DriverManager, 
        // but we can enforce early init here if needed.
        DriverManager.getDriver();
    }

    @Test
    public void runAutoApply() {
        AutoApplyService service = new AutoApplyService();
        service.startEngine();
    }

    @AfterSuite
    public void teardown() {
        DriverManager.quitDriver();
    }
}
