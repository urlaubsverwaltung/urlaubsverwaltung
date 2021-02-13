package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;

public class ApplicationPage implements Page {

    private static final By FROM_INPUT_SELECTOR = By.id("from");
    private static final By SUBMIT_SELECTOR = By.cssSelector("button#apply-application");

    private final WebDriver driver;

    public ApplicationPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return title(driver) && fromInputVisible(driver);
    }

    public void from(LocalDate date) {
        final String dateString = ofPattern("dd.MM.yyyy").format(date);
        driver.findElement(FROM_INPUT_SELECTOR).sendKeys(dateString);
    }

    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    private static boolean title(WebDriver driver) {
        return driver.getTitle().equals("New vacation request");
    }

    private static boolean fromInputVisible(WebDriver driver) {
        final WebElement element = driver.findElement(FROM_INPUT_SELECTOR);
        return element != null && element.isDisplayed();
    }
}
