package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.synyx.urlaubsverwaltung.ui.Page;

import java.time.Duration;
import java.util.Map;

import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

public class NavigationPage implements Page {

    private static final By NEW_APPLICATION_SELECTOR = By.id("application-new-link");
    private static final By SICK_NOTES_SELECTOR = By.cssSelector("[data-test-id=navigation-sick-notes-link]");
    private static final By SETTINGS_SELECTOR = By.cssSelector("[data-test-id=navigation-settings-link]");

    private final WebDriver driver;
    private final AvatarMenu avatarMenu;
    public final QuickAdd quickAdd;

    public NavigationPage(WebDriver driver) {
        this.avatarMenu = new AvatarMenu(driver);
        this.quickAdd = new QuickAdd(driver);
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return newApplicationLinkVisible(driver);
    }

    public void logout() {
        avatarMenu.logout();
    }

    public void clickSickNotes() {
        driver.findElement(SICK_NOTES_SELECTOR).click();
    }

    public void clickSettings() {
        driver.findElement(SETTINGS_SELECTOR).click();
    }


    private static boolean newApplicationLinkVisible(WebDriver driver) {
        final WebElement element = driver.findElement(NEW_APPLICATION_SELECTOR);
        return element != null && element.isDisplayed();
    }

    public static class QuickAdd {
        private static final By PLAIN_APPLICATION_SELECTOR = By.cssSelector("[data-test-id=new-application]");
        private static final By BUTTON_SELECTOR = By.cssSelector("[data-test-id=add-something-new]");
        private static final By POPUPMENU_SELECTOR = By.cssSelector("[data-test-id=add-something-new-popupmenu]");
        private static final By APPLICATION_SELECTOR = By.cssSelector("[data-test-id=quick-add-new-application]");
        private static final By SICKNOTE_SELECTOR = By.cssSelector("[data-test-id=quick-add-new-sicknote]");
        private static final By OVERTIME_SELECTOR = By.cssSelector("[data-test-id=quick-add-new-overtime]");

        private final WebDriver driver;
        private final WebDriverWait wait;

        private QuickAdd(WebDriver driver) {
            this.driver = driver;
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }

        public boolean hasPopup() {
            return !driver.findElements(POPUPMENU_SELECTOR).isEmpty()
                && driver.findElements(PLAIN_APPLICATION_SELECTOR).isEmpty();
        }

        public void click() {
            if (hasPopup()) {
                driver.findElement(BUTTON_SELECTOR).click();
            } else {
                driver.findElement(PLAIN_APPLICATION_SELECTOR).click();
            }
        }

        public void newApplication() {
            wait.until(elementToBeClickable(APPLICATION_SELECTOR));
            driver.findElement(APPLICATION_SELECTOR).click();
        }

        public void newOvertime() {
            wait.until(elementToBeClickable(OVERTIME_SELECTOR));
            driver.findElement(OVERTIME_SELECTOR).click();
        }

        public void newSickNote() {
            wait.until(elementToBeClickable(SICKNOTE_SELECTOR));
            driver.findElement(SICKNOTE_SELECTOR).click();
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
            this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }

        void logout() {
            driver.findElement(AVATAR_SELECTOR).click();
            wait.until(waitForElementAnimationToFinish(LOGOUT_SELECTOR));
            driver.findElement(LOGOUT_SELECTOR).click();
        }
    }

    private static ExpectedCondition<Boolean> waitForElementAnimationToFinish(final By locator) {
        return new ExpectedCondition<>() {
            private double x = 0;
            private double y = 0;
            private double width = 0;
            private double height = 0;

            private double convertToDouble(Object longValue) {
                if (longValue instanceof Long) {
                    return ((Long) longValue).doubleValue();
                }

                return (double) longValue;
            }

            @Override
            public Boolean apply(WebDriver driver) {
                final WebElement elem = driver.findElement(locator);
                final JavascriptExecutor js = (JavascriptExecutor) driver;
                final Map<String, Object> rect = (Map<String, Object>) js.executeScript("var rect = arguments[0].getBoundingClientRect(); return { x: rect.x, y: rect.y, width: rect.width, height: rect.height };", elem);

                double newX = convertToDouble(rect.get("x"));
                double newY = convertToDouble(rect.get("y"));
                double newWidth = convertToDouble(rect.get("width"));
                double newHeight = convertToDouble(rect.get("height"));

                if (newX != x || newY != y || newWidth != width || newHeight != height) {
                    x = newX;
                    y = newY;
                    width = newWidth;
                    height = newHeight;
                    return false;
                }

                return true;
            }

            @Override
            public String toString() {
                return String.format("CSS Selector: \"%s\"", locator.toString());
            }
        };
    }
}
