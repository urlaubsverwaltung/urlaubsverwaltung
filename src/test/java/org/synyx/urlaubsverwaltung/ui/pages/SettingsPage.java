package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.synyx.urlaubsverwaltung.ui.Page;

public class SettingsPage implements Page {

    private static final By WORKING_TIME_TAB_SELECTOR = By.cssSelector("[data-test-id=settings-tab-working-time]");
    private static final By OVERTIME_ENABLED_SELECTOR = By.cssSelector("[data-test-id=setting-overtime-enabled]");
    private static final By SAVE_BUTTON_SELECTOR = By.cssSelector("[data-test-id=settings-save-button]");

    private final WebDriver driver;

    public SettingsPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return !driver.findElements(WORKING_TIME_TAB_SELECTOR).isEmpty();
    }

    public void clickWorkingTimeTab() {
        driver.findElement(WORKING_TIME_TAB_SELECTOR).click();
    }

    public void enableOvertime() {
        driver.findElement(OVERTIME_ENABLED_SELECTOR).click();
    }

    public boolean overtimeEnabled() {
        return driver.findElement(OVERTIME_ENABLED_SELECTOR).isSelected();
    }

    /**
     * Submits the setting form. Note that this method doesn't wait until something happens (e.g. submit button is stale for instance).
     * You may have to add a wait yourself after calling this method.
     */
    public void saveSettings() {
        driver.findElement(SAVE_BUTTON_SELECTOR).click();
    }
}
