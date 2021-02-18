package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.synyx.urlaubsverwaltung.ui.Page;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;
import static org.openqa.selenium.support.ui.ExpectedConditions.frameToBeAvailableAndSwitchToIt;
import static org.synyx.urlaubsverwaltung.ui.PageConditions.elementHasAttributeWithValue;

public class NavigationPage implements Page {

    private static final By NEW_APPLICATION_SELECTOR = By.id("application-new-link");
    private static final By SETTINGS_SELECTOR = By.cssSelector("[data-test-id=navigation-settings-link]");

    private final WebDriver driver;
    private final AvatarMenu avatarMenu;
    private final AddSomethingNewMenu addSomethingNewMenu;

    public NavigationPage(WebDriver driver) {
        this.avatarMenu = new AvatarMenu(driver);
        this.addSomethingNewMenu = new AddSomethingNewMenu(driver);
        this.driver = driver;
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
        addSomethingNewMenu.newApplication();
    }

    public void clickSettings() {
        driver.findElement(SETTINGS_SELECTOR).click();
    }

    public void newSickNote() {
        addSomethingNewMenu.newSickNote();
    }

    public void newOvertime() {
        addSomethingNewMenu.newOvertime();
    }

    private static boolean newApplicationLinkVisible(WebDriver driver) {
        final WebElement element = driver.findElement(NEW_APPLICATION_SELECTOR);
        return element != null && element.isDisplayed();
    }

    private static class AddSomethingNewMenu {
        private static final By BUTTON_SELECTOR = By.cssSelector("[data-test-id=add-something-new]");
        private static final By POPUPMENU_SELECTOR = By.cssSelector("[data-test-id=add-something-new-popupmenu]");
        private static final By APPLICATION_SELECTOR = By.cssSelector("[data-test-id=add-new-application]");
        private static final By SICKNOTE_SELECTOR = By.cssSelector("[data-test-id=add-new-sicknote]");
        private static final By OVERTIME_SELECTOR = By.cssSelector("[data-test-id=add-new-overtime]");

        private final WebDriver driver;
        private final WebDriverWait wait;

        AddSomethingNewMenu(WebDriver driver) {
            this.driver = driver;
            this.wait = new WebDriverWait(driver, 5);
        }

        void newApplication() {
            openPopupMenu();
            wait.until(elementToBeClickable(APPLICATION_SELECTOR));
            driver.findElement(APPLICATION_SELECTOR).click();
        }

        void newOvertime() {
            openPopupMenu();
            wait.until(elementToBeClickable(OVERTIME_SELECTOR));
            driver.findElement(OVERTIME_SELECTOR).click();
        }

        void newSickNote() {
            openPopupMenu();
            wait.until(elementToBeClickable(SICKNOTE_SELECTOR));
            driver.findElement(SICKNOTE_SELECTOR).click();
        }

        private void openPopupMenu() {
            wait.until(elementHasAttributeWithValue(BUTTON_SELECTOR, "aria-expanded", "false"));
            wait.until(elementHasAttributeWithValue(POPUPMENU_SELECTOR, "aria-hidden", "true"));

            driver.findElement(BUTTON_SELECTOR).click();

            wait.until(elementHasAttributeWithValue(BUTTON_SELECTOR, "aria-expanded", "true"));
            wait.until(elementHasAttributeWithValue(POPUPMENU_SELECTOR, "aria-hidden", "false"));
        }
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
