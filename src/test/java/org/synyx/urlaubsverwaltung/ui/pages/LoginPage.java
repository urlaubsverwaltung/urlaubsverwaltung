package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;

import static org.synyx.urlaubsverwaltung.ui.pages.UvPage.clickAndWaitForPageRefresh;

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

    /**
     * Fills the login form with the given credentials, submits the login form and waits for page refresh.
     *
     * @param credentials username and password
     */
    public void login(Credentials credentials) {

        page.navigate("http://localhost:" + port + "/oauth2/authorization/keycloak");

        page.waitForSelector(USERNAME_SELECTOR).fill(credentials.username());
        page.waitForSelector(PASSWORD_SELECTOR).fill(credentials.password());

        clickAndWaitForPageRefresh(page, page.locator(SUBMIT_SELECTOR));
    }

    public record Credentials(String username, String password) {
    }
}
