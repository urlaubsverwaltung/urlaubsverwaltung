package org.synyx.urlaubsverwaltung.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.synyx.urlaubsverwaltung.ui.Page;

public class LoginPage implements Page {

    private static final By USERNAME_SELECTOR = By.id("username");
    private static final By PASSWORD_SELECTOR = By.id("password");
    private static final By SUBMIT_SELECTOR = By.cssSelector("button[type=submit]");

    private final WebDriver driver;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean isVisible(WebDriver driver) {
        return webpageTitleIsShown(driver) && usernameElementExists(driver) && passwordElementExists(driver);
    }

    /**
     * Fills the login form with the given credentials and submits the login form.
     *
     * @param credentials username and password
     */
    public void login(Credentials credentials) {
        driver.findElement(USERNAME_SELECTOR).sendKeys(credentials.username);
        driver.findElement(PASSWORD_SELECTOR).sendKeys(credentials.password);
        driver.findElement(SUBMIT_SELECTOR).click();
    }

    private static boolean webpageTitleIsShown(WebDriver driver) {
        return driver.getTitle().equals("Login");
    }

    private static boolean usernameElementExists(WebDriver driver) {
        return !driver.findElements(USERNAME_SELECTOR).isEmpty();
    }

    private static boolean passwordElementExists(WebDriver driver) {
        return !driver.findElements(PASSWORD_SELECTOR).isEmpty();
    }

    public static class Credentials {

        public final String username;
        public final String password;

        public Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }
}
