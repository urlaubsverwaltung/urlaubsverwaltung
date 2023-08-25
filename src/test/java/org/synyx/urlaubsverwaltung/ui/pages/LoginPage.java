package org.synyx.urlaubsverwaltung.ui.pages;

import com.microsoft.playwright.Page;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;

public class LoginPage {

    private static final String USERNAME_SELECTOR = "#username";
    private static final String PASSWORD_SELECTOR = "#password";
    private static final String SUBMIT_SELECTOR = "button[type=submit]";

    private final Page page;
    private final MessageSource messageSource;
    private final Locale locale;

    public LoginPage(Page page, MessageSource messageSource, Locale locale) {
        this.page = page;
        this.messageSource = messageSource;
        this.locale = locale;
    }

    public boolean isVisible() {
        return webpageTitleIsShown(page) && usernameElementExists(page) && passwordElementExists(page);
    }

    /**
     * Fills the login form with the given credentials and submits the login form.
     *
     * @param credentials username and password
     */
    public void login(Credentials credentials) {
        page.fill(USERNAME_SELECTOR, credentials.username);
        page.fill(PASSWORD_SELECTOR, credentials.username);
        page.locator(SUBMIT_SELECTOR).click();
        page.waitForLoadState(DOMCONTENTLOADED);
    }

    private boolean webpageTitleIsShown(Page page) {
        final String loginText = messageSource.getMessage("login.title", new Object[]{}, locale);
        return page.title().equals(loginText);
    }

    private static boolean usernameElementExists(Page page) {
        return page.locator(USERNAME_SELECTOR) != null;
    }

    private static boolean passwordElementExists(Page page) {
        return page.locator(PASSWORD_SELECTOR) != null;
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
