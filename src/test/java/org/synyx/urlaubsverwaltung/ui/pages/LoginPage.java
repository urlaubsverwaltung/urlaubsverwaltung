package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class LoginPage {

    private static final String USERNAME_SELECTOR = "#username";
    private static final String PASSWORD_SELECTOR = "#password";
    private static final String SUBMIT_SELECTOR = "#kc-login";

    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public boolean isVisible() {
        return usernameElementExists(page) && passwordElementExists(page);
    }

    /**
     * Fills the login form with the given credentials and submits the login form.
     *
     * @param credentials username and password
     */
    public void login(Credentials credentials) {
        page.fill(USERNAME_SELECTOR, credentials.username);
        page.fill(PASSWORD_SELECTOR, credentials.password);
        page.waitForResponse(Response::ok, () -> page.locator(SUBMIT_SELECTOR).click());
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    private static boolean usernameElementExists(Page page) {
        return page.locator(USERNAME_SELECTOR) != null;
    }

    private static boolean passwordElementExists(Page page) {
        return page.locator(PASSWORD_SELECTOR) != null;
    }

    public record Credentials(String username, String password) {
    }
}
