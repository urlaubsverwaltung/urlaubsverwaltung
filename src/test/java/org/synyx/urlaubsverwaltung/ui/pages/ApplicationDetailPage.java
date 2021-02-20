package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.synyx.urlaubsverwaltung.ui.Page;

public class ApplicationDetailPage implements Page {

    private static final By ALERT_SUCCESS_SELECTOR = By.className("alert-success");

    private final WebDriver driver;

    public ApplicationDetailPage(WebDriver driver) {
        this.driver = driver;
    }

    public boolean isVisibleForPerson(String username) {
        return driver.getTitle().equals("Vacation request of " + username);
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return driver.getTitle().startsWith("Vacation request of");
    }

    public boolean showsApplicationCreatedInfo() {
        final WebElement element = driver.findElement(ALERT_SUCCESS_SELECTOR);
        return element != null && element.isDisplayed();
    }
}
