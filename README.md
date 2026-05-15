# 🚀 Enterprise Naukri Auto-Apply Framework

An enterprise-grade, highly resilient automated job application framework built with **Java 17, Selenium 4, and Maven**. This tool autonomously searches and applies to jobs on Naukri.com, circumventing bot-detection, handling multi-step dynamic forms, and generating rich Excel-based analytics.

---

## 🏗️ Architecture & Design Patterns

This framework is built with scalability, maintainability, and stability in mind, employing industry-standard design patterns:

- **Page Object Model (POM):** UI interactions and locators are strictly separated from business logic (`BasePage`, `ApplyPage`).
- **Service-Oriented Architecture:** Core application logic is encapsulated in distinct services (`AutoApplyService`, `ExcelReportService`).
- **Thread-Local Driver Management:** The `DriverManager` implements thread-safe driver instantiation, ensuring future scalability for parallel execution.
- **Data-Driven Configuration:** All execution parameters are externalized into `config.properties`, requiring zero code changes for new runs.

---

## 📂 Project Structure

```text
src/main/java/com/naukri/
├── base/           # Core WebDriver utilities, safe clicks, JS injections
├── config/         # Environment and properties management
├── drivers/        # Chrome initialization, options, and anti-bot bypassing
├── models/         # POJOs/Data classes (JobListing)
├── pages/          # Page Object classes encapsulating Naukri UI elements
├── platforms/      # Platform-specific search and navigation logic
├── services/       # Application engine, loops, Excel I/O operations
└── utils/          # Randomization, wait mechanisms, retry logic, screenshots
```

---

## 🛠️ Technology Stack

| Component | Technology / Version | Purpose |
|-----------|----------------------|---------|
| **Language** | Java 17 | Core programming language |
| **Automation** | Selenium WebDriver 4.20.0 | Browser interaction and manipulation |
| **Test Runner** | TestNG 7.10.1 | Execution management and assertions |
| **Build Tool** | Apache Maven 3.x | Dependency management and build lifecycle |
| **Reporting** | Apache POI 5.2.5 | Excel (`.xlsx`) generation |
| **Logging** | Log4j2 | File and console logging |
| **Driver Setup**| WebDriverManager | Automated browser binary management |

---

## ⚙️ Prerequisites & Setup

1. **Java Runtime:** Install Java JDK 17 or higher.
2. **Maven:** Ensure Maven is added to your system `PATH`.
3. **Google Chrome:** Must be installed on the host machine.
4. **Active Profile:** You must have an active Naukri account logged into your Google Chrome browser profile.

---

## 📝 Configuration (`config.properties`)

Located in `src/main/resources/config.properties`. Update these parameters before execution:

```properties
# Search Parameters
keyword=Java Developer
experience=0-2 years
locations=Bangalore,Hyderabad,Chennai,Mumbai,Noida,Gurugram

# Execution Rules
maxApplications=100
headless=false

# System Paths
chromeProfilePath=C:/Users/rkumar72/Downloads/project/job silenium/chrome-profile
excelReportPath=reports/applied_jobs.xlsx
```

> **⚠️ CRITICAL:** Selenium requires exclusive access to the Chrome profile. Ensure all instances of Chrome using the specified `chromeProfilePath` are completely closed before execution.

---

## 🚀 Execution Guide

Run the framework directly via Maven:

```bash
mvn clean test
```

### 🧠 How the Engine Works (Workflow)
1. **Bootstrap:** Initializes Chrome with anti-bot arguments (`--disable-blink-features=AutomationControlled`) and attaches to the user session.
2. **Location Iteration:** Reads the comma-separated `locations` property and processes them sequentially.
3. **Dynamic Interrogation:** Extracts job cards, checking for exact matches and "Apply" button presence.
4. **Context Switching:** Opens jobs in new tabs, preserving the primary search context to prevent session staleness.
5. **Smart Form Processing:** Handles multi-step pagination, skips jobs demanding manual textual input, and successfully concludes upon reaching "Submit" or "Done".
6. **Graceful Degradation:** If an application fails, the engine captures a screenshot, logs the reason, and cleanly proceeds to the next job without halting the suite.

---

## 📊 Reporting & Analytics

All artifacts are automatically generated in the `reports/` directory:

- **`applied_jobs.xlsx`**: A detailed, tabular output of every job interacted with.
  - Columns: *Timestamp, Title, Company, Location, URL, Status (APPLIED, SKIPPED, FAILED), Reason, Platform*.
- **`automation.log`**: Extensive execution trace including info, warnings, and localized stack traces.
- **`screenshots/`**: Visual evidence captured instantaneously upon any application failure.
