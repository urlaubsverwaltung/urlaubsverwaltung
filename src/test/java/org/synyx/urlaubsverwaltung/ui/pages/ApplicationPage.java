package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.LocalDate;
import java.util.Locale;

import static java.time.format.DateTimeFormatter.ofPattern;

public class ApplicationPage implements Page {

    private static final By FROM_INPUT_SELECTOR = By.id("from");
    private static final By SUBMIT_SELECTOR = By.cssSelector("button#apply-application");

    private final WebDriver driver;
    private final MessageSource messageSource;
    private final Locale locale;

    public ApplicationPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return title(driver) && fromInputExists(driver);
    }

    public void from(LocalDate date) {
        final String dateString = ofPattern("dd.MM.yyyy").format(date);
        driver.findElement(FROM_INPUT_SELECTOR).sendKeys(dateString);
    }

    public void submit() {
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    private boolean title(WebDriver driver) {
        final String titleText = messageSource.getMessage("application.data.header.title.new", new Object[]{}, locale);
        return driver.getTitle().equals(titleText);
    }

    private static boolean fromInputExists(WebDriver driver) {
        return !driver.findElements(FROM_INPUT_SELECTOR).isEmpty();
    }
}
