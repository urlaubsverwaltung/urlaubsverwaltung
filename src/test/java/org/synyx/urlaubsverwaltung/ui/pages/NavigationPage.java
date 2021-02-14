package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.synyx.urlaubsverwaltung.ui.Page;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.elementHasAttributeWithValue;

public class NavigationPage implements Page {

    private static final By NEW_APPLICATION_SELECTOR = By.id("application-new-link");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final AvatarMenu avatarMenu;

    public NavigationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, 5);
        this.avatarMenu = new AvatarMenu(driver);
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return newApplicationLinkVisible(driver);
    }

    public void logout() {
        avatarMenu.logout();
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

    private static class AvatarMenu {

        private static final By AVATAR_SELECTOR = By.cssSelector("[data-test-id=avatar]");
        private static final By AVATAR_POPUPMENU_SELECTOR = By.cssSelector("[data-test-id=avatar-popupmenu]");
        private static final By LOGOUT_SELECTOR = By.cssSelector("[data-test-id=logout]");

        private final WebDriver driver;
        private final WebDriverWait wait;

        AvatarMenu(WebDriver driver) {
            this.driver = driver;
            this.wait = new WebDriverWait(driver, 5);
        }

        void logout() {

            wait.until(elementHasAttributeWithValue(AVATAR_SELECTOR, "aria-expanded", "false"));
            wait.until(elementHasAttributeWithValue(AVATAR_POPUPMENU_SELECTOR, "aria-hidden", "true"));

            driver.findElement(AVATAR_SELECTOR).click();

            wait.until(elementHasAttributeWithValue(AVATAR_SELECTOR, "aria-expanded", "true"));
            wait.until(elementHasAttributeWithValue(AVATAR_POPUPMENU_SELECTOR, "aria-hidden", "false"));
            wait.until(elementToBeClickable(LOGOUT_SELECTOR));

            driver.findElement(LOGOUT_SELECTOR).click();
        }
    }
}
