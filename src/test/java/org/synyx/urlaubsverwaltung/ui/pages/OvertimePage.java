package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;

public class OvertimePage implements Page {

    private static final By START_DATE_SELECTOR = By.cssSelector("[data-test-id=overtime-start-date]");
    private static final By END_DATE_SELECTOR = By.cssSelector("[data-test-id=overtime-end-date]");
    private static final By HOURS_SELECTOR = By.cssSelector("[data-test-id=overtime-hours]");
    private static final By MINUTES_SELECTOR = By.cssSelector("[data-test-id=overtime-minutes]");
    private static final By SUBMIT_SELECTOR = By.cssSelector("[data-test-id=overtime-submit-button]");

    private final WebDriver driver;

    public OvertimePage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(START_DATE_SELECTOR).isEmpty()
            && !driver.findElements(END_DATE_SELECTOR).isEmpty()
            && !driver.findElements(SUBMIT_SELECTOR).isEmpty();
    }

    public void startDate(LocalDate startDate) {
        final String dateString = ofPattern("dd.MM.yyyy").format(startDate);
        driver.findElement(START_DATE_SELECTOR).sendKeys(dateString);
    }

    public void hours(int hours) {
        driver.findElement(HOURS_SELECTOR).sendKeys(String.valueOf(hours));
    }

    public void minutes(int minutes) {
        driver.findElement(MINUTES_SELECTOR).sendKeys(String.valueOf(minutes));
    }

    /**
     * Submits the overtime form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    public boolean showsEndDate(LocalDate endDate) {
        final String value = driver.findElement(END_DATE_SELECTOR).getAttribute("value");
        final String expectedDateString = ofPattern("dd.MM.yyyy").format(endDate);
        return value.equals(expectedDateString);
    }
}
