package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.springframework.context.MessageSource;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.util.Locale;

public class OverviewPage implements Page {

    private static final By DATEPICKER_SELECTOR = By.id("datepicker");

    private final WebDriver driver;
    private final MessageSource messageSource;
    private final Locale locale;

    public OverviewPage(WebDriver driver, MessageSource messageSource, Locale locale) {
        this.driver = driver;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return datepickerExists(driver);
    }

    private static boolean datepickerExists(WebDriver driver) {
        return !driver.findElements(DATEPICKER_SELECTOR).isEmpty();
    }

    public boolean isVisibleForPerson(String username, int year) {
        final String titleText = messageSource.getMessage("overview.header.title", new Object[]{username, String.valueOf(year)}, locale);
        return driver.getTitle().contains(titleText);
    }
}
