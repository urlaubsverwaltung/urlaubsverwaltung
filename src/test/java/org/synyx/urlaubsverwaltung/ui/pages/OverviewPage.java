package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.synyx.urlaubsverwaltung.ui.Page;

public class OverviewPage implements Page {

    private static final By DATEPICKER_SELECTOR = By.id("datepicker");

    private final WebDriver driver;

    public OverviewPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return datepickerExists(driver);
    }

    private static boolean datepickerExists(WebDriver driver) {
        return !driver.findElements(DATEPICKER_SELECTOR).isEmpty();
    }

    public boolean isVisibleForPerson(String username) {
        return driver.getTitle().contains(String.format("Overview of %s from", username));
    }
}
