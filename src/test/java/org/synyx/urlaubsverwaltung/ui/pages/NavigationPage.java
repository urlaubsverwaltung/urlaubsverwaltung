package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.synyx.urlaubsverwaltung.ui.Page;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

public class NavigationPage implements Page {

    private static final By NEW_APPLICATION_SELECTOR = By.id("application-new-link");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public NavigationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 20);
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return newApplicationLinkVisible(driver);
    }

    /**
     * Click "new application" link.
     */
    public void clickNewApplication() {
        wait.until(elementToBeClickable(NEW_APPLICATION_SELECTOR));
        driver.findElement(NEW_APPLICATION_SELECTOR).click();
    }

    private static boolean newApplicationLinkVisible(WebDriver driver) {
        final WebElement element = driver.findElement(NEW_APPLICATION_SELECTOR);
        return element != null && element.isDisplayed();
    }
}
