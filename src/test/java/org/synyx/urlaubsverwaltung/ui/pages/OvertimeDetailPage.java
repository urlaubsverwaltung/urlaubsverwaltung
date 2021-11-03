package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.synyx.urlaubsverwaltung.ui.Page;

public class OvertimeDetailPage implements Page {

    private static final By ALERT_SUCCESS_SELECTOR = By.className("alert-success");
    private static final By PERSON_SELECTOR = By.cssSelector("[data-test-id=overtime-person]");
    private static final By DURATION_SELECTOR = By.cssSelector("[data-test-id=overtime-duration]");

    private final WebDriver driver;

    public OvertimeDetailPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isVisibleForPerson(String username) {
        return driver.findElement(PERSON_SELECTOR).getText().strip().startsWith(username);
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(PERSON_SELECTOR).isEmpty() && !driver.findElements(DURATION_SELECTOR).isEmpty();
    }

    public boolean showsOvertimeCreatedInfo() {
        final WebElement element = driver.findElement(ALERT_SUCCESS_SELECTOR);
        return element != null && element.isDisplayed();
    }

    public boolean showsHours(int hours) {
        final String durationText = driver.findElement(DURATION_SELECTOR).getText().strip();
        // this test may be a false positive when the expected hours are the same as the actual minutes of the overtime entry
        // while the overtime entry contains minutes only... improve it when you need it.
        return durationText.startsWith(hours + " ");
    }

    public boolean showsMinutes(int minutes) {
        final String durationText = driver.findElement(DURATION_SELECTOR).getText().strip();
        // check if the second digit chunk matches the expected minutes
        // this test fails when the overtime entry contains minutes only, of course. improve it when you need it.
        return durationText.matches(String.format("^\\d+ \\w+\\. %d \\w+\\.", minutes));
    }
}
