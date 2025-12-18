package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

public class LoginPage {

    private static final String USERNAME_SELECTOR = "#username";
    private static final String PASSWORD_SELECTOR = "#password";
    private static final String SUBMIT_SELECTOR = "#kc-login";

    private final Page page;
    private final int port;

    public LoginPage(Page page, int port) {
        this.page = page;
        this.port = port;
    }

    public void isVisible() {
        page.waitForSelector(USERNAME_SELECTOR);
        page.waitForSelector(PASSWORD_SELECTOR);
    }

    /**
     * Fills the login form with the given credentials and submits the login form.
     *
     * @param credentials username and password
     */
    public void login(Credentials credentials) {
        page.navigate("http://localhost:" + port + "/oauth2/authorization/keycloak");
        this.isVisible();
        page.fill(USERNAME_SELECTOR, credentials.username);
        page.fill(PASSWORD_SELECTOR, credentials.password);
        page.locator(SUBMIT_SELECTOR).click();
    }

    public record Credentials(String username, String password) {
    }
}
